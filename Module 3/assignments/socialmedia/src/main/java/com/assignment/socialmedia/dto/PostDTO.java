package com.assignment.socialmedia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request/response carrier for a {@link com.assignment.socialmedia.entity.Post}.
 * {@code userId} identifies the author on the way in; the read-only fields
 * ({@code id}, {@code createdAt}, {@code authorUsername}, {@code comments}) are
 * populated on the way out.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDTO {

    private Long id;

    @NotBlank(message = "Title is required")
    @Size(min = 5, message = "Title must be at least 5 characters")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    private LocalDateTime createdAt;

    @NotNull(message = "userId is required")
    private Long userId;

    private String authorUsername;

    private List<CommentDTO> comments;
}
