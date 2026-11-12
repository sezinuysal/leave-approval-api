package com.sezinsu.leaveapproval.domain;

import com.sezinsu.leaveapproval.exception.InvalidOperationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LeaveRequestTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    private LeaveRequest newRequest() {
        return new LeaveRequest("Ayse", LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 6), "Holiday");
    }

    @Test
    void newRequestStartsPendingAtManagerStep() {
        LeaveRequest request = newRequest();

        assertEquals(LeaveStatus.PENDING, request.getStatus());
        assertEquals(ApprovalStep.MANAGER, request.getCurrentStep());
    }

    @Test
    void managerApprovalMovesRequestToHrStep() {
        LeaveRequest request = newRequest();

        request.decide("manager", true, "ok", NOW);

        assertEquals(LeaveStatus.PENDING, request.getStatus());
        assertEquals(ApprovalStep.HR, request.getCurrentStep());
    }

    @Test
    void approvalByManagerAndHrApprovesRequest() {
        LeaveRequest request = newRequest();

        request.decide("manager", true, null, NOW);
        request.decide("hr", true, null, NOW);

        assertEquals(LeaveStatus.APPROVED, request.getStatus());
        assertEquals(2, request.getDecisions().size());
    }

    @Test
    void rejectionAtAnyStepRejectsRequest() {
        LeaveRequest request = newRequest();
        request.decide("manager", true, null, NOW);

        request.decide("hr", false, "no capacity", NOW);

        assertEquals(LeaveStatus.REJECTED, request.getStatus());
    }

    @Test
    void deciding_afterWorkflowIsFinished_isNotAllowed() {
        LeaveRequest request = newRequest();
        request.decide("manager", false, null, NOW);

        assertThrows(InvalidOperationException.class, () -> request.decide("hr", true, null, NOW));
    }

    @Test
    void endDateBeforeStartDate_isRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new LeaveRequest("Ayse", LocalDate.of(2026, 3, 6), LocalDate.of(2026, 3, 2), null));
    }
}
