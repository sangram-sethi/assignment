package com.assignment.socialmedia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request/response carrier for a
 * {@link com.assignment.socialmedia.entity.Comment}. A comment references both
 * its author ({@code userId}) and the {@code postId} it belongs to.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDTO {

    private Long id;

    @NotBlank(message = "Comment text is required")
    @Size(min = 2, message = "Comment must be at least 2 characters")
    private String text;

    private LocalDateTime createdAt;

    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "postId is required")
    private Long postId;

    private String authorUsername;
}
