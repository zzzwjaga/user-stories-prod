package com.prod.user_stories_prod.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prod.user_stories_prod.exseptions.ValidationException;
import com.prod.user_stories_prod.responses.InvestCheckResponce;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${vsegpt.api.url:https://api.vsegpt.ru/v1/chat/completions}")
    private String apiUrl;

    @Value("${vsegpt.api.key}")
    private String apiKey;

    @Value("${vsegpt.model:gpt-4o-mini}")
    private String model;

    public AIService() {
        this.restClient = RestClient.builder()
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public InvestCheckResponce investCheck(String story) {

        String prompt = """
        Оцени пользовательскую историю по критериям INVEST (каждый от 0 до 10).
        Ответь ТОЛЬКО в формате JSON.

        История: %s

        Формат ответа:
        {
            "independentScore": число,
            "negotiableScore": число,
            "valuableScore": число,
            "estimableScore": число,
            "smallScore": число,
            "testableScore": число,
            "issues": "проблемы",
            "suggestions": "рекомендации"
        }
        """.formatted(story);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("temperature", 0.7);

        try {

            String requestJson = objectMapper.writeValueAsString(requestBody);

            String responseJson = restClient.post()
                    .uri(apiUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestJson)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError,
                            (request2, response) -> {

                                String body = new String(
                                        response.getBody().readAllBytes()
                                );

                                throw new ValidationException(
                                        "GPT API ERROR: "
                                                + response.getStatusCode()
                                                + " BODY: "
                                                + body
                                );
                            })
                    .body(String.class);

            if (responseJson == null || responseJson.isBlank()) {
                throw new ValidationException("Empty response from AI");
            }

            JsonNode root = objectMapper.readTree(responseJson);

            String content = root.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

            String jsonContent = extractJson(content);

            return objectMapper.readValue(
                    jsonContent,
                    InvestCheckResponce.class
            );

        } catch (ValidationException e) {

            throw e;

        } catch (Exception e) {

            throw new ValidationException(
                    "AI processing error: " + e.getMessage()
            );
        }
    }


    private String extractJson(String content) {
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start != -1 && end != -1 && start < end) {
            return content.substring(start, end + 1);
        }
        return content;
    }
}