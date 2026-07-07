package org.northernarc.assessment4.serviceimpl;

import org.northernarc.assessment4.dto.CustomerSummaryDTO;
import org.northernarc.assessment4.dto.DashboardResponse;
import org.northernarc.assessment4.exception.AccountNotFoundException;
import org.northernarc.assessment4.exception.CustomerNotFoundException;
import org.northernarc.assessment4.exception.EmailAlreadyExistsException;
import org.northernarc.assessment4.model.Account;
import org.northernarc.assessment4.model.Customer;
import org.northernarc.assessment4.model.Role;
import org.northernarc.assessment4.repository.AccountRepository;
import org.northernarc.assessment4.repository.CustomerRepository;
import org.northernarc.assessment4.service.BankService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BankServiceImpl implements BankService {

    private static final Logger log = LoggerFactory.getLogger(BankServiceImpl.class);

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public BankServiceImpl(CustomerRepository customerRepository,
                           AccountRepository accountRepository,
                           PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Customer createCustomer(Customer customer) {
        customerRepository.findByEmail(customer.getEmail()).ifPresent(existing -> {
            throw new EmailAlreadyExistsException(
                    "A customer is already registered with that email");
        });
        // Assign privileges server-side; self-registration can only ever be a USER.
        customer.setRole(Role.USER);
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        Customer saved = customerRepository.save(customer);
        log.info("Registered customer id={} branch={}", saved.getCustomerId(), saved.getBranch());
        return saved;
    }

    @Override
    @Transactional
    public Account createAccount(Account account) {
        Account saved = accountRepository.save(account);
        log.info("Opened account {} type={}", saved.getAccountNumber(), saved.getAccountType());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Account> getAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerSummaryDTO getCustomerSummary(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found with id: " + customerId));

        List<Account> accounts = customer.getAccounts();
        double totalBalance = accounts.stream()
                .mapToDouble(Account::getBalance)
                .sum();

        return new CustomerSummaryDTO(
                customer.getCustomerName(),
                customer.getBranch(),
                accounts.size(),
                totalBalance);
    }

    @Override
    @Transactional
    public void deleteAccount(String accountNumber) {
        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found: " + accountNumber));
        accountRepository.delete(account);
        log.warn("Deleted account {}", accountNumber);
    }

    @Override
    @Transactional
    public Account updateAccountBalance(String accountNumber, Double amount) {
        int updated = accountRepository.increaseBalance(accountNumber, amount);
        if (updated == 0) {
            throw new AccountNotFoundException("Account not found: " + accountNumber);
        }
        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found: " + accountNumber));
        log.info("Adjusted balance of account {} by {}", accountNumber, amount);
        return account;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        long totalCustomers = customerRepository.countCustomers();
        long totalAccounts = accountRepository.countAccounts();
        double totalBalance = accountRepository.sumAllBalances();

        String topBranch = customerRepository.findBranchesRankedByBalance()
                .stream().findFirst().orElse(null);
        String highestBalanceCustomer = customerRepository.findCustomersRankedByBalance()
                .stream().findFirst().orElse(null);

        return new DashboardResponse(
                totalCustomers,
                totalAccounts,
                totalBalance,
                topBranch,
                highestBalanceCustomer);
    }
}
