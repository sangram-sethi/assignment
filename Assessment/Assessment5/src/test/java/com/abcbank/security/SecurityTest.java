package com.abcbank.security;

import com.abcbank.dto.LoginRequest;
import com.abcbank.dto.RegisterRequest;
import com.abcbank.entity.Customer;
import com.abcbank.repository.CustomerRepository;
import com.abcbank.testsupport.TestFixtures;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Executable specification for the JWT security layer, verifying which
 * endpoints are public and that missing / invalid / expired tokens are all
 * rejected with 401 while a valid token is accepted.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Security (JWT) specification")
class SecurityTest {

    private static final String SECURED_USER_EMAIL = "secure.user@abcbank.com";
    private static final String PROTECTED_ENDPOINT = "/api/customers";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private JwtService jwtService;

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        customerRepository.save(new Customer(null, "Secure User", SECURED_USER_EMAIL,
                TestFixtures.VALID_PHONE, TestFixtures.ENCODED_PASSWORD));
    }

    @Test
    @DisplayName("permits POST /api/auth/register without authentication")
    void registerEndpoint_isPublic() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest("New User", "new.user@abcbank.com",
                TestFixtures.VALID_PHONE, TestFixtures.RAW_PASSWORD);

        // Act
        int status = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getStatus();

        // Assert - the request is not blocked by authentication
        assertThat(status).isNotEqualTo(401);
    }

    @Test
    @DisplayName("permits POST /api/auth/login without authentication")
    void loginEndpoint_isPublic() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest(SECURED_USER_EMAIL, TestFixtures.RAW_PASSWORD);

        // Act
        int status = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getStatus();

        // Assert
        assertThat(status).isNotEqualTo(401);
    }

    @Test
    @DisplayName("returns 401 Unauthorized when the JWT is missing")
    void protectedEndpoint_withoutToken_returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(get(PROTECTED_ENDPOINT))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("returns 401 Unauthorized when the JWT is invalid")
    void protectedEndpoint_withInvalidToken_returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(get(PROTECTED_ENDPOINT)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer this.is.not.a.valid.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("returns 401 Unauthorized when the JWT has expired")
    void protectedEndpoint_withExpiredToken_returns401() throws Exception {
        // Arrange
        String expiredToken = buildExpiredToken(SECURED_USER_EMAIL);

        // Act & Assert
        mockMvc.perform(get(PROTECTED_ENDPOINT)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("accepts a valid JWT and lets the request through the security filter")
    void protectedEndpoint_withValidToken_isAuthenticated() throws Exception {
        // Arrange
        String validToken = jwtService.generateToken(SECURED_USER_EMAIL);

        // Act
        int status = mockMvc.perform(get(PROTECTED_ENDPOINT)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andReturn().getResponse().getStatus();

        // Assert - authentication succeeded, so it is neither 401 nor 403
        assertThat(status).isNotEqualTo(401);
        assertThat(status).isNotEqualTo(403);
    }

    private String buildExpiredToken(String subject) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date(now - 7_200_000))
                .expiration(new Date(now - 3_600_000))
                .signWith(key)
                .compact();
    }
}
