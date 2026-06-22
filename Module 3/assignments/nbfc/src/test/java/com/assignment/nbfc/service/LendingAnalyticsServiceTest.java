package com.assignment.nbfc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.assignment.nbfc.entity.LoanApplication;
import com.assignment.nbfc.repository.LoanApplicationRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Verifies every business rule, all eight fraud conditions, the hidden test
 * cases and the architect-level bonus of the Lending Risk Analytics Engine.
 */
class LendingAnalyticsServiceTest {

    private static LendingAnalyticsService engineFrom(String... records) {
        LendingAnalyticsService engine = new LendingAnalyticsService(new LoanApplicationRepository());
        engine.loadApplications(Arrays.asList(records));
        return engine;
    }

    // ----------------------------------------------------------------- Rule 1

    @Test
    void duplicateResolution_keepsHigherCreditScore() {
        LendingAnalyticsService engine = engineFrom(
                "A101|Rahul Sharma|HDFC|Personal Loan|500000|780",
                "A101|Rahul Sharma|HDFC|Personal Loan|450000|800");

        LoanApplication kept = engine.highestLoanApplication().orElseThrow();
        assertEquals(800, kept.getCreditScore());
        assertEquals(450000, kept.getLoanAmount());
        assertEquals(1, engine.topCreditProfiles(10).size());
    }

    @Test
    void duplicateResolution_sameScore_keepsLowerAmount() { // Hidden Test 1
        LendingAnalyticsService engine = engineFrom(
                "A1|Rahul|HDFC|Personal Loan|500000|780",
                "A1|Rahul|HDFC|Personal Loan|400000|780");

        assertEquals(400000, engine.topCreditProfiles(1).get(0).getLoanAmount());
    }

    @Test
    void duplicateResolution_sameScoreAndAmount_keepsSmallerName() { // Hidden Test 2
        LendingAnalyticsService engine = engineFrom(
                "A1|Zaheer|HDFC|Personal Loan|500000|780",
                "A1|Aarav|HDFC|Personal Loan|500000|780");

        assertEquals("Aarav", engine.topCreditProfiles(1).get(0).getCustomerName());
    }

    // ----------------------------------------------------------------- Rule 2

    @Test
    void invalidRecords_areIgnored() {
        LendingAnalyticsService engine = engineFrom(
                null,
                "   ",
                "",
                "|Rahul|HDFC|Personal Loan|500000|780",      // empty id
                "A2||HDFC|Personal Loan|500000|780",          // empty name
                "A3|Rahul||Personal Loan|500000|780",         // empty lender
                "A4|Rahul|HDFC||500000|780",                  // empty loan type
                "A5|Rahul|HDFC|Personal Loan|0|780",          // amount <= 0
                "A6|Rahul|HDFC|Personal Loan|-1|780",         // negative amount
                "A7|Rahul|HDFC|Personal Loan|500000|299",     // score < 300
                "A8|Rahul|HDFC|Personal Loan|500000|901",     // score > 900
                "A9|Rahul|HDFC|Personal Loan|abc|780",        // non-numeric amount
                "A10|Rahul|HDFC|Personal Loan|500000|xyz",    // non-numeric score
                "A11|Rahul|HDFC|Personal Loan|500000",        // too few fields
                "A12|Rahul|HDFC|Personal Loan|500000|800");   // the only valid record

        List<LoanApplication> valid = engine.topCreditProfiles(100);
        assertEquals(1, valid.size());
        assertEquals("A12", valid.get(0).getApplicationId());
    }

    @Test
    void creditScoreBoundaries_areInclusive() {
        LendingAnalyticsService engine = engineFrom(
                "A1|Min|HDFC|Personal Loan|100000|300",
                "A2|Max|HDFC|Personal Loan|100000|900");
        assertEquals(2, engine.topCreditProfiles(10).size());
    }

    // ----------------------------------------------------------------- Rule 3

    @Test
    void topCreditProfiles_ordersByScoreThenAmountThenName() {
        LendingAnalyticsService engine = engineFrom(
                "A1|Charlie|HDFC|Personal Loan|500000|800",
                "A2|Bravo|ICICI|Home Loan|300000|800",
                "A3|Alpha|Axis|Car Loan|300000|800",
                "A4|Delta|SBI|Gold Loan|100000|750");

        List<LoanApplication> top = engine.topCreditProfiles(3);
        assertEquals(List.of("Alpha", "Bravo", "Charlie"),
                top.stream().map(LoanApplication::getCustomerName).toList());
    }

    @Test
    void topCreditProfiles_nonPositiveN_returnsEmpty() {
        LendingAnalyticsService engine = engineFrom("A1|Rahul|HDFC|Personal Loan|500000|800");
        assertTrue(engine.topCreditProfiles(0).isEmpty());
        assertTrue(engine.topCreditProfiles(-5).isEmpty());
    }

    // ----------------------------------------------------------------- Rule 4

    @Test
    void averageLoanAmountByType_isSortedAndRounded() {
        LendingAnalyticsService engine = engineFrom(
                "A1|A|HDFC|Personal Loan|100000|800",
                "A2|B|ICICI|Personal Loan|150001|800",
                "A3|C|Axis|Home Loan|500000|800");

        Map<String, Double> averages = engine.averageLoanAmountByType();
        assertEquals(List.of("Home Loan", "Personal Loan"),
                List.copyOf(averages.keySet()));
        assertEquals(125000.5, averages.get("Personal Loan"));
        assertEquals(500000.0, averages.get("Home Loan"));
    }

    // ----------------------------------------------------------------- Rule 5

    @Test
    void highestLoanApplication_breaksTiesByScoreThenId() {
        LendingAnalyticsService engine = engineFrom(
                "A2|A|HDFC|Home Loan|900000|800",
                "A1|B|ICICI|Home Loan|900000|800",
                "A3|C|Axis|Home Loan|900000|750");

        Optional<LoanApplication> highest = engine.highestLoanApplication();
        assertEquals("A1", highest.orElseThrow().getApplicationId());
    }

    @Test
    void highestLoanApplication_emptyEngine_returnsEmpty() {
        assertTrue(new LendingAnalyticsService(new LoanApplicationRepository())
                .highestLoanApplication().isEmpty());
    }

    // ----------------------------------------------------------------- Rule 6

    @Test
    void lendersWithMultipleLoanTypes_returnsSortedSet() {
        LendingAnalyticsService engine = engineFrom(
                "A1|A|HDFC|Personal Loan|100000|800",
                "A2|B|HDFC|Home Loan|100000|800",
                "A3|C|ICICI|Car Loan|100000|800",
                "A4|D|ICICI|Car Loan|100000|800");

        Set<String> result = engine.lendersWithMultipleLoanTypes();
        assertEquals(Set.of("HDFC"), result);
        assertTrue(result instanceof java.util.TreeSet);
    }

    // ----------------------------------------------------------------- Rule 7

    @Test
    void groupApplicationsByLender_sortsLendersAndApplications() {
        LendingAnalyticsService engine = engineFrom(
                "A1|A|ICICI|Home Loan|500000|700",
                "A2|B|HDFC|Personal Loan|300000|800",
                "A3|C|HDFC|Personal Loan|400000|800",
                "A4|D|HDFC|Personal Loan|100000|750");

        Map<String, List<LoanApplication>> grouped = engine.groupApplicationsByLender();
        assertEquals(List.of("HDFC", "ICICI"), List.copyOf(grouped.keySet()));

        List<String> hdfcOrder = grouped.get("HDFC").stream()
                .map(LoanApplication::getApplicationId).toList();
        // score DESC then amount ASC -> 800/300k, 800/400k, 750/100k
        assertEquals(List.of("A2", "A3", "A4"), hdfcOrder);
    }

    // ------------------------------------------------------ suspicious: each rule

    @Test
    void condition1_consecutiveRepeatedWords() {
        LendingAnalyticsService engine = engineFrom(
                "A1|Rahul Rahul Sharma|HDFC|Personal Loan|100000|800",
                "A2|Clean Name|ICICI|Home Loan|200000|790");
        List<String> suspicious = engine.suspiciousApplications();
        assertTrue(suspicious.contains("Rahul Rahul Sharma"));
        assertFalse(suspicious.contains("Clean Name"));
    }

    @Test
    void condition1_caseInsensitive() { // Hidden Test 3
        LendingAnalyticsService engine = engineFrom(
                "A1|Rahul RAHUL Sharma|HDFC|Personal Loan|100000|800",
                "A2|Priya Priya Verma|ICICI|Home Loan|200000|790");
        List<String> suspicious = engine.suspiciousApplications();
        assertTrue(suspicious.contains("Rahul RAHUL Sharma"));
        assertTrue(suspicious.contains("Priya Priya Verma"));
    }

    @Test
    void condition2_lenderNameInsideCustomerName() {
        LendingAnalyticsService engine = engineFrom(
                "A1|HDFC Rahul|HDFC|Personal Loan|100000|800",
                "A2|Clean Person|ICICI|Home Loan|200000|790");
        List<String> suspicious = engine.suspiciousApplications();
        assertTrue(suspicious.contains("HDFC Rahul"));
        assertFalse(suspicious.contains("Clean Person"));
    }

    @Test
    void condition3_amountExceeds250PercentOfTypeAverage() {
        // Average = (100000 + 120000 + 1200000) / 3 = 473333.33; 250% = 1183333.33.
        // Only the outlier (1200000) breaches it, and because the outlier also has
        // the highest credit score, condition 4 cannot be what flags it.
        LendingAnalyticsService engine = engineFrom(
                "A1|Alpha|HDFC|Personal Loan|100000|770",
                "A2|Bravo|ICICI|Personal Loan|120000|780",
                "A3|Outlier|Axis|Personal Loan|1200000|800");
        List<String> suspicious = engine.suspiciousApplications();
        assertTrue(suspicious.contains("Outlier"));
        assertFalse(suspicious.contains("Alpha"));
        assertFalse(suspicious.contains("Bravo"));
    }

    @Test
    void condition4_belowAvgScoreAndAboveAvgAmount() {
        LendingAnalyticsService engine = engineFrom(
                "A1|Good One|HDFC|Home Loan|500000|820",
                "A2|Risky Two|ICICI|Home Loan|1000000|650");
        assertTrue(engine.suspiciousApplications().contains("Risky Two"));
        assertFalse(engine.suspiciousApplications().contains("Good One"));
    }

    @Test
    void condition5_moreThanThreeWords() {
        LendingAnalyticsService engine = engineFrom(
                "A1|Rahul Kumar Singh Sharma|HDFC|Personal Loan|100000|800",
                "A2|Amit Kumar Singh|ICICI|Home Loan|200000|790");
        List<String> suspicious = engine.suspiciousApplications();
        assertTrue(suspicious.contains("Rahul Kumar Singh Sharma"));
        assertFalse(suspicious.contains("Amit Kumar Singh")); // exactly 3 words
    }

    @Test
    void condition6_sameCustomerMoreThanThreeLenders() {
        LendingAnalyticsService engine = engineFrom(
                "A1|Rahul Sharma|HDFC|Personal Loan|100000|800",
                "A2|Rahul Sharma|ICICI|Personal Loan|100000|800",
                "A3|Rahul Sharma|Axis|Personal Loan|100000|800",
                "A4|Rahul Sharma|SBI|Personal Loan|100000|800");
        assertTrue(engine.suspiciousApplications().contains("Rahul Sharma"));
    }

    @Test
    void condition6_exactlyThreeLenders_isNotSuspicious() {
        LendingAnalyticsService engine = engineFrom(
                "A1|Solo Person|HDFC|Personal Loan|100000|800",
                "A2|Solo Person|ICICI|Personal Loan|100000|800",
                "A3|Solo Person|Axis|Personal Loan|100000|800");
        assertFalse(engine.suspiciousApplications().contains("Solo Person"));
    }

    @Test
    void condition7_sameAmountScoreDifferentCustomers() {
        LendingAnalyticsService engine = engineFrom(
                "A1|Rahul|HDFC|Personal Loan|500000|750",
                "A2|Priya|ICICI|Personal Loan|500000|750");
        List<String> suspicious = engine.suspiciousApplications();
        assertTrue(suspicious.contains("Rahul"));
        assertTrue(suspicious.contains("Priya"));
    }

    @Test
    void condition8_anagramWithinSameLender() {
        LendingAnalyticsService engine = engineFrom(
                "A1|RAM KUMAR|HDFC|Personal Loan|100000|800",
                "A2|KUMAR RAM|HDFC|Home Loan|200000|750");
        List<String> suspicious = engine.suspiciousApplications();
        assertTrue(suspicious.contains("RAM KUMAR"));
        assertTrue(suspicious.contains("KUMAR RAM"));
    }

    @Test
    void condition8_multipleAnagramGroups() { // Hidden Test 7
        LendingAnalyticsService engine = engineFrom(
                "A1|RAM KUMAR|HDFC|Personal Loan|100000|800",
                "A2|KUMAR RAM|HDFC|Home Loan|200000|750",
                "A3|ARUM RAMK|HDFC|Car Loan|300000|700");
        List<String> suspicious = engine.suspiciousApplications();
        assertTrue(suspicious.contains("RAM KUMAR"));
        assertTrue(suspicious.contains("KUMAR RAM"));
        assertTrue(suspicious.contains("ARUM RAMK"));
    }

    @Test
    void condition8_anagramAcrossDifferentLenders_isNotSuspicious() {
        LendingAnalyticsService engine = engineFrom(
                "A1|RAM KUMAR|HDFC|Personal Loan|123456|811",
                "A2|KUMAR RAM|ICICI|Home Loan|234567|733");
        List<String> suspicious = engine.suspiciousApplications();
        assertFalse(suspicious.contains("RAM KUMAR"));
        assertFalse(suspicious.contains("KUMAR RAM"));
    }

    // ------------------------------------------------------ suspicious: output

    @Test
    void suspicious_outputIsDistinctAndAlphabeticallySorted() {
        LendingAnalyticsService engine = engineFrom(
                "A1|Zeta Zeta Name|HDFC|Personal Loan|100000|800",
                "A2|Alpha Alpha Name|ICICI|Home Loan|200000|790",
                "A3|Clean One Here|Axis|Car Loan|300000|780");

        List<String> suspicious = engine.suspiciousApplications();
        assertEquals(List.of("Alpha Alpha Name", "Zeta Zeta Name"), suspicious);
    }

    @Test
    void suspicious_emptyEngine_returnsEmptyList() {
        assertTrue(new LendingAnalyticsService(new LoanApplicationRepository())
                .suspiciousApplications().isEmpty());
    }

    // ----------------------------------------------------------- Hidden tests

    @Test
    void hiddenTest4_leadingAndTrailingSpacesAreTrimmed() {
        LendingAnalyticsService engine = engineFrom(
                "A1| Rahul Sharma |HDFC|Personal Loan|500000|800");
        assertEquals("Rahul Sharma",
                engine.topCreditProfiles(1).get(0).getCustomerName());
    }

    @Test
    void hiddenTest5_unicodeNamesAreSupported() {
        LendingAnalyticsService engine = engineFrom(
                "A1|\u0bb0\u0bbe\u0b95\u0bc1\u0bb2\u0bcd \u0b95\u0bc1\u0bae\u0bbe\u0bb0\u0bcd|HDFC|Personal Loan|100000|800",
                "A2|\u0905\u092e\u093f\u0924 \u0915\u0941\u092e\u093e\u0930|ICICI|Home Loan|200000|820");
        assertEquals(2, engine.topCreditProfiles(10).size());
    }

    @Test
    void hiddenTest6_typeAverageDoesNotDivideByZero() {
        // Single record: its own amount equals the type average, so 250% test
        // (amount > 2.5 * average) must be false and must not throw.
        LendingAnalyticsService engine = engineFrom(
                "A1|Solo Saver|HDFC|Personal Loan|100000|800");
        assertFalse(engine.suspiciousApplications().contains("Solo Saver"));
    }

    // ------------------------------------------------------------------ Bonus

    @Test
    void bonus_loanTypeWiseTopApplicantByLender() {
        LendingAnalyticsService engine = engineFrom(
                "A1|Rahul Sharma|HDFC|Personal Loan|500000|780",
                "A2|Priya Verma|ICICI|Home Loan|4500000|820",
                "A3|Amit Singh|Axis Bank|Car Loan|900000|760",
                "A4|Low Score|HDFC|Personal Loan|500000|700");

        Map<String, Map<String, Optional<LoanApplication>>> report =
                engine.loanTypeWiseTopApplicantByLender();

        assertEquals("Rahul Sharma",
                report.get("Personal Loan").get("HDFC").orElseThrow().getCustomerName());
        assertEquals("Priya Verma",
                report.get("Home Loan").get("ICICI").orElseThrow().getCustomerName());
        assertEquals("Amit Singh",
                report.get("Car Loan").get("Axis Bank").orElseThrow().getCustomerName());
    }

    // -------------------------------------------------- structured create (DTO)

    @Test
    void addApplication_storesValidApplication() {
        LendingAnalyticsService engine = new LendingAnalyticsService(new LoanApplicationRepository());
        engine.addApplication(new LoanApplication("A1", "Rahul", "HDFC", "Personal Loan", 500000, 800));
        assertEquals(1, engine.count());
        assertEquals("Rahul", engine.findById("A1").orElseThrow().getCustomerName());
    }

    @Test
    void addApplication_rejectsInvalidCreditScore() {
        LendingAnalyticsService engine = new LendingAnalyticsService(new LoanApplicationRepository());
        try {
            engine.addApplication(new LoanApplication("A1", "Rahul", "HDFC", "Personal Loan", 500000, 1000));
            assertTrue(false, "Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertEquals(0, engine.count());
        }
    }
}
