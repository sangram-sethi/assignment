package approval;
import customer.Customer;

public interface LoanApprovalStrategy {

    boolean approve(Customer customer);

}
