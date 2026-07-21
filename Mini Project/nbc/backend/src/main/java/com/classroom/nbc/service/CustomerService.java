package com.classroom.nbc.service;

import com.classroom.nbc.dto.response.CustomerResponse;
import com.classroom.nbc.dto.response.LoanSummaryResponse;

public interface CustomerService {

    CustomerResponse getProfile(Long customerId);

    LoanSummaryResponse getLoanSummary(Long customerId);
}
