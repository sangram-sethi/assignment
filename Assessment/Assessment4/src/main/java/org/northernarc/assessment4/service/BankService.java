package org.northernarc.assessment4.service;

import org.northernarc.assessment4.dto.CustomerSummaryDTO;
import org.northernarc.assessment4.dto.DashboardResponse;
import org.northernarc.assessment4.model.Account;
import org.northernarc.assessment4.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Business operations for the Secure Banking API.
 */
public interface BankService {

    Customer createCustomer(Customer customer);

    Account createAccount(Account account);

    Page<Account> getAccounts(Pageable pageable);

    CustomerSummaryDTO getCustomerSummary(Long customerId);

    void deleteAccount(String accountNumber);

    Account updateAccountBalance(String accountNumber, Double amount);

    DashboardResponse getDashboard();
}
