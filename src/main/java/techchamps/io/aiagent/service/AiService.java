package techchamps.io.aiagent.service;

import techchamps.io.aiagent.model.*;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AiService {

    @Value("${openai.api.key:}")
    private String apiKey;

    @Value("${openai.model:gpt-4}")
    private String defaultModel;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

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
        return ModelConfig.IMAGE_MODELS;
    }

    public void setCurrentImageModel(String model) {
        if (ModelConfig.IMAGE_MODELS.contains(model)) {
            this.currentImageModel = model;
        }
    }

    // File upload methods
    public FileUploadResponse handleFileUpload(MultipartFile file, String context, String prompt) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return new FileUploadResponse("File is empty");
            }

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(uniqueFilename);

            // Save file
            Files.copy(file.getInputStream(), filePath);

            if ("chat".equals(context)) {
                return handleChatFileUpload(file, uniqueFilename, originalFilename);
            } else if ("image".equals(context)) {
                return handleImageFileUpload(file, uniqueFilename, originalFilename, prompt);
            } else {
                return new FileUploadResponse("Invalid context. Use 'chat' or 'image'");
            }

        } catch (IOException e) {
            return new FileUploadResponse("Error uploading file: " + e.getMessage());
        }
    }

    private FileUploadResponse handleChatFileUpload(MultipartFile file, String uniqueFilename, String originalFilename) {
        try {
            // Read file content
            String fileContent = new String(file.getBytes());
            
            // Create a message with file content
            String message = "I've uploaded a file: " + originalFilename + "\n\nFile content:\n" + fileContent;
            
            return new FileUploadResponse(
                "File uploaded successfully for chat analysis", 
                originalFilename, 
                "chat"
            );
        } catch (IOException e) {
            return new FileUploadResponse("Error reading file content: " + e.getMessage());
        }
    }

    private FileUploadResponse handleImageFileUpload(MultipartFile file, String uniqueFilename, String originalFilename, String prompt) {
        try {
            // For image generation, we'll use the uploaded image as a reference
            // This would typically involve calling OpenAI's image variation API
            // For now, we'll return a success message
            String message = "Image uploaded successfully for generation. " +
                           (prompt != null && !prompt.trim().isEmpty() ? 
                            "Prompt: " + prompt : "No prompt provided");
            
            return new FileUploadResponse(
                message, 
                originalFilename, 
                "image"
            );
        } catch (Exception e) {
            return new FileUploadResponse("Error processing image: " + e.getMessage());
        }
    }
} 