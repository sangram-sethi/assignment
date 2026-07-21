package com.classroom.products.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

        private static final String BEARER_PREFIX = "Bearer ";
        public static final String ACCESS_TOKEN_COOKIE = "access_token";

        private final JwtService jwtService;
        private final UserDetailsService userDetailsService;

        public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
                this.jwtService = jwtService;
                this.userDetailsService = userDetailsService;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                        HttpServletResponse response,
                        FilterChain filterChain) throws ServletException, IOException {

                String token = resolveToken(request);
                if (token != null) {
                        if (jwtService.isValid(token)
                                        && SecurityContextHolder.getContext().getAuthentication() == null) {
                                String username = jwtService.extractUsername(token);
                                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                                userDetails, null, userDetails.getAuthorities());
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                }

                filterChain.doFilter(request, response);
        }

        /**
         * Resolves the JWT from the Authorization header (for API/Postman clients)
         * or, failing that, from the httpOnly {@code access_token} cookie used by
         * the browser SPA.
         */
        private String resolveToken(HttpServletRequest request) {
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                        return authHeader.substring(BEARER_PREFIX.length());
                }
                if (request.getCookies() != null) {
                        for (Cookie cookie : request.getCookies()) {
                                if (ACCESS_TOKEN_COOKIE.equals(cookie.getName())) {
                                        return cookie.getValue();
                                }
                        }
                }
                return null;
        }
}
