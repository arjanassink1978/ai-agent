package techchamps.io.aiagent.service;

import techchamps.io.aiagent.model.*;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AiService {

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    @Value("${openai.model:gpt-4}")
    private String model;

    @Value("${openai.image.model:dall-e-3}")
    private String imageModel;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private OpenAiService openAiService;

    public void configureOpenAi(String apiKey) {
        this.openAiApiKey = apiKey;
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(60));
        } else {
            this.openAiService = null;
        }
    }

    public ChatResponse chat(ChatRequest request) {
        if (openAiService == null) {
            return new ChatResponse("AI service is not configured. Please set the OpenAI API key.");
        }

        try {
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("system", "You are a helpful AI assistant."));
            
            messages.add(new ChatMessage("user", request.getMessage()));

            ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(messages)
                    .maxTokens(1000)
                    .build();

            String response = openAiService.createChatCompletion(completionRequest)
                    .getChoices().get(0).getMessage().getContent();

            return new ChatResponse(response);
        } catch (Exception e) {
            return new ChatResponse("Error: " + e.getMessage());
        }
    }

    public ImageResponse generateImage(ImageRequest request) {
        if (openAiService == null) {
            return new ImageResponse("AI service is not configured. Please set the OpenAI API key.");
        }

        try {
            CreateImageRequest imageRequest = CreateImageRequest.builder()
                    .prompt(request.getPrompt())
                    .n(1)
                    .size("1024x1024")
                    .build();

            List<String> imageUrls = openAiService.createImage(imageRequest)
                    .getData().stream()
                    .map(image -> image.getUrl())
                    .collect(Collectors.toList());

            return new ImageResponse(imageUrls, request.getPrompt(), "dall-e-3");
        } catch (Exception e) {
            return new ImageResponse("Error generating image: " + e.getMessage());
        }
    }

    public FileUploadResponse handleFileUpload(MultipartFile file, String context, String prompt) {
        if ("image".equals(context)) {
            String uniqueFilename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            String originalFilename = file.getOriginalFilename();
            return handleImageFileUpload(file, uniqueFilename, originalFilename, prompt);
        } else {
            String uniqueFilename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            String originalFilename = file.getOriginalFilename();
            return handleChatFileUpload(file, uniqueFilename, originalFilename);
        }
    }

    public FileUploadResponse handleImageFileUpload(MultipartFile file, String uniqueFilename, String originalFilename, String prompt) {
        try {
            if (openAiService == null) {
                return new FileUploadResponse("AI service is not configured. Please set the OpenAI API key.");
            }

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath);

            String generationPrompt = prompt != null ? prompt : "Create a creative variation inspired by the uploaded image";
            
            CreateImageRequest imageRequest = CreateImageRequest.builder()
                    .prompt(generationPrompt)
                    .n(1)
                    .size("1024x1024")
                    .build();

            List<String> imageUrls = openAiService.createImage(imageRequest)
                    .getData().stream()
                    .map(image -> image.getUrl())
                    .collect(Collectors.toList());

            return new FileUploadResponse(
                "Image uploaded and processed successfully",
                uniqueFilename,
                imageUrls,
                "image"
            );

        } catch (Exception e) {
            e.printStackTrace();
            return new FileUploadResponse("Error generating image: " + e.getMessage());
        }
    }

    private FileUploadResponse handleChatFileUpload(MultipartFile file, String uniqueFilename, String originalFilename) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath);

            return new FileUploadResponse(
                "File uploaded successfully",
                uniqueFilename,
                "chat"
            );
        } catch (IOException e) {
            return new FileUploadResponse("Error uploading file: " + e.getMessage());
        }
    }

    // Configuration and model management methods
    public void configure(String apiKey, String model) {
        this.openAiApiKey = apiKey;
        this.model = model;
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(60));
        }
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setImageModel(String imageModel) {
        this.imageModel = imageModel;
    }

    public String getCurrentModel() {
        return model;
    }

    public String getCurrentImageModel() {
        return imageModel;
    }

    public boolean isConfigured() {
        return openAiService != null && openAiApiKey != null && !openAiApiKey.trim().isEmpty();
    }

    public List<String> getAvailableModels() {
        return List.of("gpt-4", "gpt-4-turbo", "gpt-4o", "gpt-3.5-turbo", "gpt-3.5-turbo-16k");
    }

    public List<String> getImageModels() {
        return List.of("dall-e-3", "dall-e-2");
    }

    public ChatResponse generateResponseWithSession(ChatRequest request) {
        if (openAiService == null) {
            return new ChatResponse("AI service is not configured. Please set the OpenAI API key.");
        }

        try {
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("system", "You are a helpful AI assistant."));
            messages.add(new ChatMessage("user", request.getMessage()));

            ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(messages)
                    .maxTokens(1000)
                    .build();

            String response = openAiService.createChatCompletion(completionRequest)
                    .getChoices().get(0).getMessage().getContent();

            return new ChatResponse(response);
        } catch (Exception e) {
            return new ChatResponse("Error: " + e.getMessage());
        }
    }

    // Overload for controller compatibility
    public ImageResponse generateImage(String prompt, String size, String quality, String style) {
        ImageRequest req = new ImageRequest();
        req.setPrompt(prompt);
        req.setSize(size);
        req.setQuality(quality);
        req.setStyle(style);
        req.setModel(this.imageModel);
        return generateImage(req);
    }
} 