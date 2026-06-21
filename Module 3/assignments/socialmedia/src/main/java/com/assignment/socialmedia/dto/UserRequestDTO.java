package com.assignment.socialmedia.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Inbound payload used to create or update a user. The password is accepted on
 * the way in but never echoed back (see {@link UserResponseDTO}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequestDTO {

    @NotBlank(message = "Username is required")
    @Size(min = 4, message = "Username must be at least 4 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a well-formed address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    /** Optional profile created together with the user. */
    @Valid
    private ProfileDTO profile;

    /** Optional set of role ids to grant to the user. */
    private Set<Long> roleIds;
}
