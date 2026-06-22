package com.assignment.nbfc.dto;

/**
 * Mutable transport object used to receive a single loan application from a REST
 * client as JSON. It is mapped onto the immutable
 * {@link com.assignment.nbfc.entity.LoanApplication} entity in the
 * controller layer.
 */
public class LoanApplicationRequest {

    private String applicationId;
    private String customerName;
    private String lenderName;
    private String loanType;
    private double loanAmount;
    private int creditScore;

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getLenderName() {
        return lenderName;
    }

    public void setLenderName(String lenderName) {
        this.lenderName = lenderName;
    }

    public String getLoanType() {
        return loanType;
    }

    public void setLoanType(String loanType) {
        this.loanType = loanType;
    }

    public double getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(double loanAmount) {
        this.loanAmount = loanAmount;
    }

    public int getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(int creditScore) {
        this.creditScore = creditScore;
    }
}
