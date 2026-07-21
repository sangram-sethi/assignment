package com.classroom.nbc.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility for computing Equated Monthly Installment (EMI) details of a loan.
 *
 * <p>EMI = [P * r * (1 + r)^n] / [(1 + r)^n - 1]
 * where P = principal, r = monthly interest rate, n = tenure in months.
 */
public final class EmiCalculator {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal MONTHS_IN_YEAR = BigDecimal.valueOf(12);
    private static final int CALC_SCALE = 12;
    private static final int MONEY_SCALE = 2;

    private EmiCalculator() {
    }

    public static EmiResult calculate(BigDecimal principal, BigDecimal annualInterestRate, int tenureMonths) {
        BigDecimal monthlyRate = annualInterestRate
                .divide(HUNDRED, CALC_SCALE, RoundingMode.HALF_UP)
                .divide(MONTHS_IN_YEAR, CALC_SCALE, RoundingMode.HALF_UP);

        BigDecimal monthlyEmi;
        if (monthlyRate.signum() == 0) {
            monthlyEmi = principal.divide(
                    BigDecimal.valueOf(tenureMonths), MONEY_SCALE, RoundingMode.HALF_UP);
        } else {
            BigDecimal onePlusRatePowN = BigDecimal.ONE.add(monthlyRate).pow(tenureMonths);
            monthlyEmi = principal.multiply(monthlyRate).multiply(onePlusRatePowN)
                    .divide(onePlusRatePowN.subtract(BigDecimal.ONE), MONEY_SCALE, RoundingMode.HALF_UP);
        }

        BigDecimal totalPayable = monthlyEmi.multiply(BigDecimal.valueOf(tenureMonths))
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal totalInterest = totalPayable.subtract(principal)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        return new EmiResult(monthlyEmi, totalPayable, totalInterest);
    }

    public record EmiResult(BigDecimal monthlyEmi, BigDecimal totalPayable, BigDecimal totalInterest) {
    }
}
