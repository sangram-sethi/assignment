package com.assignment.socialmedia.config;

import com.assignment.socialmedia.entity.Role;
import com.assignment.socialmedia.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Seeds the baseline {@code USER} and {@code ADMIN} roles on start-up so the
 * many-to-many association has something to reference out of the box.
 */
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;

    @Bean
    public CommandLineRunner seedRoles() {
        return args -> {
            createRoleIfMissing("USER");
            createRoleIfMissing("ADMIN");
        };
    }

    private void createRoleIfMissing(String roleName) {
        if (!roleRepository.existsByRoleName(roleName)) {
            Role role = new Role();
            role.setRoleName(roleName);
            roleRepository.save(role);
        }
    }
}
