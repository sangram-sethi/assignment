package org.northernarc.assessment4.repository;

import org.northernarc.assessment4.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // --- Task 3: Derived queries ---
    List<Customer> findByBranch(String branch);

    Optional<Customer> findByEmail(String email);

    // --- Task 4: JPQL queries ---

    /**
     * Rich customers: those whose aggregate balance across all owned accounts
     * exceeds the supplied threshold.
     */
    @Query("SELECT c FROM Customer c JOIN c.accounts a " +
            "GROUP BY c HAVING SUM(a.balance) > :amount")
    List<Customer> findRichCustomers(@Param("amount") Double amount);

    /**
     * Aggregated total balance per branch: returns [branch, totalBalance].
     */
    @Query("SELECT c.branch, SUM(a.balance) FROM Customer c JOIN c.accounts a " +
            "GROUP BY c.branch")
    List<Object[]> findTotalBalancePerBranch();

    /**
     * Customers holding more than one account.
     */
    @Query("SELECT c FROM Customer c JOIN c.accounts a " +
            "GROUP BY c HAVING COUNT(a) > 1")
    List<Customer> findCustomersWithMultipleAccounts();

    // --- Final Challenge: optimized dashboard aggregates ---

    @Query("SELECT COUNT(c) FROM Customer c")
    long countCustomers();

    @Query("SELECT c.branch FROM Customer c JOIN c.accounts a " +
            "GROUP BY c.branch ORDER BY SUM(a.balance) DESC")
    List<String> findBranchesRankedByBalance();

    @Query("SELECT c.customerName FROM Customer c JOIN c.accounts a " +
            "GROUP BY c.customerName ORDER BY SUM(a.balance) DESC")
    List<String> findCustomersRankedByBalance();
}
