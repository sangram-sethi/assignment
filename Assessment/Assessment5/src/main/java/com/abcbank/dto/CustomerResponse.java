package com.abcbank.dto;

/**
 * Response view of a customer. The password is deliberately never exposed.
 */
public record CustomerResponse(
        Long id,
        String name,
        String email,
        String phone
) {
}
