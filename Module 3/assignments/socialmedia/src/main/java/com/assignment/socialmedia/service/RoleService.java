package com.assignment.socialmedia.service;

import com.assignment.socialmedia.dto.RoleDTO;
import java.util.List;

public interface RoleService {

    RoleDTO createRole(RoleDTO roleDTO);

    List<RoleDTO> getAllRoles();
}
