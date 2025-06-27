package com.example.aiagent.controller;

import com.example.aiagent.model.ChatRequest;
import com.example.aiagent.model.ChatResponse;
import com.example.aiagent.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ChatController {

    @Autowired
    private AiService aiService;

    @GetMapping("/")
    public String chatPage(Model model) {
        model.addAttribute("isConfigured", aiService.isConfigured());
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

    @PostMapping("/api/configure")
    @ResponseBody
    public ResponseEntity<ChatResponse> configure(@RequestParam String apiKey) {
        try {
            if (apiKey == null || apiKey.trim().isEmpty()) {
                return ResponseEntity.ok(new ChatResponse("", "API key cannot be empty"));
            }
            
            aiService.setApiKey(apiKey.trim());
            return ResponseEntity.ok(new ChatResponse("Configuration updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.ok(new ChatResponse("", "Error: " + e.getMessage()));
        }
    }
} 