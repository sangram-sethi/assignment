package org.northernarc.assessment4.repository;

import org.northernarc.assessment4.model.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // --- Task 3: Derived query ---
    List<Transaction> findByTransactionType(String transactionType);

    // --- Task 4: JPQL query with LIMIT via Pageable ---

    /**
     * Returns the most recent transaction(s). Combine with
     * {@code PageRequest.of(0, 1)} to fetch a single latest record.
     */
    @Query("SELECT t FROM Transaction t ORDER BY t.transactionDate DESC, t.transactionId DESC")
    List<Transaction> findLatestTransaction(Pageable pageable);
}
