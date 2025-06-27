package techchamps.io.aiagent.service;

import techchamps.io.aiagent.model.ModelConfig;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiService {

    @Value("${openai.api.key:}")
    private String apiKey;

    @Value("${openai.model:gpt-4}")
    private String defaultModel;

    private OpenAiService openAiService;
    private String currentModel;
    private String currentImageModel;

    public void initializeService() {
        if (apiKey != null && !apiKey.isEmpty()) {
            this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(30));
            this.currentModel = defaultModel;
            this.currentImageModel = ModelConfig.getDefaultImageModel();
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

    public void setImageModel(String imageModel) {
        if (ModelConfig.getImageModels().contains(imageModel)) {
            this.currentImageModel = imageModel;
        } else {
            this.currentImageModel = ModelConfig.getDefaultImageModel();
        }
    }

    public void configure(String apiKey, String model) {
        this.apiKey = apiKey;
        if (apiKey != null && !apiKey.isEmpty()) {
            this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(30));
        }
        setModel(model);
        this.currentImageModel = ModelConfig.getDefaultImageModel();
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

    public List<String> generateImage(String prompt, String size, String quality, String style) {
        if (openAiService == null) {
            throw new RuntimeException("AI service is not configured. Please set the OpenAI API key.");
        }

        try {
            String imageSize = "1024x1024";
            if ("1792x1024".equals(size)) {
                imageSize = "1792x1024";
            } else if ("1024x1792".equals(size)) {
                imageSize = "1024x1792";
            }

            CreateImageRequest request = CreateImageRequest.builder()
                    .prompt(prompt)
                    .model(currentImageModel)
                    .size(imageSize)
                    .quality("hd".equals(quality) ? "hd" : "standard")
                    .style(style != null ? style : "vivid")
                    .n(1)
                    .build();

            return openAiService.createImage(request)
                    .getData()
                    .stream()
                    .map(data -> data.getUrl())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error generating image: " + e.getMessage());
        }
    }

    public boolean isConfigured() {
        return openAiService != null;
    }

    public String getCurrentModel() {
        return currentModel != null ? currentModel : defaultModel;
    }

    public String getCurrentImageModel() {
        return currentImageModel != null ? currentImageModel : ModelConfig.getDefaultImageModel();
    }

    public List<String> getAvailableModels() {
        return ModelConfig.getAvailableModels();
    }

    public List<String> getImageModels() {
        return ModelConfig.getImageModels();
    }
} 