package com.assignment.socialmedia.serviceimpl;

import com.assignment.socialmedia.dto.CommentDTO;
import com.assignment.socialmedia.dto.PostDTO;
import com.assignment.socialmedia.entity.Post;
import com.assignment.socialmedia.entity.User;
import com.assignment.socialmedia.exception.ResourceNotFoundException;
import com.assignment.socialmedia.repository.PostRepository;
import com.assignment.socialmedia.repository.UserRepository;
import com.assignment.socialmedia.service.PostService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    public PostDTO createPost(PostDTO dto) {
        User author = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getUserId()));

        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setUser(author);

        return toDto(postRepository.save(post));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostDTO> getAllPosts() {
        return postRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PostDTO getPostById(Long id) {
        return toDto(findPostOrThrow(id));
    }

    @Override
    public PostDTO updatePost(Long id, PostDTO dto) {
        Post post = findPostOrThrow(id);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());

        if (dto.getUserId() != null && !dto.getUserId().equals(post.getUser().getId())) {
            User author = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getUserId()));
            post.setUser(author);
        }

        return toDto(postRepository.save(post));
    }

    @Override
    public void deletePost(Long id) {
        postRepository.delete(findPostOrThrow(id));
    }

    private Post findPostOrThrow(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
    }

    private PostDTO toDto(Post post) {
        List<CommentDTO> commentDTOs = post.getComments() == null ? List.of()
                : post.getComments().stream()
                        .map(comment -> CommentDTO.builder()
                                .id(comment.getId())
                                .text(comment.getText())
                                .createdAt(comment.getCreatedAt())
                                .userId(comment.getUser() != null ? comment.getUser().getId() : null)
                                .postId(post.getId())
                                .authorUsername(comment.getUser() != null ? comment.getUser().getUsername() : null)
                                .build())
                        .toList();

        return PostDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .userId(post.getUser() != null ? post.getUser().getId() : null)
                .authorUsername(post.getUser() != null ? post.getUser().getUsername() : null)
                .comments(commentDTOs)
                .build();
    }
}
