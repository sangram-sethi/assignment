package com.assignment.nbfc.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.assignment.nbfc.repository.LoanApplicationRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

/** End-to-end tests for the lending REST API. */
@SpringBootTest
class LendingControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private LoanApplicationRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void createApplication_returnsCreated() throws Exception {
        String body = """
                {
                  "applicationId": "A1",
                  "customerName": "Rahul Sharma",
                  "lenderName": "HDFC",
                  "loanType": "Personal Loan",
                  "loanAmount": 500000,
                  "creditScore": 800
                }""";

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.applicationId").value("A1"))
                .andExpect(jsonPath("$.customerName").value("Rahul Sharma"));
    }

    @Test
    void createApplication_invalidScore_returnsBadRequest() throws Exception {
        String body = """
                {
                  "applicationId": "A1",
                  "customerName": "Rahul",
                  "lenderName": "HDFC",
                  "loanType": "Personal Loan",
                  "loanAmount": 500000,
                  "creditScore": 1000
                }""";

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void bulkLoad_storesValidRecordsOnly() throws Exception {
        List<String> records = List.of(
                "A1|Rahul|HDFC|Personal Loan|500000|800",
                "A2|Priya|ICICI|Home Loan|700000|820",
                "BAD|||||",
                "A3|Amit|Axis|Car Loan|0|750"); // amount <= 0, ignored

        mockMvc.perform(post("/api/loans/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(records)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.received").value(4))
                .andExpect(jsonPath("$.stored").value(2))
                .andExpect(jsonPath("$.added").value(2));
    }

    @Test
    void findById_missing_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/loans/UNKNOWN"))
                .andExpect(status().isNotFound());
    }

    @Test
    void topCreditProfiles_returnsOrderedResults() throws Exception {
        seed("A1|Charlie|HDFC|Personal Loan|500000|800",
                "A2|Bravo|ICICI|Home Loan|300000|800",
                "A3|Alpha|Axis|Car Loan|300000|800");

        mockMvc.perform(get("/api/loans/analytics/top-credit-profiles").param("n", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].customerName").value("Alpha"))
                .andExpect(jsonPath("$[1].customerName").value("Bravo"));
    }

    @Test
    void suspiciousApplications_flagsRepeatedWords() throws Exception {
        seed("A1|Rahul Rahul Sharma|HDFC|Personal Loan|100000|800",
                "A2|Clean Name|ICICI|Home Loan|200000|790");

        mockMvc.perform(get("/api/loans/analytics/suspicious"))
                .andExpect(status().isOk())
                .andExpect(content().json("[\"Rahul Rahul Sharma\"]"));
    }

    @Test
    void deleteById_removesApplication() throws Exception {
        seed("A1|Rahul|HDFC|Personal Loan|500000|800");

        mockMvc.perform(delete("/api/loans/A1")).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/loans/A1")).andExpect(status().isNotFound());
    }

    private void seed(String... records) throws Exception {
        mockMvc.perform(post("/api/loans/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(records))))
                .andExpect(status().isOk());
    }
}
