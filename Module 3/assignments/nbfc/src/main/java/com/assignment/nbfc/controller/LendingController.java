package com.assignment.nbfc.controller;

import com.assignment.nbfc.dto.LoanApplicationRequest;
import com.assignment.nbfc.entity.LoanApplication;
import com.assignment.nbfc.service.LendingAnalyticsService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for the Lending Risk Analytics Engine.
 *
 * <p>Exposes CRUD operations over loan applications plus every analytics report
 * produced by {@link LendingAnalyticsService}.
 */
@RestController
@RequestMapping("/api/loans")
public class LendingController {

    private final LendingAnalyticsService service;

    public LendingController(LendingAnalyticsService service) {
        this.service = service;
    }

    // --------------------------------------------------------------------- CRUD

    /** Create a single loan application from a JSON body. */
    @PostMapping
    public ResponseEntity<LoanApplication> create(@RequestBody LoanApplicationRequest request) {
        LoanApplication saved = service.addApplication(toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /** Bulk-ingest raw {@code id|name|lender|type|amount|score} records. */
    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Long>> bulkLoad(@RequestBody List<String> records) {
        long before = service.count();
        service.loadApplications(records);
        long after = service.count();

        Map<String, Long> summary = new LinkedHashMap<>();
        summary.put("received", records == null ? 0L : records.size());
        summary.put("stored", after);
        summary.put("added", after - before);
        return ResponseEntity.ok(summary);
    }

    @GetMapping
    public List<LoanApplication> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanApplication> findById(@PathVariable String id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable String id) {
        return service.deleteById(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        service.deleteAll();
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------- analytics

    /** Rule 3 — the {@code n} strongest credit profiles. */
    @GetMapping("/analytics/top-credit-profiles")
    public List<LoanApplication> topCreditProfiles(@RequestParam(defaultValue = "5") int n) {
        return service.topCreditProfiles(n);
    }

    /** Rule 4 — average loan amount per loan type. */
    @GetMapping("/analytics/average-by-type")
    public Map<String, Double> averageLoanAmountByType() {
        return service.averageLoanAmountByType();
    }

    /** Rule 5 — the application with the highest loan amount. */
    @GetMapping("/analytics/highest")
    public ResponseEntity<LoanApplication> highestLoanApplication() {
        return service.highestLoanApplication()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Rule 6 — lenders that offer more than one distinct loan type. */
    @GetMapping("/analytics/multi-type-lenders")
    public Set<String> lendersWithMultipleLoanTypes() {
        return service.lendersWithMultipleLoanTypes();
    }

    /** Rule 7 — applications grouped by lender. */
    @GetMapping("/analytics/by-lender")
    public Map<String, List<LoanApplication>> groupApplicationsByLender() {
        return service.groupApplicationsByLender();
    }

    /** The Real Nightmare — distinct, sorted names of suspicious applicants. */
    @GetMapping("/analytics/suspicious")
    public List<String> suspiciousApplications() {
        return service.suspiciousApplications();
    }

    /** Bonus — for every loan type, the top applicant of each lender. */
    @GetMapping("/analytics/top-applicant-by-lender")
    public Map<String, Map<String, LoanApplication>> loanTypeWiseTopApplicantByLender() {
        Map<String, Map<String, LoanApplication>> result = new TreeMap<>();
        service.loanTypeWiseTopApplicantByLender().forEach((loanType, byLender) -> {
            Map<String, LoanApplication> topByLender = new TreeMap<>();
            byLender.forEach((lender, top) -> top.ifPresent(app -> topByLender.put(lender, app)));
            result.put(loanType, topByLender);
        });
        return result;
    }

    // ----------------------------------------------------------- error handling

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleInvalidApplication(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    private LoanApplication toEntity(LoanApplicationRequest request) {
        return new LoanApplication(
                request.getApplicationId(),
                request.getCustomerName(),
                request.getLenderName(),
                request.getLoanType(),
                request.getLoanAmount(),
                request.getCreditScore());
    }
}
