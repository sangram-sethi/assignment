package com.assignment.socialmedia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Carrier for {@link com.assignment.socialmedia.entity.Profile} data, used both
 * nested inside a user payload and on the standalone profile endpoints.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileDTO {

    private Long id;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String bio;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$",
            message = "Phone number must be 10-15 digits, optionally prefixed with '+'")
    private String phoneNumber;
}
