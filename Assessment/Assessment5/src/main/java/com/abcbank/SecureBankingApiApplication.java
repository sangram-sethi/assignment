package com.abcbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Secure Banking API.
 *
 * <p>NOTE: This project follows a Test-Driven Development (TDD) approach. The
 * production business logic (service implementations and controller handlers)
 * is intentionally left as skeletons. The authoritative specification of the
 * expected behaviour lives in the {@code src/test} package.
 */
@SpringBootApplication
public class SecureBankingApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecureBankingApiApplication.class, args);
    }
}
