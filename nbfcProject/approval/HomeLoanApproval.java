package approval;
import customer.Customer;

public class HomeLoanApproval
        implements LoanApprovalStrategy {

    @Override
    public boolean approve(Customer customer) {

        return customer.getCreditScore() >= 750;
    }
}
