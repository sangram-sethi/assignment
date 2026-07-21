package com.classroom.products.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponseDTO {

    private String tokenType;

    private String accessToken;

    private long expiresInMs;

    private String username;

    private List<String> roles;

}
