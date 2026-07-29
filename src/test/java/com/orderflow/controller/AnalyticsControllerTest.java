package com.orderflow.controller;

import com.orderflow.dto.analytics.AnalyticsSummaryDto;
import com.orderflow.repository.ReportRepository;
import com.orderflow.repository.SubscriptionRepository;
import com.orderflow.repository.UserRepository;
import com.orderflow.security.ApiKeyAuthFilter;
import com.orderflow.security.JwtAuthenticationFilter;
import com.orderflow.security.JwtProvider;
import com.orderflow.security.SecurityConfig;
import com.orderflow.service.AnalyticsService;
import com.orderflow.service.ReportExportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AnalyticsController.class)
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
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private ReportExportService reportExportService;

    @MockBean
    private ReportRepository reportRepository;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private SubscriptionRepository subscriptionRepository;

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("GET /analytics/summary should return 200 OK with analytics metrics")
    void testGetSummary() throws Exception {
        AnalyticsSummaryDto summary = AnalyticsSummaryDto.builder()
                .totalIncomingMessages(1500L)
                .totalOutgoingReplies(1450L)
                .totalFailedReplies(50L)
                .avgResponseTimeMs(142L)
                .activeCustomersCount(8540L)
                .monthlyRevenue(new BigDecimal("12450.00"))
                .successRatePercentage(96.7)
                .topKeywordPattern("order status")
                .dailyBreakdown(List.of())
                .topKeywords(List.of())
                .build();

        when(analyticsService.getSummary(any(), any())).thenReturn(summary);

        mockMvc.perform(get("/analytics/summary")
                .header("X-API-Key", "dev-api-key-change-in-production"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalIncomingMessages").value(1500))
                .andExpect(jsonPath("$.data.totalOutgoingReplies").value(1450))
                .andExpect(jsonPath("$.data.avgResponseTimeMs").value(142));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("GET /analytics/reports/export/csv should return CSV file stream")
    void testExportCsv() throws Exception {
        byte[] csvBytes = "OrderFlow Analytics CSV".getBytes();
        when(reportExportService.generateCsvReport(any(), any())).thenReturn(csvBytes);

        mockMvc.perform(get("/analytics/reports/export/csv")
                .header("X-API-Key", "dev-api-key-change-in-production"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(content().bytes(csvBytes));
    }
}
