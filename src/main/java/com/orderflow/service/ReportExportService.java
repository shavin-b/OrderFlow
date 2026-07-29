package com.orderflow.service;

import com.orderflow.dto.analytics.AnalyticsSummaryDto;
import com.orderflow.dto.analytics.DailyStatDto;
import com.orderflow.entity.Report;
import com.orderflow.entity.Report.ReportStatus;
import com.orderflow.entity.Report.ReportType;
import com.orderflow.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportExportService {

    private final AnalyticsService analyticsService;
    private final ReportRepository reportRepository;

    public byte[] generateCsvReport(LocalDate startDate, LocalDate endDate) {
        AnalyticsSummaryDto summary = analyticsService.getSummary(startDate, endDate);
        StringBuilder csv = new StringBuilder();

        csv.append("OrderFlow Enterprise Analytics Report\n");
        csv.append("Period,").append(startDate).append(" to ").append(endDate).append("\n");
        csv.append("Total Incoming Messages,").append(summary.getTotalIncomingMessages()).append("\n");
        csv.append("Total Outgoing Replies,").append(summary.getTotalOutgoingReplies()).append("\n");
        csv.append("Total Failed Replies,").append(summary.getTotalFailedReplies()).append("\n");
        csv.append("Success Rate (%),").append(summary.getSuccessRatePercentage()).append("\n");
        csv.append("Avg Response Time (ms),").append(summary.getAvgResponseTimeMs()).append("\n");
        csv.append("\n");

        csv.append("Date,Incoming Messages,Outgoing Replies,Failed Replies,Avg Response Time (ms),Top Keyword\n");
        for (DailyStatDto day : summary.getDailyBreakdown()) {
            csv.append(day.getStatDate()).append(",")
               .append(day.getIncomingMessages()).append(",")
               .append(day.getOutgoingReplies()).append(",")
               .append(day.getFailedReplies()).append(",")
               .append(day.getAvgResponseTimeMs()).append(",")
               .append("\"").append(day.getTopKeyword() != null ? day.getTopKeyword() : "").append("\"\n");
        }

        saveReportRecord("CSV Analytics Report", ReportType.CSV, startDate, endDate);
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] generateExcelReport(LocalDate startDate, LocalDate endDate) {
        AnalyticsSummaryDto summary = analyticsService.getSummary(startDate, endDate);
        StringBuilder xml = new StringBuilder();

        xml.append("<?xml version=\"1.0\"?>\n");
        xml.append("<?mso-application progid=\"Excel.Sheet\"?>\n");
        xml.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"\n");
        xml.append(" xmlns:o=\"urn:schemas-microsoft-com:office:office\"\n");
        xml.append(" xmlns:x=\"urn:schemas-microsoft-com:office:excel\"\n");
        xml.append(" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\">\n");

        xml.append(" <Worksheet ss:Name=\"Analytics Summary\">\n");
        xml.append("  <Table>\n");

        xml.append("   <Row><Cell><Data ss:Type=\"String\">OrderFlow Enterprise Analytics</Data></Cell></Row>\n");
        xml.append("   <Row><Cell><Data ss:Type=\"String\">Start Date</Data></Cell><Cell><Data ss:Type=\"String\">")
           .append(startDate).append("</Data></Cell></Row>\n");
        xml.append("   <Row><Cell><Data ss:Type=\"String\">End Date</Data></Cell><Cell><Data ss:Type=\"String\">")
           .append(endDate).append("</Data></Cell></Row>\n");
        xml.append("   <Row><Cell><Data ss:Type=\"String\">Total Incoming Messages</Data></Cell><Cell><Data ss:Type=\"Number\">")
           .append(summary.getTotalIncomingMessages()).append("</Data></Cell></Row>\n");
        xml.append("   <Row><Cell><Data ss:Type=\"String\">Total Outgoing Replies</Data></Cell><Cell><Data ss:Type=\"Number\">")
           .append(summary.getTotalOutgoingReplies()).append("</Data></Cell></Row>\n");
        xml.append("   <Row><Cell><Data ss:Type=\"String\">Total Failed Replies</Data></Cell><Cell><Data ss:Type=\"Number\">")
           .append(summary.getTotalFailedReplies()).append("</Data></Cell></Row>\n");
        xml.append("   <Row><Cell><Data ss:Type=\"String\">Success Rate (%)</Data></Cell><Cell><Data ss:Type=\"Number\">")
           .append(summary.getSuccessRatePercentage()).append("</Data></Cell></Row>\n");
        xml.append("   <Row></Row>\n");

        xml.append("   <Row>\n");
        xml.append("    <Cell><Data ss:Type=\"String\">Date</Data></Cell>\n");
        xml.append("    <Cell><Data ss:Type=\"String\">Incoming Messages</Data></Cell>\n");
        xml.append("    <Cell><Data ss:Type=\"String\">Outgoing Replies</Data></Cell>\n");
        xml.append("    <Cell><Data ss:Type=\"String\">Failed Replies</Data></Cell>\n");
        xml.append("    <Cell><Data ss:Type=\"String\">Avg Response Time (ms)</Data></Cell>\n");
        xml.append("   </Row>\n");

        for (DailyStatDto day : summary.getDailyBreakdown()) {
            xml.append("   <Row>\n");
            xml.append("    <Cell><Data ss:Type=\"String\">").append(day.getStatDate()).append("</Data></Cell>\n");
            xml.append("    <Cell><Data ss:Type=\"Number\">").append(day.getIncomingMessages()).append("</Data></Cell>\n");
            xml.append("    <Cell><Data ss:Type=\"Number\">").append(day.getOutgoingReplies()).append("</Data></Cell>\n");
            xml.append("    <Cell><Data ss:Type=\"Number\">").append(day.getFailedReplies()).append("</Data></Cell>\n");
            xml.append("    <Cell><Data ss:Type=\"Number\">").append(day.getAvgResponseTimeMs()).append("</Data></Cell>\n");
            xml.append("   </Row>\n");
        }

        xml.append("  </Table>\n");
        xml.append(" </Worksheet>\n");
        xml.append("</Workbook>\n");

        saveReportRecord("Excel Analytics Spreadsheet", ReportType.EXCEL, startDate, endDate);
        return xml.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] generatePdfReport(LocalDate startDate, LocalDate endDate) {
        AnalyticsSummaryDto summary = analyticsService.getSummary(startDate, endDate);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StringBuilder pdfContent = new StringBuilder();

        pdfContent.append("%PDF-1.4\n");
        pdfContent.append("1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n");
        pdfContent.append("2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n");
        pdfContent.append("3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >> endobj\n");

        String textStream = String.format(
                "BT /F1 18 Tf 50 740 Td (OrderFlow Enterprise Analytics PDF Report) Tj ET " +
                "BT /F1 12 Tf 50 710 Td (Date Range: %s to %s) Tj ET " +
                "BT /F1 12 Tf 50 680 Td (Total Incoming Messages: %d) Tj ET " +
                "BT /F1 12 Tf 50 660 Td (Total Outgoing Replies: %d) Tj ET " +
                "BT /F1 12 Tf 50 640 Td (Total Failed Replies: %d) Tj ET " +
                "BT /F1 12 Tf 50 620 Td (Delivery Success Rate: %.1f %%) Tj ET " +
                "BT /F1 12 Tf 50 600 Td (Average Response Time: %d ms) Tj ET",
                startDate, endDate, summary.getTotalIncomingMessages(),
                summary.getTotalOutgoingReplies(), summary.getTotalFailedReplies(),
                summary.getSuccessRatePercentage(), summary.getAvgResponseTimeMs()
        );

        pdfContent.append("4 0 obj << /Length ").append(textStream.length()).append(" >> stream\n");
        pdfContent.append(textStream).append("\nendstream\nendobj\n");
        pdfContent.append("5 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n");
        pdfContent.append("xref\n0 6\n0000000000 65535 f \n");
        pdfContent.append("trailer << /Size 6 /Root 1 0 R >>\nstartxref\n500\n%%EOF\n");

        saveReportRecord("PDF Executive Summary", ReportType.PDF, startDate, endDate);
        return pdfContent.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void saveReportRecord(String title, ReportType type, LocalDate startDate, LocalDate endDate) {
        Report report = Report.builder()
                .reportName(title)
                .reportType(type)
                .startDate(startDate)
                .endDate(endDate)
                .status(ReportStatus.COMPLETED)
                .downloadUrl("/analytics/reports/export/" + type.name().toLowerCase())
                .build();
        reportRepository.save(report);
    }
}
