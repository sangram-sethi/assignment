package approval;
import customer.Customer;

public class PersonalLoanApproval
        implements LoanApprovalStrategy {

    @Override
    public boolean approve(Customer customer) {

        return customer.getCreditScore() >= 700;
    }
}
