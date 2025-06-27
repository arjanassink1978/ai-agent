package techchamps.io.aiagent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import techchamps.io.aiagent.service.GitHubService;
import techchamps.io.aiagent.service.AiService;
import techchamps.io.aiagent.model.ChatRequest;
import techchamps.io.aiagent.model.ChatResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CodingController {

    @Autowired
    private GitHubService githubService;

    @Autowired
    private AiService aiService;

    @PostMapping("/github/authenticate")
    public ResponseEntity<Map<String, Object>> authenticateGitHub(@RequestBody Map<String, String> request) {
        String personalAccessToken = request.get("personalAccessToken");
        
        if (personalAccessToken == null || personalAccessToken.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Personal Access Token is required");
            return ResponseEntity.badRequest().body(error);
        }
        
        Map<String, Object> result = githubService.authenticateUser(personalAccessToken);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/github/repositories")
    public ResponseEntity<Map<String, Object>> getUserRepositories(@RequestBody Map<String, String> request) {
        String personalAccessToken = request.get("personalAccessToken");
        
        if (personalAccessToken == null || personalAccessToken.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Personal Access Token is required");
            return ResponseEntity.badRequest().body(error);
        }
        
        Map<String, Object> result = githubService.getUserRepositories(personalAccessToken);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/connect-repository")
    public ResponseEntity<Map<String, Object>> connectToRepository(@RequestBody Map<String, String> request) {
        String personalAccessToken = request.get("personalAccessToken");
        String repositoryUrl = request.get("repositoryUrl");
        
        if (personalAccessToken == null || personalAccessToken.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Personal Access Token is required");
            return ResponseEntity.badRequest().body(error);
        }
        
        if (repositoryUrl == null || repositoryUrl.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Repository URL is required");
            return ResponseEntity.badRequest().body(error);
        }
        
        Map<String, Object> result = githubService.connectToRepository(personalAccessToken, repositoryUrl);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/coding-chat")
    public ResponseEntity<ChatResponse> codingChat(@RequestBody ChatRequest request) {
        try {
            // Extract GitHub token and repository info from the request
            String personalAccessToken = (String) request.getMetadata().get("personalAccessToken");
            String repositoryUrl = (String) request.getMetadata().get("repositoryUrl");
            List<String> selectedFiles = request.getMetadata().get("selectedFiles") != null ? 
                (List<String>) request.getMetadata().get("selectedFiles") : null;
            
            // Build context from selected files
            StringBuilder context = new StringBuilder();
            if (selectedFiles != null && !selectedFiles.isEmpty()) {
                context.append("Selected files for analysis:\n");
                for (String filePath : selectedFiles) {
                    String fileContent = githubService.getFileContent(personalAccessToken, repositoryUrl, filePath);
                    if (fileContent != null) {
                        context.append("\n--- File: ").append(filePath).append(" ---\n");
                        context.append(fileContent);
                        context.append("\n");
                    }
                }
            }
            
            // Add repository context to the message
            String enhancedMessage = "Repository: " + repositoryUrl + "\n\n" + 
                                   (context.length() > 0 ? "Context:\n" + context.toString() + "\n\n" : "") +
                                   "User question: " + request.getMessage();
            
            // Create enhanced request
            ChatRequest enhancedRequest = new ChatRequest();
            enhancedRequest.setMessage(enhancedMessage);
            enhancedRequest.setModel(request.getModel());
            
            ChatResponse response = aiService.chat(enhancedRequest);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setMessage("Sorry, I encountered an error while processing your request: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
} 