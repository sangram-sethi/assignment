import customer.Customer;
import exception.InvalidLoanException;
import loan.*;
import approval.*;


public class Main {

    public static void main(String[] args) throws InvalidLoanException {

        Customer customer =
                new Customer(
                        "C101",
                        "Sangram",
                        "ABCDE1234F",
                        780);

        Loan loan =
                new PersonalLoan(
                        500000,
                        14,
                        36);

        LoanApprovalStrategy strategy =
                new PersonalLoanApproval();

        // if(strategy.approve(customer)) {

        //     System.out.println("Approved");

        //     System.out.println(
        //             "EMI = " +
        //             loan.calculateEMI());

        // } else {

        //     System.out.println("Rejected");
        // }

        try{
            if(strategy.approve(customer)) {
                System.out.println("Approved");
            }
        } catch(Exception e) {
            throw new InvalidLoanException("Credit score too Low !!!");
        }
    }
}
