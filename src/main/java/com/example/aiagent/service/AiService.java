package com.example.aiagent.service;

import com.example.aiagent.model.ModelConfig;
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

    @Value("${openai.model:gpt-4}")
    private String defaultModel;

    private OpenAiService openAiService;
    private String currentModel;

    public void initializeService() {
        if (apiKey != null && !apiKey.isEmpty()) {
            this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(30));
            this.currentModel = defaultModel;
        }
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
        initializeService();
    }

    public void setModel(String model) {
        if (ModelConfig.getAvailableModels().contains(model)) {
            this.currentModel = model;
        } else {
            this.currentModel = ModelConfig.getDefaultModel();
        }
    }

    public void configure(String apiKey, String model) {
        this.apiKey = apiKey;
        if (apiKey != null && !apiKey.isEmpty()) {
            this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(30));
        }
        setModel(model);
    }

    public String generateResponse(String userMessage) {
        if (openAiService == null) {
            return "AI service is not configured. Please set the OpenAI API key in application.properties or use the configuration form.";
        }

        try {
            ChatMessage message = new ChatMessage("user", userMessage);
            
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(currentModel != null ? currentModel : defaultModel)
                    .messages(List.of(message))
                    .maxTokens(1000)
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

    public String getCurrentModel() {
        return currentModel != null ? currentModel : defaultModel;
    }

    public List<String> getAvailableModels() {
        return ModelConfig.getAvailableModels();
    }
} 