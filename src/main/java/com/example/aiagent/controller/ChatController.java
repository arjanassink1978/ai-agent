package com.example.aiagent.controller;

import com.example.aiagent.model.ChatRequest;
import com.example.aiagent.model.ChatResponse;
import com.example.aiagent.model.ConfigurationRequest;
import com.example.aiagent.model.ImageRequest;
import com.example.aiagent.model.ImageResponse;
import com.example.aiagent.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
public class ChatController {

    @Autowired
    private AiService aiService;

    @GetMapping("/")
    public String chatPage(Model model) {
        model.addAttribute("isConfigured", aiService.isConfigured());
        model.addAttribute("currentModel", aiService.getCurrentModel());
        model.addAttribute("currentImageModel", aiService.getCurrentImageModel());
        model.addAttribute("availableModels", aiService.getAvailableModels());
        model.addAttribute("imageModels", aiService.getImageModels());
        return "chat";
    }

    @PostMapping("/api/chat")
    @ResponseBody
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        try {
            String response = aiService.generateResponse(request.getMessage());
            return ResponseEntity.ok(new ChatResponse(response));
        } catch (Exception e) {
            return ResponseEntity.ok(new ChatResponse("", "Error: " + e.getMessage()));
        }
    }

    @PostMapping("/api/generate-image")
    @ResponseBody
    public ResponseEntity<ImageResponse> generateImage(@RequestBody ImageRequest request) {
        try {
            if (request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
                return ResponseEntity.ok(new ImageResponse("Image prompt cannot be empty"));
            }
            
            String size = request.getSize() != null ? request.getSize() : "1024x1024";
            String quality = request.getQuality() != null ? request.getQuality() : "standard";
            String style = request.getStyle() != null ? request.getStyle() : "vivid";
            
            var imageUrls = aiService.generateImage(request.getPrompt().trim(), size, quality, style);
            return ResponseEntity.ok(new ImageResponse(imageUrls, request.getPrompt(), aiService.getCurrentImageModel()));
        } catch (Exception e) {
            return ResponseEntity.ok(new ImageResponse("Error: " + e.getMessage()));
        }
    }

    @PostMapping("/api/configure")
    @ResponseBody
    public ResponseEntity<ChatResponse> configure(@RequestBody ConfigurationRequest configRequest) {
        try {
            if (configRequest.getApiKey() == null || configRequest.getApiKey().trim().isEmpty()) {
                return ResponseEntity.ok(new ChatResponse("", "API key cannot be empty"));
            }
            
            aiService.configure(configRequest.getApiKey().trim(), configRequest.getModel());
            return ResponseEntity.ok(new ChatResponse("Configuration updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.ok(new ChatResponse("", "Error: " + e.getMessage()));
        }
    }

    @GetMapping("/api/models")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getModels() {
        Map<String, Object> response = new HashMap<>();
        response.put("currentModel", aiService.getCurrentModel());
        response.put("currentImageModel", aiService.getCurrentImageModel());
        response.put("availableModels", aiService.getAvailableModels());
        response.put("imageModels", aiService.getImageModels());
        response.put("isConfigured", aiService.isConfigured());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/model")
    @ResponseBody
    public ResponseEntity<ChatResponse> setModel(@RequestParam String model) {
        try {
            aiService.setModel(model);
            return ResponseEntity.ok(new ChatResponse("Model updated to: " + aiService.getCurrentModel()));
        } catch (Exception e) {
            return ResponseEntity.ok(new ChatResponse("", "Error: " + e.getMessage()));
        }
    }

    @PostMapping("/api/image-model")
    @ResponseBody
    public ResponseEntity<ChatResponse> setImageModel(@RequestParam String imageModel) {
        try {
            aiService.setImageModel(imageModel);
            return ResponseEntity.ok(new ChatResponse("Image model updated to: " + aiService.getCurrentImageModel()));
        } catch (Exception e) {
            return ResponseEntity.ok(new ChatResponse("", "Error: " + e.getMessage()));
        }
    }
} 