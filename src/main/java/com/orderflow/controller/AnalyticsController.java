package com.orderflow.controller;

import com.orderflow.dto.ApiResponse;
import com.orderflow.dto.analytics.AnalyticsSummaryDto;
import com.orderflow.dto.analytics.MonthlyStatDto;
import com.orderflow.dto.analytics.ReportRequestDto;
import com.orderflow.entity.Report;
import com.orderflow.repository.ReportRepository;
import com.orderflow.service.AnalyticsService;
import com.orderflow.service.ReportExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Analytics & Reports", description = "Enterprise Analytics metrics, date filtering, and export APIs")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final ReportExportService reportExportService;
    private final ReportRepository reportRepository;

    @GetMapping("/summary")
    @Operation(summary = "Get aggregated analytics summary with date range filtering")
    public ResponseEntity<ApiResponse<AnalyticsSummaryDto>> getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Fetching analytics summary from {} to {}", startDate, endDate);
        AnalyticsSummaryDto summary = analyticsService.getSummary(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/monthly")
    @Operation(summary = "Get monthly statistics trend")
    public ResponseEntity<ApiResponse<List<MonthlyStatDto>>> getMonthlyStats() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getMonthlyStatistics()));
    }

    @GetMapping("/reports")
    @Operation(summary = "Get list of generated reports")
    public ResponseEntity<ApiResponse<List<Report>>> getReports() {
        return ResponseEntity.ok(ApiResponse.success(reportRepository.findAllByOrderByCreatedAtDesc()));
    }

    @PostMapping("/reports/generate")
    @Operation(summary = "Generate custom export report (CSV, EXCEL, or PDF)")
    public ResponseEntity<ApiResponse<String>> generateReport(@Valid @RequestBody ReportRequestDto dto) {
        String downloadPath = "/analytics/reports/export/" + dto.getReportType().name().toLowerCase() +
                "?startDate=" + dto.getStartDate() + "&endDate=" + dto.getEndDate();
        return ResponseEntity.ok(ApiResponse.success("Report generated successfully", downloadPath));
    }

    @GetMapping("/reports/export/csv")
    @Operation(summary = "Export analytics data as CSV file")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDate start = (startDate != null) ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();

        byte[] csvData = reportExportService.generateCsvReport(start, end);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=orderflow-analytics-" + start + "-to-" + end + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }

    @GetMapping("/reports/export/excel")
    @Operation(summary = "Export analytics data as Excel Spreadsheet file")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDate start = (startDate != null) ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();

        byte[] excelData = reportExportService.generateExcelReport(start, end);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=orderflow-analytics-" + start + "-to-" + end + ".xml")
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(excelData);
    }

    @GetMapping("/reports/export/pdf")
    @Operation(summary = "Export executive analytics report as PDF document")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDate start = (startDate != null) ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();

        byte[] pdfData = reportExportService.generatePdfReport(start, end);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=orderflow-analytics-" + start + "-to-" + end + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfData);
    }
}
