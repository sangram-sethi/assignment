package com.assignment.nbfc.entity;

/**
 * Immutable loan application entity received from a partner bank / NBFC.
 *
 * <p>Wire format of an incoming record:
 * {@code APPLICATION_ID|CUSTOMER_NAME|LENDER_NAME|LOAN_TYPE|LOAN_AMOUNT|CREDIT_SCORE}
 */
public final class LoanApplication {

    private final String applicationId;
    private final String customerName;
    private final String lenderName;
    private final String loanType;
    private final double loanAmount;
    private final int creditScore;

    public LoanApplication(String applicationId,
                           String customerName,
                           String lenderName,
                           String loanType,
                           double loanAmount,
                           int creditScore) {

        this.applicationId = applicationId;
        this.customerName = customerName;
        this.lenderName = lenderName;
        this.loanType = loanType;
        this.loanAmount = loanAmount;
        this.creditScore = creditScore;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getLenderName() {
        return lenderName;
    }

    public String getLoanType() {
        return loanType;
    }

    public double getLoanAmount() {
        return loanAmount;
    }

    public int getCreditScore() {
        return creditScore;
    }

    @Override
    public String toString() {
        return applicationId + "|" + customerName + "|" + lenderName + "|"
                + loanType + "|" + loanAmount + "|" + creditScore;
    }
}
