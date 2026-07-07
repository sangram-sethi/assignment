package org.northernarc.assessment4.dto;

/**
 * Aggregated analytics for the platform dashboard.
 */
public record DashboardResponse(
        long totalCustomers,
        long totalAccounts,
        double totalBalance,
        String topBranch,
        String highestBalanceCustomer
) {
}
