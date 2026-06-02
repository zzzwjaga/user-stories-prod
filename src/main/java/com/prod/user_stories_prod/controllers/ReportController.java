package com.prod.user_stories_prod.controllers;

import com.prod.user_stories_prod.entities.Report;
import com.prod.user_stories_prod.services.ReportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private  final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Value("${report.storage.dir:./reports}")
    private String reportsDir;

    @GetMapping("/board/{board_id}")
    @PreAuthorize("@boardSecurityService.canEdit(authentication.name, #board_id)")
    public ResponseEntity<Report> getReport(@PathVariable UUID board_id){
        Report report = reportService.generateReport(board_id);
        return ResponseEntity.ok(report);
    }

    @GetMapping(value = "/board/{board_id}/json", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("@boardSecurityService.canEdit(authentication.name, #board_id)")
    public ResponseEntity<String> getReportJson(@PathVariable UUID board_id){
        String json = reportService.genereatedAsJson(board_id);
        return ResponseEntity.ok(json);
    }

    @GetMapping("/board/{board_id}/export")
    @PreAuthorize("@boardSecurityService.canEdit(authentication.name, #board_id)")
    public ResponseEntity<byte[]> exportReport(@PathVariable UUID board_id){
        String filePath = reportService.generateReportAndSaveToFile(board_id, reportsDir);
        try{
            byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
            String filename = java.nio.file.Paths.get(filePath).getFileName().toString();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", filename);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileContent);

        } catch (Exception e) {
            throw new RuntimeException("Error while exporting report");
        }
    }
}
