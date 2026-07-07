package org.northernarc.assessment4.dto;

/**
 * Client-facing projection of a {@link org.northernarc.assessment4.model.Customer}
 * that never exposes the underlying entity or sensitive fields.
 */
public record CustomerSummaryDTO(
        String customerName,
        String branch,
        long numberOfAccounts,
        double totalBalance
) {
}
