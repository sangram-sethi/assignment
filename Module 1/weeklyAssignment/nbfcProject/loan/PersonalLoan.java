package loan;

public class PersonalLoan extends Loan {
    public PersonalLoan(double amount,
                        double roi,
                        int tenure) {

        super(amount, roi, tenure);
    }

    @Override
    public double calculateEMI() {

        double r = roi / (12 * 100);

        return amount * r *
                Math.pow(1+r, tenure) /
                (Math.pow(1+r, tenure)-1);
    }
}
