package com.aalto.paycraft.exception;

public class PayrollNotFound extends RuntimeException {
    public PayrollNotFound(String message) {
        super(message);
    }
}
