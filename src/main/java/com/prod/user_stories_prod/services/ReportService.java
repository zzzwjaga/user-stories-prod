package com.prod.user_stories_prod.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.prod.user_stories_prod.entities.Board;
import com.prod.user_stories_prod.entities.Report;
import com.prod.user_stories_prod.entities.Status;
import com.prod.user_stories_prod.exseptions.ValidationException;
import com.prod.user_stories_prod.repositories.BoardRepository;
import com.prod.user_stories_prod.repositories.ReportRepository;
import com.prod.user_stories_prod.responses.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final BoardRepository boardRepository;
    private final ObjectMapper  objectMapper;
    private static final Logger log =
            LoggerFactory.getLogger(ReportService.class);


    public ReportService(ReportRepository reportRepository, BoardRepository boardRepository, ObjectMapper objectMapper) {
        this.reportRepository = reportRepository;
        this.boardRepository = boardRepository;
        this.objectMapper = objectMapper;
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public Report generateReport(UUID board_id) {

        Optional<Board> board = boardRepository.findById(board_id);
        if(board.isEmpty()){
            log.error("Board not found boardId={}", board_id);
            throw new ValidationException(ErrorCode.BOARD_NOT_FOUND);
        }

        int totalStories = reportRepository.getTotalCount(board_id);
        int completedStories = reportRepository.getTotalCount(board_id);
        int storiesWithoutInvest = reportRepository.getStoriesWithoutInvest(board_id);
        double avgCompletionHours = reportRepository.avgCompetitionHours(board_id);
        double avgStoryPoints = reportRepository.getAvgStoryPoints(board_id);
        double avgInvest =  reportRepository.getAvgInvest(board_id);
        Map<String, Double> investScores = reportRepository.getAvgInvestScores(board_id);

        Map<Status, Double> statusDistribution = reportRepository.getStatusDisribution(board_id);

        return new Report(
                board_id,
                board.get().boardname(),
                totalStories,
                statusDistribution,
                avgCompletionHours,
                avgInvest,
                investScores.getOrDefault("independent", 0.0),
                investScores.getOrDefault("negotiable", 0.0),
                investScores.getOrDefault("valuable", 0.0),
                investScores.getOrDefault("estimable", 0.0),
                investScores.getOrDefault("small", 0.0),
                investScores.getOrDefault("testable", 0.0),
                completedStories,
                storiesWithoutInvest,
                avgStoryPoints
                );
    }

    public String genereatedAsJson(UUID board_id) {
        Report report = generateReport(board_id);
        try {
            log.info("Generating Report for Board {}", board_id);
            return objectMapper.writeValueAsString(report);
        } catch (Exception e) {
            log.error("Error while generating report");
            throw new ValidationException("Failed to convert report to JSON");
        }
    }

    public String generateReportAndSaveToFile(UUID board_id, String outputDir) {
        Report report = generateReport(board_id);
        try {
            log.info("Generating Report for Board {}", board_id);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("report_board_%s_%s.json", board_id.toString().substring(0, 8), timestamp);

            Path filePath = Paths.get(outputDir, filename);
            Files.createDirectories(filePath.getParent());
            objectMapper.writeValue(filePath.toFile(), report);
            return filePath.toString();
        } catch (Exception e) {
            log.error("Error while generating report");
            throw new ValidationException("Failed to convert report to JSON");

        }
    }





}
