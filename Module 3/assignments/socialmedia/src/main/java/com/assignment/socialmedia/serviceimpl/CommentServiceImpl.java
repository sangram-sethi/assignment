package com.assignment.socialmedia.serviceimpl;

import com.assignment.socialmedia.dto.CommentDTO;
import com.assignment.socialmedia.entity.Comment;
import com.assignment.socialmedia.entity.Post;
import com.assignment.socialmedia.entity.User;
import com.assignment.socialmedia.exception.ResourceNotFoundException;
import com.assignment.socialmedia.repository.CommentRepository;
import com.assignment.socialmedia.repository.PostRepository;
import com.assignment.socialmedia.repository.UserRepository;
import com.assignment.socialmedia.service.CommentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Override
    public CommentDTO createComment(CommentDTO dto) {
        User author = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getUserId()));
        Post post = postRepository.findById(dto.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + dto.getPostId()));

        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setUser(author);
        comment.setPost(post);

        return toDto(commentRepository.save(comment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDTO> getAllComments() {
        return commentRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDTO getCommentById(Long id) {
        return toDto(findCommentOrThrow(id));
    }

    @Override
    public void deleteComment(Long id) {
        commentRepository.delete(findCommentOrThrow(id));
    }

    private Comment findCommentOrThrow(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));
    }

    private CommentDTO toDto(Comment comment) {
        return CommentDTO.builder()
                .id(comment.getId())
                .text(comment.getText())
                .createdAt(comment.getCreatedAt())
                .userId(comment.getUser() != null ? comment.getUser().getId() : null)
                .postId(comment.getPost() != null ? comment.getPost().getId() : null)
                .authorUsername(comment.getUser() != null ? comment.getUser().getUsername() : null)
                .build();
    }
}
