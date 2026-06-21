package com.assignment.socialmedia.serviceimpl;

import com.assignment.socialmedia.dto.RoleDTO;
import com.assignment.socialmedia.entity.Role;
import com.assignment.socialmedia.exception.DuplicateResourceException;
import com.assignment.socialmedia.repository.RoleRepository;
import com.assignment.socialmedia.service.RoleService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public RoleDTO createRole(RoleDTO dto) {
        if (roleRepository.existsByRoleName(dto.getRoleName())) {
            throw new DuplicateResourceException("Role already exists: " + dto.getRoleName());
        }
        Role role = new Role();
        role.setRoleName(dto.getRoleName());
        return toDto(roleRepository.save(role));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    private RoleDTO toDto(Role role) {
        return RoleDTO.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .build();
    }
}
