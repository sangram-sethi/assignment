package com.assignment.socialmedia.service;

import com.assignment.socialmedia.dto.UserRequestDTO;
import com.assignment.socialmedia.dto.UserResponseDTO;
import java.util.List;

public interface UserService {

    UserResponseDTO createUser(UserRequestDTO request);

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO getUserById(Long id);

    UserResponseDTO updateUser(Long id, UserRequestDTO request);

    void deleteUser(Long id);
}
