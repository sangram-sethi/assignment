package com.assignment.nbfc.service;

import com.assignment.nbfc.entity.LoanApplication;
import com.assignment.nbfc.repository.LoanApplicationRepository;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

/**
 * Real-Time Lending Risk Analytics Engine (business layer).
 *
 * <p>Parses and validates raw loan application records, delegates persistence and
 * deduplication to {@link LoanApplicationRepository}, and produces the risk and
 * analytics reports consumed by the REST layer.
 */
@Service
public class LendingAnalyticsService {

    private final LoanApplicationRepository repository;

    public LendingAnalyticsService(LoanApplicationRepository repository) {
        this.repository = repository;
    }

    // ----------------------------------------------------------- ingestion / CRUD

    /**
     * Rule 1 + Rule 2 — parse, validate and store the incoming raw records.
     *
     * <p>Invalid records (Rule 2) are ignored: {@code null} / blank lines, lines
     * not containing exactly six fields, empty mandatory fields, non-numeric
     * amount / score, loan amount {@code <= 0}, or a credit score outside the
     * inclusive range {@code [300, 900]}. Duplicate ids (Rule 1) are resolved by
     * the repository.
     */
    public void loadApplications(List<String> records) {
        if (records == null) {
            return;
        }
        for (String record : records) {
            if (record == null || record.trim().isEmpty()) {
                continue;
            }
            String[] fields = record.split("\\|", -1);
            if (fields.length != 6) {
                continue;
            }
            String applicationId = fields[0].trim();
            String customerName = fields[1].trim();
            String lenderName = fields[2].trim();
            String loanType = fields[3].trim();
            String amountText = fields[4].trim();
            String scoreText = fields[5].trim();

            if (applicationId.isEmpty() || customerName.isEmpty()
                    || lenderName.isEmpty() || loanType.isEmpty()) {
                continue;
            }

            double loanAmount;
            int creditScore;
            try {
                loanAmount = Double.parseDouble(amountText);
                creditScore = Integer.parseInt(scoreText);
            } catch (NumberFormatException ex) {
                continue;
            }

            if (loanAmount <= 0 || creditScore < 300 || creditScore > 900) {
                continue;
            }

            repository.save(new LoanApplication(
                    applicationId, customerName, lenderName, loanType, loanAmount, creditScore));
        }
    }

    /**
     * Validates and stores a single structured application, applying the same
     * business rules as {@link #loadApplications(List)}.
     *
     * @throws IllegalArgumentException when any mandatory field is blank, the loan
     *                                  amount is non-positive, or the credit score
     *                                  is outside {@code [300, 900]}
     */
    public LoanApplication addApplication(LoanApplication application) {
        validate(application);
        return repository.save(application);
    }

    public List<LoanApplication> findAll() {
        return repository.findAll();
    }

    public Optional<LoanApplication> findById(String applicationId) {
        return repository.findById(applicationId);
    }

    public boolean deleteById(String applicationId) {
        return repository.deleteById(applicationId);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public long count() {
        return repository.count();
    }

    private void validate(LoanApplication application) {
        if (application == null) {
            throw new IllegalArgumentException("Loan application must not be null");
        }
        if (isBlank(application.getApplicationId()) || isBlank(application.getCustomerName())
                || isBlank(application.getLenderName()) || isBlank(application.getLoanType())) {
            throw new IllegalArgumentException(
                    "applicationId, customerName, lenderName and loanType are mandatory");
        }
        if (application.getLoanAmount() <= 0) {
            throw new IllegalArgumentException("loanAmount must be greater than 0");
        }
        if (application.getCreditScore() < 300 || application.getCreditScore() > 900) {
            throw new IllegalArgumentException("creditScore must be within the inclusive range [300, 900]");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    // ----------------------------------------------------------------- analytics

    /**
     * Rule 3 — the {@code n} strongest credit profiles, ordered by credit score
     * descending, then loan amount ascending, then customer name ascending.
     */
    public List<LoanApplication> topCreditProfiles(int n) {
        if (n <= 0) {
            return List.of();
        }
        return repository.findAll().stream()
                .sorted(Comparator.comparingInt(LoanApplication::getCreditScore).reversed()
                        .thenComparingDouble(LoanApplication::getLoanAmount)
                        .thenComparing(LoanApplication::getCustomerName))
                .limit(n)
                .collect(Collectors.toList());
    }

    /**
     * Rule 4 — average loan amount per loan type, sorted by loan type and rounded
     * to two decimal places.
     */
    public Map<String, Double> averageLoanAmountByType() {
        return repository.findAll().stream()
                .collect(Collectors.groupingBy(
                        LoanApplication::getLoanType,
                        TreeMap::new,
                        Collectors.collectingAndThen(
                                Collectors.averagingDouble(LoanApplication::getLoanAmount),
                                average -> Math.round(average * 100.0) / 100.0)));
    }

    /**
     * Rule 5 — the application with the highest loan amount, breaking ties by
     * higher credit score and then by smaller application id.
     */
    public Optional<LoanApplication> highestLoanApplication() {
        return repository.findAll().stream()
                .max(Comparator.comparingDouble(LoanApplication::getLoanAmount)
                        .thenComparingInt(LoanApplication::getCreditScore)
                        .thenComparing(LoanApplication::getApplicationId, Comparator.reverseOrder()));
    }

    /**
     * Rule 6 — lenders that offer more than one distinct loan type, sorted.
     */
    public Set<String> lendersWithMultipleLoanTypes() {
        return repository.findAll().stream()
                .collect(Collectors.groupingBy(
                        LoanApplication::getLenderName,
                        Collectors.mapping(LoanApplication::getLoanType, Collectors.toSet())))
                .entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * Rule 7 — applications grouped by lender (lenders sorted alphabetically),
     * with each lender's applications ordered by credit score descending and then
     * loan amount ascending.
     */
    public Map<String, List<LoanApplication>> groupApplicationsByLender() {
        Comparator<LoanApplication> withinLender =
                Comparator.comparingInt(LoanApplication::getCreditScore).reversed()
                        .thenComparingDouble(LoanApplication::getLoanAmount);

        Map<String, List<LoanApplication>> sorted = repository.findAll().stream()
                .collect(Collectors.groupingBy(
                        LoanApplication::getLenderName,
                        TreeMap::new,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream().sorted(withinLender).collect(Collectors.toList()))));

        return new LinkedHashMap<>(sorted);
    }

    /**
     * The Real Nightmare — distinct, alphabetically sorted customer names of every
     * suspicious application.
     *
     * <p>Implemented purely with the Stream API: no {@code for}/{@code while}
     * loops, no recursion and no helper methods. An application is suspicious when
     * <em>any</em> of the eight fraud conditions described in the specification
     * holds.
     */
    public List<String> suspiciousApplications() {
        Collection<LoanApplication> apps = repository.values();

        // Loan-type level aggregates (precise, un-rounded) used by conditions 3 & 4.
        Map<String, Double> avgAmountByType = apps.stream()
                .collect(Collectors.groupingBy(LoanApplication::getLoanType,
                        Collectors.averagingDouble(LoanApplication::getLoanAmount)));
        Map<String, Double> avgScoreByType = apps.stream()
                .collect(Collectors.groupingBy(LoanApplication::getLoanType,
                        Collectors.averagingDouble(LoanApplication::getCreditScore)));

        // Condition 6 — customer names that applied with more than 3 distinct lenders.
        Set<String> manyLenderCustomers = apps.stream()
                .collect(Collectors.groupingBy(LoanApplication::getCustomerName,
                        Collectors.mapping(LoanApplication::getLenderName, Collectors.toSet())))
                .entrySet().stream()
                .filter(entry -> entry.getValue().size() > 3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        // Conditions 1-6 — per-application checks.
        Stream<String> perApplication = apps.stream()
                .filter(app -> {
                    String name = app.getCustomerName();
                    String[] words = name.trim().split("\\s+");
                    double avgAmount = avgAmountByType.getOrDefault(app.getLoanType(), 0.0);
                    double avgScore = avgScoreByType.getOrDefault(app.getLoanType(), 0.0);
                    return
                            // Condition 1 — consecutive repeated words (case-insensitive).
                            IntStream.range(1, words.length)
                                    .anyMatch(i -> words[i].equalsIgnoreCase(words[i - 1]))
                            // Condition 2 — lender name appears inside the customer name.
                            || name.toLowerCase().contains(app.getLenderName().toLowerCase())
                            // Condition 3 — loan amount exceeds 250% of the loan-type average.
                            || (avgAmount > 0 && app.getLoanAmount() > avgAmount * 2.5)
                            // Condition 4 — below-average score AND above-average amount for the type.
                            || (app.getCreditScore() < avgScore && app.getLoanAmount() > avgAmount)
                            // Condition 5 — customer name has more than 3 words.
                            || words.length > 3
                            // Condition 6 — customer spread across more than 3 lenders.
                            || manyLenderCustomers.contains(name);
                })
                .map(LoanApplication::getCustomerName);

        // Condition 7 — same loan type + amount + score shared by different customers.
        Stream<String> condition7 = apps.stream()
                .collect(Collectors.groupingBy(
                        app -> app.getLoanType() + '\u0000' + app.getLoanAmount() + '\u0000' + app.getCreditScore(),
                        Collectors.mapping(LoanApplication::getCustomerName, Collectors.toSet())))
                .values().stream()
                .filter(names -> names.size() > 1)
                .flatMap(Set::stream);

        // Condition 8 — anagram of another customer name within the same lender.
        Stream<String> condition8 = apps.stream()
                .collect(Collectors.groupingBy(
                        app -> app.getLenderName() + '\u0000' + app.getCustomerName().toLowerCase().codePoints()
                                .filter(c -> !Character.isWhitespace(c))
                                .sorted()
                                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                                .toString(),
                        Collectors.mapping(LoanApplication::getCustomerName, Collectors.toSet())))
                .values().stream()
                .filter(names -> names.size() > 1)
                .flatMap(Set::stream);

        return Stream.concat(perApplication, Stream.concat(condition7, condition8))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Bonus Challenge (Architect Level) — for every loan type, the top applicant
     * of each lender, expressed only through nested {@code groupingBy}. The top
     * applicant is the one with the highest credit score, then the lowest loan
     * amount, then the smallest customer name.
     */
    public Map<String, Map<String, Optional<LoanApplication>>> loanTypeWiseTopApplicantByLender() {
        Comparator<LoanApplication> topApplicant =
                Comparator.comparingInt(LoanApplication::getCreditScore)
                        .thenComparing(Comparator.comparingDouble(LoanApplication::getLoanAmount).reversed())
                        .thenComparing(Comparator.comparing(LoanApplication::getCustomerName).reversed());

        return repository.findAll().stream()
                .collect(Collectors.groupingBy(
                        LoanApplication::getLoanType,
                        Collectors.groupingBy(
                                LoanApplication::getLenderName,
                                Collectors.maxBy(topApplicant))));
    }
}
