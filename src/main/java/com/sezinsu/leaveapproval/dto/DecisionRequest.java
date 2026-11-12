package com.sezinsu.leaveapproval.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DecisionRequest(
        @NotBlank(message = "approver is required") String approver,
        @NotNull(message = "approved is required") Boolean approved,
        String comment) {
}
