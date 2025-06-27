package techchamps.io.aiagent.controller;

import techchamps.io.aiagent.model.*;
import techchamps.io.aiagent.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ChatController {

    @Autowired
    private AiService aiService;

    @GetMapping("/")
    public String chatPage(Model model) {
        model.addAttribute("availableModels", aiService.getAvailableModels());
        model.addAttribute("imageModels", aiService.getImageModels());
        model.addAttribute("currentModel", aiService.getCurrentModel());
        model.addAttribute("currentImageModel", aiService.getCurrentImageModel());
        model.addAttribute("isConfigured", aiService.isConfigured());
        return "chat";
    }

    @PostMapping("/api/chat")
    @ResponseBody
    public ChatResponse chat(@RequestBody ChatRequest request) {
        try {
            String response = aiService.generateResponse(request.getMessage());
            return new ChatResponse(response);
        } catch (Exception e) {
            return new ChatResponse(null, "Error: " + e.getMessage());
        }
    }

    @PostMapping("/api/configure")
    @ResponseBody
    public ResponseEntity<String> configure(@RequestBody ConfigurationRequest request) {
        try {
            aiService.configure(request.getApiKey(), request.getModel());
            return ResponseEntity.ok("Configuration updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Configuration failed: " + e.getMessage());
        }
    }

    @PostMapping("/api/set-model")
    @ResponseBody
    public ResponseEntity<String> setModel(@RequestParam String model) {
        try {
            aiService.setModel(model);
            return ResponseEntity.ok("Model updated to: " + model);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update model: " + e.getMessage());
        }
    }

    @PostMapping("/api/generate-image")
    @ResponseBody
    public ImageResponse generateImage(@RequestBody ImageRequest request) {
        try {
            List<String> imageUrls = aiService.generateImage(
                request.getPrompt(),
                request.getSize(),
                request.getQuality(),
                request.getStyle()
            );
            return new ImageResponse(imageUrls, request.getPrompt(), request.getModel());
        } catch (Exception e) {
            return new ImageResponse("Error generating image: " + e.getMessage());
        }
    }

    @PostMapping("/api/set-image-model")
    @ResponseBody
    public ResponseEntity<String> setImageModel(@RequestParam String model) {
        try {
            aiService.setImageModel(model);
            return ResponseEntity.ok("Image model updated to: " + model);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update image model: " + e.getMessage());
        }
    }

    @GetMapping("/api/models")
    @ResponseBody
    public ModelConfig getModels() {
        return new ModelConfig(aiService.getCurrentModel(), aiService.getCurrentImageModel());
    }
} 