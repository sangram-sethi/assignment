package com.classroom.nbc.exception;

public class LoanAlreadyProcessedException extends RuntimeException {

    public LoanAlreadyProcessedException(String message) {
        super(message);
    }
}
