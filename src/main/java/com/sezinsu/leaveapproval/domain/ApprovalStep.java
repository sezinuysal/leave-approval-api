package com.sezinsu.leaveapproval.domain;

/** Approval steps in the order they are processed: MANAGER first, then HR. */
public enum ApprovalStep {
    MANAGER,
    HR
}
