package com.assignment.socialmedia.serviceimpl;

import com.assignment.socialmedia.dto.ProfileDTO;
import com.assignment.socialmedia.dto.RoleDTO;
import com.assignment.socialmedia.dto.UserRequestDTO;
import com.assignment.socialmedia.dto.UserResponseDTO;
import com.assignment.socialmedia.entity.Profile;
import com.assignment.socialmedia.entity.Role;
import com.assignment.socialmedia.entity.User;
import com.assignment.socialmedia.exception.DuplicateResourceException;
import com.assignment.socialmedia.exception.ResourceNotFoundException;
import com.assignment.socialmedia.repository.RoleRepository;
import com.assignment.socialmedia.repository.UserRepository;
import com.assignment.socialmedia.service.UserService;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserResponseDTO createUser(UserRequestDTO request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        if (request.getProfile() != null) {
            Profile profile = new Profile();
            applyProfile(profile, request.getProfile());
            profile.setUser(user);
            user.setProfile(profile);
        }

        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            user.setRoles(resolveRoles(request.getRoleIds()));
        }

        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        return toResponse(findUserOrThrow(id));
    }

    @Override
    public UserResponseDTO updateUser(Long id, UserRequestDTO request) {
        User user = findUserOrThrow(id);

        if (!user.getUsername().equals(request.getUsername())
                && userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + request.getUsername());
        }
        if (!user.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        if (request.getProfile() != null) {
            Profile profile = user.getProfile();
            if (profile == null) {
                profile = new Profile();
                profile.setUser(user);
                user.setProfile(profile);
            }
            applyProfile(profile, request.getProfile());
        }

        if (request.getRoleIds() != null) {
            user.setRoles(resolveRoles(request.getRoleIds()));
        }

        return toResponse(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        User user = findUserOrThrow(id);
        userRepository.delete(user);
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private Set<Role> resolveRoles(Set<Long> roleIds) {
        return roleIds.stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId)))
                .collect(Collectors.toSet());
    }

    private void applyProfile(Profile profile, ProfileDTO dto) {
        profile.setFirstName(dto.getFirstName());
        profile.setLastName(dto.getLastName());
        profile.setBio(dto.getBio());
        profile.setPhoneNumber(dto.getPhoneNumber());
    }

    private UserResponseDTO toResponse(User user) {
        ProfileDTO profileDTO = null;
        Profile profile = user.getProfile();
        if (profile != null) {
            profileDTO = ProfileDTO.builder()
                    .id(profile.getId())
                    .firstName(profile.getFirstName())
                    .lastName(profile.getLastName())
                    .bio(profile.getBio())
                    .phoneNumber(profile.getPhoneNumber())
                    .build();
        }

        Set<RoleDTO> roleDTOs = user.getRoles().stream()
                .map(role -> RoleDTO.builder()
                        .id(role.getId())
                        .roleName(role.getRoleName())
                        .build())
                .collect(Collectors.toSet());

        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .profile(profileDTO)
                .roles(roleDTOs)
                .postCount(user.getPosts() != null ? user.getPosts().size() : 0)
                .commentCount(user.getComments() != null ? user.getComments().size() : 0)
                .build();
    }
}
