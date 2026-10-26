package com.bus.booking.security;

import com.bus.booking.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("1. Truy cập Endpoint được bảo vệ không có JWT -> Trả về 401 Unauthorized dạng JSON")
    void testProtectedEndpoint_WithoutToken_Returns401Json() throws Exception {
        mockMvc.perform(post("/api/v1/tickets/book")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message", containsString("Token")));
    }

    @Test
    @WithMockUser(username = "customer1", roles = {"CUSTOMER"})
    @DisplayName("2. Role CUSTOMER truy cập Endpoint của ADMIN -> Trả về 403 Forbidden dạng JSON")
    void testAdminEndpoint_WithCustomerRole_Returns403Json() throws Exception {
        mockMvc.perform(get("/api/v1/admin/overview"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message", containsString("không có quyền truy cập")));
    }

    @Test
    @WithMockUser(username = "customer1", roles = {"CUSTOMER"})
    @DisplayName("3. Role CUSTOMER truy cập Endpoint của OPERATOR -> Trả về 403 Forbidden dạng JSON")
    void testOperatorEndpoint_WithCustomerRole_Returns403Json() throws Exception {
        mockMvc.perform(get("/api/v1/operator/buses"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }
}
