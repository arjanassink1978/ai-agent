package techchamps.io.aiagent.controller;

import techchamps.io.aiagent.model.*;
import techchamps.io.aiagent.service.AiService;
import techchamps.io.aiagent.service.ChatSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class ChatController {

    @Autowired
    private AiService aiService;
    
    @Autowired
    private ChatSessionService chatSessionService;

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
            // Use the new session-aware method
            return aiService.generateResponseWithSession(request);
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

    @PostMapping("/api/image")
    public ResponseEntity<ImageResponse> generateImage(@RequestBody ImageRequest request) {
        try {
            ImageResponse response = aiService.generateImage(
                request.getPrompt(),
                request.getSize(),
                request.getQuality(),
                request.getStyle()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ImageResponse("Error generating image: " + e.getMessage()));
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

    @PostMapping("/api/upload/chat")
    public ResponseEntity<FileUploadResponse> uploadFileForChat(@RequestParam("file") MultipartFile file) {
        try {
            FileUploadResponse response = aiService.handleFileUpload(file, "chat", null);
            if (response.getError() != null) {
                return ResponseEntity.badRequest().body(response);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new FileUploadResponse("Error uploading file: " + e.getMessage()));
        }
    }

    @PostMapping("/api/upload/image")
    public ResponseEntity<FileUploadResponse> uploadFileForImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "prompt", required = false) String prompt) {
        try {
            FileUploadResponse response = aiService.handleFileUpload(file, "image", prompt);
            if (response.getError() != null) {
                return ResponseEntity.badRequest().body(response);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new FileUploadResponse("Error uploading file: " + e.getMessage()));
        }
    }
    
    // Session management endpoints
    @GetMapping("/api/sessions")
    @ResponseBody
    public List<ChatSession> getAllSessions() {
        return chatSessionService.getAllSessions();
    }
    
    @GetMapping("/api/sessions/{sessionId}")
    @ResponseBody
    public ResponseEntity<SessionResponse> getSession(@PathVariable String sessionId) {
        try {
            var session = chatSessionService.getSession(sessionId);
            if (session.isPresent()) {
                ChatSession chatSession = session.get();
                SessionResponse response = new SessionResponse(
                    chatSession.getSessionId(),
                    chatSession.getTitle(),
                    chatSession.getContext(),
                    chatSession.getModel(),
                    chatSession.getImageModel()
                );
                response.setMessages(chatSessionService.getSessionMessages(sessionId));
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            SessionResponse errorResponse = new SessionResponse();
            errorResponse.setError("Error retrieving session: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PostMapping("/api/sessions")
    @ResponseBody
    public ResponseEntity<SessionResponse> createSession(@RequestBody SessionRequest request) {
        try {
            ChatSession session = chatSessionService.createSession(
                request.getTitle(),
                request.getContext(),
                request.getModel(),
                request.getImageModel()
            );
            SessionResponse response = new SessionResponse(
                session.getSessionId(),
                session.getTitle(),
                session.getContext(),
                session.getModel(),
                session.getImageModel()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            SessionResponse errorResponse = new SessionResponse();
            errorResponse.setError("Error creating session: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PutMapping("/api/sessions/{sessionId}/context")
    @ResponseBody
    public ResponseEntity<SessionResponse> updateSessionContext(
            @PathVariable String sessionId,
            @RequestBody String context) {
        try {
            ChatSession session = chatSessionService.updateSessionContext(sessionId, context);
            SessionResponse response = new SessionResponse(
                session.getSessionId(),
                session.getTitle(),
                session.getContext(),
                session.getModel(),
                session.getImageModel()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            SessionResponse errorResponse = new SessionResponse();
            errorResponse.setError("Error updating session context: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PutMapping("/api/sessions/{sessionId}/title")
    @ResponseBody
    public ResponseEntity<SessionResponse> updateSessionTitle(
            @PathVariable String sessionId,
            @RequestBody String title) {
        try {
            ChatSession session = chatSessionService.updateSessionTitle(sessionId, title);
            SessionResponse response = new SessionResponse(
                session.getSessionId(),
                session.getTitle(),
                session.getContext(),
                session.getModel(),
                session.getImageModel()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            SessionResponse errorResponse = new SessionResponse();
            errorResponse.setError("Error updating session title: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @DeleteMapping("/api/sessions/{sessionId}")
    @ResponseBody
    public ResponseEntity<String> deleteSession(@PathVariable String sessionId) {
        try {
            chatSessionService.deleteSession(sessionId);
            return ResponseEntity.ok("Session deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting session: " + e.getMessage());
        }
    }
    
    @GetMapping("/api/sessions/search")
    @ResponseBody
    public List<ChatSession> searchSessions(@RequestParam String q) {
        return chatSessionService.searchSessions(q);
    }
} 