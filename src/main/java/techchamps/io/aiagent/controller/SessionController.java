package techchamps.io.aiagent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import techchamps.io.aiagent.model.UserSession;
import techchamps.io.aiagent.service.UserSessionService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/session")
public class SessionController {
    
    @Autowired
    private UserSessionService userSessionService;
    
    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateSession() {
        String sessionId = userSessionService.generateSessionId();
        Map<String, String> response = new HashMap<>();
        response.put("sessionId", sessionId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{sessionId}")
    public ResponseEntity<Map<String, Object>> getSession(@PathVariable String sessionId) {
        UserSession session = userSessionService.getOrCreateSession(sessionId);
        Map<String, Object> response = new HashMap<>();
        
        response.put("sessionId", session.getSessionId());
        response.put("githubToken", session.getGithubToken());
        response.put("githubUsername", session.getGithubUsername());
        response.put("githubDisplayName", session.getGithubDisplayName());
        response.put("selectedRepository", session.getSelectedRepository());
        response.put("openaiApiKey", session.getOpenaiApiKey());
        response.put("chatModel", session.getChatModel());
        response.put("imageModel", session.getImageModel());
        
        // Parse repositories JSON
        List<Object> repositories = userSessionService.getRepositories(sessionId);
        response.put("repositories", repositories);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{sessionId}/github-token")
    public ResponseEntity<Map<String, String>> saveGithubToken(
            @PathVariable String sessionId,
            @RequestBody Map<String, String> request) {
        
        String token = request.get("token");
        userSessionService.saveGithubToken(sessionId, token);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "GitHub token saved successfully");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{sessionId}/github-user")
    public ResponseEntity<Map<String, String>> saveGithubUser(
            @PathVariable String sessionId,
            @RequestBody Map<String, String> request) {
        
        String username = request.get("username");
        String displayName = request.get("displayName");
        userSessionService.saveGithubUserInfo(sessionId, username, displayName);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "GitHub user info saved successfully");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{sessionId}/repository")
    public ResponseEntity<Map<String, String>> saveSelectedRepository(
            @PathVariable String sessionId,
            @RequestBody Map<String, String> request) {
        
        String repository = request.get("repository");
        userSessionService.saveSelectedRepository(sessionId, repository);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Repository saved successfully");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{sessionId}/repositories")
    public ResponseEntity<Map<String, String>> saveRepositories(
            @PathVariable String sessionId,
            @RequestBody Map<String, Object> request) {
        
        @SuppressWarnings("unchecked")
        List<Object> repositories = (List<Object>) request.get("repositories");
        userSessionService.saveRepositories(sessionId, repositories);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Repositories saved successfully");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{sessionId}/openai-config")
    public ResponseEntity<Map<String, String>> saveOpenAIConfig(
            @PathVariable String sessionId,
            @RequestBody Map<String, String> request) {
        
        String apiKey = request.get("apiKey");
        String chatModel = request.get("chatModel");
        String imageModel = request.get("imageModel");
        userSessionService.saveOpenAIConfig(sessionId, apiKey, chatModel, imageModel);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "OpenAI config saved successfully");
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{sessionId}/github")
    public ResponseEntity<Map<String, String>> clearGithubData(@PathVariable String sessionId) {
        userSessionService.clearGithubData(sessionId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "GitHub data cleared successfully");
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{sessionId}/repository")
    public ResponseEntity<Map<String, String>> clearRepositoryData(@PathVariable String sessionId) {
        userSessionService.clearRepositoryData(sessionId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Repository data cleared successfully");
        return ResponseEntity.ok(response);
    }
} 