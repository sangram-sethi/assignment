package com.classroom.products.controller;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroom.products.dto.CurrentUserDTO;
import com.classroom.products.dto.LoginRequestDTO;
import com.classroom.products.dto.LoginResponseDTO;
import com.classroom.products.security.JwtAuthenticationFilter;
import com.classroom.products.security.JwtService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

        private final AuthenticationManager authenticationManager;
        private final JwtService jwtService;
        private final long expirationMs;
        private final boolean cookieSecure;

        public AuthController(AuthenticationManager authenticationManager,
                        JwtService jwtService,
                        @Value("${app.jwt.expiration-ms}") long expirationMs,
                        @Value("${app.jwt.cookie.secure:false}") boolean cookieSecure) {
                this.authenticationManager = authenticationManager;
                this.jwtService = jwtService;
                this.expirationMs = expirationMs;
                this.cookieSecure = cookieSecure;
        }

        @PostMapping("/login")
        public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String token = jwtService.generateToken(userDetails);
                List<String> roles = authentication.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .toList();

                // The token is delivered to the browser as an httpOnly cookie (invisible
                // to JavaScript). It is also returned in the body so non-browser clients
                // (Postman, tests) can use the Authorization: Bearer header.
                ResponseCookie cookie = buildTokenCookie(token, Duration.ofMillis(expirationMs));

                LoginResponseDTO body = new LoginResponseDTO("Bearer", token, expirationMs,
                                userDetails.getUsername(), roles);

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                                .body(body);
        }

        @GetMapping("/me")
        public ResponseEntity<CurrentUserDTO> me(Authentication authentication) {
                List<String> roles = authentication.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .toList();
                return ResponseEntity.ok(new CurrentUserDTO(authentication.getName(), roles));
        }

        @PostMapping("/logout")
        public ResponseEntity<Void> logout() {
                // Overwrite the cookie with an immediately-expiring one to clear it.
                ResponseCookie cookie = buildTokenCookie("", Duration.ZERO);
                return ResponseEntity.noContent()
                                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                                .build();
        }

        private ResponseCookie buildTokenCookie(String value, Duration maxAge) {
                return ResponseCookie.from(JwtAuthenticationFilter.ACCESS_TOKEN_COOKIE, value)
                                .httpOnly(true)
                                .secure(cookieSecure)
                                .sameSite("Strict")
                                .path("/")
                                .maxAge(maxAge)
                                .build();
        }
}
