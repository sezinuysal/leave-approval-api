package com.sezinsu.leaveapproval.domain;

import com.sezinsu.leaveapproval.exception.InvalidOperationException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A leave request that goes through a two-step approval workflow:
 * MANAGER first, then HR. A rejection at any step ends the workflow.
 */
@Entity
@Table(name = "leave_requests")
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String employeeName;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveStatus status = LeaveStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStep currentStep = ApprovalStep.MANAGER;

    @OneToMany(mappedBy = "leaveRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("decidedAt ASC, id ASC")
    private List<ApprovalDecision> decisions = new ArrayList<>();

    protected LeaveRequest() {
        // required by JPA
    }

    public LeaveRequest(String employeeName, LocalDate startDate, LocalDate endDate, String reason) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must not be before startDate");
        }
        this.employeeName = employeeName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
    }

    /**
     * Records a decision for the current step and moves the workflow forward.
     *
     * @throws InvalidOperationException if the request is already approved or rejected
     */
    public void decide(String approver, boolean approved, String comment, Instant decidedAt) {
        if (status != LeaveStatus.PENDING) {
            throw new InvalidOperationException("Leave request is already " + status);
        }
        decisions.add(new ApprovalDecision(this, currentStep, approver, approved, comment, decidedAt));

        if (!approved) {
            status = LeaveStatus.REJECTED;
        } else if (currentStep == ApprovalStep.MANAGER) {
            currentStep = ApprovalStep.HR;
        } else {
            status = LeaveStatus.APPROVED;
        }
    }

    public Long getId() {
        return id;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getReason() {
        return reason;
    }

    public LeaveStatus getStatus() {
        return status;
    }

    public ApprovalStep getCurrentStep() {
        return currentStep;
    }

    public List<ApprovalDecision> getDecisions() {
        return Collections.unmodifiableList(decisions);
    }
}
