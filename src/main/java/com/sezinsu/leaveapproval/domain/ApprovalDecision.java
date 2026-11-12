package com.sezinsu.leaveapproval.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

/** One approver's decision on one step of a leave request. */
@Entity
@Table(name = "approval_decisions")
public class ApprovalDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leave_request_id")
    private LeaveRequest leaveRequest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStep step;

    @Column(nullable = false)
    private String approver;

    @Column(nullable = false)
    private boolean approved;

    private String comment;

    @Column(nullable = false)
    private Instant decidedAt;

    protected ApprovalDecision() {
        // required by JPA
    }

    ApprovalDecision(LeaveRequest leaveRequest, ApprovalStep step, String approver,
                     boolean approved, String comment, Instant decidedAt) {
        this.leaveRequest = leaveRequest;
        this.step = step;
        this.approver = approver;
        this.approved = approved;
        this.comment = comment;
        this.decidedAt = decidedAt;
    }

    public Long getId() {
        return id;
    }

    public ApprovalStep getStep() {
        return step;
    }

    public String getApprover() {
        return approver;
    }

    public boolean isApproved() {
        return approved;
    }

    public String getComment() {
        return comment;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }
}
