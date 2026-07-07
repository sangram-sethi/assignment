package org.northernarc.assessment4.dto;

import org.northernarc.assessment4.model.Account;

/**
 * Outbound representation of an account. Excludes the owning customer graph and
 * transaction history to keep the API contract stable and lean.
 */
public record AccountResponse(
        String accountNumber,
        String accountType,
        Double balance
) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance());
    }
}
