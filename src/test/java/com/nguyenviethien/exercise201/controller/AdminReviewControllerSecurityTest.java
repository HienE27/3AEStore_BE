package com.nguyenviethien.exercise201.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminReviewControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void adminEndpoint_requiresAuthentication() throws Exception {
        // call protected admin endpoint without auth -> expect 401/403
        mockMvc.perform(get("/api/admin/reviews"))
                .andExpect(status().is4xxClientError());
    }
}


