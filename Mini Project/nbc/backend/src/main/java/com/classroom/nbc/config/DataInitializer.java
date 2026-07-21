package com.classroom.nbc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.classroom.nbc.enums.Role;
import com.classroom.nbc.model.User;
import com.classroom.nbc.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Seeds a default {@link Role#LOAN_APPROVER} account on startup so the loan
 * review features are usable out of the box. Customer accounts are created
 * through the public registration endpoint, but no such endpoint exists for
 * approvers, so one is provisioned here when absent.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${nbc.default-approver.username:approver}")
    private String approverUsername;

    @Value("${nbc.default-approver.password:Approver@Nbc2026}")
    private String approverPassword;

    @Override
    public void run(String... args) {
        User approver = userRepository.findByUsername(approverUsername).orElse(null);

        if (approver == null) {
            approver = User.builder()
                    .username(approverUsername)
                    .password(passwordEncoder.encode(approverPassword))
                    .role(Role.LOAN_APPROVER)
                    .build();
            userRepository.save(approver);
            log.info("Seeded default loan-approver account '{}'", approverUsername);
            return;
        }

        // Keep the seeded approver's password in sync with the configured value.
        if (!passwordEncoder.matches(approverPassword, approver.getPassword())) {
            approver.setPassword(passwordEncoder.encode(approverPassword));
            userRepository.save(approver);
            log.info("Updated password for loan-approver account '{}'", approverUsername);
        }
    }
}
