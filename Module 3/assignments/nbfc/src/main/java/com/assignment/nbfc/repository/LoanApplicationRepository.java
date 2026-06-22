package com.assignment.nbfc.repository;

import com.assignment.nbfc.entity.LoanApplication;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * In-memory data store for {@link LoanApplication} entities.
 *
 * <p>Applications are held in a {@link Map} keyed by their unique application id,
 * which both guarantees uniqueness and gives O(1) lookups. Duplicate ids are
 * resolved on {@link #save(LoanApplication)} using {@link #DUPLICATE_RESOLVER}.
 */
@Repository
public class LoanApplicationRepository {

    /** Deduplicated applications keyed by their unique application id. */
    private final Map<String, LoanApplication> applications = new HashMap<>();

    /**
     * Duplicate-resolution ordering. The "winning" record sorts first: higher
     * credit score, then lower loan amount, then lexicographically smaller
     * customer name.
     */
    private static final Comparator<LoanApplication> DUPLICATE_RESOLVER =
            Comparator.comparingInt(LoanApplication::getCreditScore).reversed()
                    .thenComparingDouble(LoanApplication::getLoanAmount)
                    .thenComparing(LoanApplication::getCustomerName);

    /**
     * Stores the application, keeping the stronger record when an application with
     * the same id already exists.
     *
     * @return the record now held for that application id
     */
    public LoanApplication save(LoanApplication application) {
        return applications.merge(application.getApplicationId(), application,
                (existing, incoming) ->
                        DUPLICATE_RESOLVER.compare(existing, incoming) <= 0 ? existing : incoming);
    }

    public Optional<LoanApplication> findById(String applicationId) {
        return Optional.ofNullable(applications.get(applicationId));
    }

    public List<LoanApplication> findAll() {
        return new ArrayList<>(applications.values());
    }

    /** Live, read-only view of the stored applications for analytics streaming. */
    public Collection<LoanApplication> values() {
        return Collections.unmodifiableCollection(applications.values());
    }

    public boolean existsById(String applicationId) {
        return applications.containsKey(applicationId);
    }

    public boolean deleteById(String applicationId) {
        return applications.remove(applicationId) != null;
    }

    public void deleteAll() {
        applications.clear();
    }

    public long count() {
        return applications.size();
    }
}
