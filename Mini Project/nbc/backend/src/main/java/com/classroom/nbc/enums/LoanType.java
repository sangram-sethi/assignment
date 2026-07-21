package com.classroom.nbc.enums;

import java.math.BigDecimal;

public enum LoanType {
    HOME_LOAN(new BigDecimal("8.50")),
    PERSONAL_LOAN(new BigDecimal("12.00")),
    VEHICLE_LOAN(new BigDecimal("9.50")),
    EDUCATION_LOAN(new BigDecimal("10.00")),
    BUSINESS_LOAN(new BigDecimal("11.50"));

    private final BigDecimal annualInterestRate;

    LoanType(BigDecimal annualInterestRate) {
        this.annualInterestRate = annualInterestRate;
    }

    public BigDecimal getAnnualInterestRate() {
        return annualInterestRate;
    }
}
