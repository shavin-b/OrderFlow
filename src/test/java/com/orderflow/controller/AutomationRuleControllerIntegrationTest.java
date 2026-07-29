package com.orderflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderflow.dto.automation.AutomationRuleDto;
import com.orderflow.dto.automation.KeywordDto;
import com.orderflow.dto.automation.ReplyDto;
import com.orderflow.entity.Keyword.MatchType;
import com.orderflow.repository.SubscriptionRepository;
import com.orderflow.repository.UserRepository;
import com.orderflow.security.ApiKeyAuthFilter;
import com.orderflow.security.JwtAuthenticationFilter;
import com.orderflow.security.JwtProvider;
import com.orderflow.security.SecurityConfig;
import com.orderflow.service.AutomationRuleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AutomationRuleController.class)
@Import({SecurityConfig.class, ApiKeyAuthFilter.class, JwtAuthenticationFilter.class})
@TestPropertySource(properties = {
    "security.api-keys=dev-api-key-change-in-production",
    "whatsapp.api.verify-token=test_verify_token",
    "whatsapp.api.app-secret=test_app_secret",
    "whatsapp.api.phone-number-id=test_phone_id",
    "whatsapp.api.access-token=test_token",
    "whatsapp.api.base-url=https://graph.facebook.com/v20.0"
})
@ActiveProfiles("test")
class AutomationRuleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AutomationRuleService automationRuleService;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private SubscriptionRepository subscriptionRepository;

    @Test
    @DisplayName("POST /automation/rules without API key should return 401 Unauthorized")
    void testUnauthorizedWithoutApiKey() throws Exception {
        AutomationRuleDto dto = AutomationRuleDto.builder()
                .name("Order Track Rule")
                .keywords(List.of(KeywordDto.builder().pattern("track").matchType(MatchType.CONTAINS).build()))
                .replies(List.of(ReplyDto.builder().messageBody("Tracking url: ...").build()))
                .build();

        mockMvc.perform(post("/automation/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /automation/rules with valid API key header should create rule and return 201 Created")
    void testCreateRuleWithApiKey() throws Exception {
        AutomationRuleDto dto = AutomationRuleDto.builder()
                .name("Order Track Rule")
                .priority(5)
                .keywords(List.of(KeywordDto.builder().pattern("track").matchType(MatchType.CONTAINS).build()))
                .replies(List.of(ReplyDto.builder().messageBody("Tracking url: ...").build()))
                .build();

        AutomationRuleDto created = AutomationRuleDto.builder()
                .id(1L)
                .name("Order Track Rule")
                .priority(5)
                .build();

        when(automationRuleService.create(any())).thenReturn(created);

        mockMvc.perform(post("/automation/rules")
                .header("X-API-Key", "dev-api-key-change-in-production")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Order Track Rule"));
    }
}
