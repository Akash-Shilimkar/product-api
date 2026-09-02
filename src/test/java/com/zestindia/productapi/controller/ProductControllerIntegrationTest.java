package com.zestindia.productapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zestindia.productapi.dto.AuthRequest;
import com.zestindia.productapi.dto.ProductRequest;
import com.zestindia.productapi.entity.User;
import com.zestindia.productapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.findByUsername("admin").orElseGet(() -> userRepository.save(
                User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("Admin@123"))
                        .role(User.Role.ROLE_ADMIN)
                        .build()));
    }

    private String obtainAccessToken() throws Exception {
        AuthRequest request = new AuthRequest("admin", "Admin@123");

        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("accessToken").asText();
    }

    @Test
    void unauthenticatedRequest_isRejected() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isForbidden());
    }

    @Test
    void fullCrudLifecycle_worksEndToEnd() throws Exception {
        String token = obtainAccessToken();

        // CREATE
        ProductRequest createRequest = new ProductRequest("Mechanical Keyboard");
        String createResponse = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("Mechanical Keyboard"))
                .andReturn().getResponse().getContentAsString();

        int id = objectMapper.readTree(createResponse).get("id").asInt();

        // READ (single)
        mockMvc.perform(get("/api/v1/products/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Mechanical Keyboard"));

        // READ (paginated list)
        mockMvc.perform(get("/api/v1/products?page=0&size=10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        // UPDATE
        ProductRequest updateRequest = new ProductRequest("Mechanical Keyboard RGB");
        mockMvc.perform(put("/api/v1/products/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Mechanical Keyboard RGB"));

        // DELETE
        mockMvc.perform(delete("/api/v1/products/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // Verify gone
        mockMvc.perform(get("/api/v1/products/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProduct_withBlankName_returnsBadRequest() throws Exception {
        String token = obtainAccessToken();

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new ProductRequest(""))))
                .andExpect(status().isBadRequest());
    }
}
