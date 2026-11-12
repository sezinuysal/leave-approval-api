package com.sezinsu.leaveapproval.exception;

/** Thrown when an operation is not allowed in the current state (maps to HTTP 409). */
public class InvalidOperationException extends RuntimeException {

    public InvalidOperationException(String message) {
        super(message);
    }
}
