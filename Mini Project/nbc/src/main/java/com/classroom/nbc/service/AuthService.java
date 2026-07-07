package com.classroom.nbc.service;

import com.classroom.nbc.dto.request.LoginRequest;
import com.classroom.nbc.dto.request.RegisterCustomerRequest;
import com.classroom.nbc.dto.response.CustomerResponse;
import com.classroom.nbc.dto.response.JwtResponse;

public interface AuthService {

    CustomerResponse registerCustomer(RegisterCustomerRequest request);

    JwtResponse login(LoginRequest request);
}
