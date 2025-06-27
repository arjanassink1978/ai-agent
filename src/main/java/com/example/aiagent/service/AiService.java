package com.example.aiagent.service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class AiService {

    @Value("${openai.api.key:}")
    private String apiKey;

    @Value("${openai.model:gpt-3.5-turbo}")
    private String model;

    private OpenAiService openAiService;

    public void initializeService() {
        if (apiKey != null && !apiKey.isEmpty()) {
            this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(30));
        }
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
        initializeService();
    }

    public String generateResponse(String userMessage) {
        if (openAiService == null) {
            return "AI service is not configured. Please set the OpenAI API key in application.properties or use the configuration form.";
        }

        try {
            ChatMessage message = new ChatMessage("user", userMessage);
            
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(List.of(message))
                    .maxTokens(500)
                    .temperature(0.7)
                    .build();

            return openAiService.createChatCompletion(request)
                    .getChoices().get(0).getMessage().getContent();
        } catch (Exception e) {
            return "Sorry, I encountered an error: " + e.getMessage();
        }
    }

    public boolean isConfigured() {
        return openAiService != null;
    }
} 