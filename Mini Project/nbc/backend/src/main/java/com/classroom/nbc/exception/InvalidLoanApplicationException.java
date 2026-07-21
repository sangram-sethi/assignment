package com.classroom.nbc.exception;

public class InvalidLoanApplicationException extends RuntimeException {

    public InvalidLoanApplicationException(String message) {
        super(message);
    }
}
