package org.northernarc.assessment4.repository;

import org.northernarc.assessment4.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, String> {

    // --- Task 3: Derived queries ---
    List<Account> findByAccountType(String accountType);

    List<Account> findByBalanceGreaterThan(Double amount);

    // --- Task 4: JPQL query ---

    /**
     * Accounts that have never had a transaction logged against them.
     */
    @Query("SELECT a FROM Account a LEFT JOIN a.transactions t WHERE t IS NULL")
    List<Account> findAccountsWithNoTransactions();

    // --- Task 5: Modifying JPQL update ---

    /**
     * Batch increment of an account's balance.
     *
     * @return the number of rows affected (1 when the account exists).
     */
    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.balance = a.balance + :amount " +
            "WHERE a.accountNumber = :accountNumber")
    int increaseBalance(@Param("accountNumber") String accountNumber,
                        @Param("amount") Double amount);

    // --- Final Challenge: optimized dashboard aggregates ---

    @Query("SELECT COUNT(a) FROM Account a")
    long countAccounts();

    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a")
    Double sumAllBalances();
}
