package loan;

public abstract class Loan {
    protected String loanId;
    protected double amount;
    protected double roi;
    protected int tenure;

    public Loan(double amount,
                double roi,
                int tenure) {

        this.amount = amount;
        this.roi = roi;
        this.tenure = tenure;
    }

    public abstract double calculateEMI();
}
