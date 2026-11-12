package com.sezinsu.leaveapproval.dto;

import com.sezinsu.leaveapproval.domain.ApprovalDecision;
import com.sezinsu.leaveapproval.domain.ApprovalStep;
import com.sezinsu.leaveapproval.domain.LeaveRequest;
import com.sezinsu.leaveapproval.domain.LeaveStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record LeaveRequestResponse(
        Long id,
        String employeeName,
        LocalDate startDate,
        LocalDate endDate,
        String reason,
        LeaveStatus status,
        ApprovalStep currentStep,
        List<DecisionResponse> decisions) {

    public record DecisionResponse(
            ApprovalStep step,
            String approver,
            boolean approved,
            String comment,
            Instant decidedAt) {

        static DecisionResponse from(ApprovalDecision decision) {
            return new DecisionResponse(decision.getStep(), decision.getApprover(),
                    decision.isApproved(), decision.getComment(), decision.getDecidedAt());
        }
    }

    public static LeaveRequestResponse from(LeaveRequest request) {
        return new LeaveRequestResponse(
                request.getId(),
                request.getEmployeeName(),
                request.getStartDate(),
                request.getEndDate(),
                request.getReason(),
                request.getStatus(),
                request.getCurrentStep(),
                request.getDecisions().stream().map(DecisionResponse::from).toList());
    }
}
