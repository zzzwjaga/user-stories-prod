package com.prod.user_stories_prod.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.prod.user_stories_prod.responses.InvestCheckResponce;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

@Service
public class AIService {

    private final OpenAiChatModel chatModel;
    private final ObjectMapper objectMapper;


    public AIService(OpenAiChatModel chatModel, ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    public InvestCheckResponce investCheck(String story_text) {
        String prompt = buildPrompt(story_text);

        try {
            ChatResponse response = chatModel.call(new Prompt(prompt));
            String content = response.getResult().getOutput().getText();

            String jsonContent = extractJson(content);

            return objectMapper.readValue(jsonContent, InvestCheckResponce.class);
        } catch (Exception e) {
            System.err.println("Error calling AI: " + e.getMessage());
            return new InvestCheckResponce(0, 0, 0, 0, 0, 0,
                    "Ошибка при вызове AI: " + e.getMessage(),
                    "Проверьте подключение к DeepSeek API");
        }
    }


    private String buildPrompt(String story_text) {
        return """
            Ты — эксперт по методологии INVEST для оценки пользовательских историй (user stories).
            
            Оцени следующую пользовательскую историю по каждому критерию INVEST.
            Каждый критерий оценивается от 0 до 10, где:
            - 0-2: критическое нарушение критерия
            - 2-4: серьёзные проблемы
            - 4-6: среднее качество, есть проблемы
            - 6-8: хорошее качество
            - 8-10: отличное соответствие критерию
            
            Пользовательская история: %s
          
            
            Ответь ТОЛЬКО в формате JSON, без лишнего текста:
            {
                "independentScore": число,
                "negotiableScore": число,
                "valuableScore": число,
                "estimableScore": число,
                "smallScore": число,
                "testableScore": число,
                "issues": "список проблем (кратко)",
                "suggestions": "рекомендации по улучшению (кратко)"
            }
            """.formatted(
                story_text
        );
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
