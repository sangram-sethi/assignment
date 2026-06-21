package com.assignment.socialmedia.service;

import com.assignment.socialmedia.dto.CommentDTO;
import java.util.List;

public interface CommentService {

    CommentDTO createComment(CommentDTO commentDTO);

    List<CommentDTO> getAllComments();

    CommentDTO getCommentById(Long id);

    void deleteComment(Long id);
}
