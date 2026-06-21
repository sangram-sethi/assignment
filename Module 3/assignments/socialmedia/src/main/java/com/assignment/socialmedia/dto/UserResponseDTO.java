package com.assignment.socialmedia.dto;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Outbound representation of a user. Deliberately omits the password and breaks
 * the entity's bidirectional relationships, exposing only flat, safe data.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    private ProfileDTO profile;
    private Set<RoleDTO> roles;
    private int postCount;
    private int commentCount;
}
