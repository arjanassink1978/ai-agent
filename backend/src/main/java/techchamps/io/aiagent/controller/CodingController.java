package techchamps.io.aiagent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import techchamps.io.aiagent.service.GitHubService;
import techchamps.io.aiagent.service.MCPService;
import techchamps.io.aiagent.service.AiService;
import techchamps.io.aiagent.model.ChatRequest;
import techchamps.io.aiagent.model.ChatResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import techchamps.io.aiagent.model.RepositoryConnectRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "http://localhost:8080"})
public class CodingController {

    @Autowired
    private GitHubService githubService;

    @Autowired
    private MCPService mcpService;

    @Autowired
    private AiService aiService;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
        
        // Try to get username from the token first
        Map<String, Object> authResult = githubService.authenticateUser(personalAccessToken);
        if (!(Boolean) authResult.get("success")) {
            return ResponseEntity.ok(authResult);
        }
        
        String username = (String) authResult.get("username");
        
        // Use MCP service to list repositories
        try {
            CompletableFuture<ObjectNode> mcpResult = mcpService.listRepositories(personalAccessToken, username);
            ObjectNode result = mcpResult.get();
            
            if (result.get("success").asBoolean()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("repositories", result.get("repositories"));
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", result.get("error").asText());
                return ResponseEntity.ok(error);
            }
        } catch (Exception e) {
            // Fallback to GitHubService if MCP fails
            Map<String, Object> result = githubService.getUserRepositories(personalAccessToken);
            return ResponseEntity.ok(result);
        }
    }

    @PostMapping("/connect-repository")
    public ResponseEntity<Map<String, Object>> connectToRepository(@RequestBody RepositoryConnectRequest request) {
        String personalAccessToken = request.getPersonalAccessToken();
        String repositoryUrl = request.getRepositoryUrl();

        System.out.println("=== CONNECT REPOSITORY DEBUG ===");
        System.out.println("personalAccessToken: " + (personalAccessToken != null ? "present (length: " + personalAccessToken.length() + ")" : "null"));
        System.out.println("repositoryUrl: " + repositoryUrl);
        System.out.println("repositoryUrl class: " + (repositoryUrl != null ? repositoryUrl.getClass().getName() : "null"));
        System.out.println("Full request object: " + request);
        System.out.println("================================");

        Map<String, Object> response = new HashMap<>();
        if (personalAccessToken == null || personalAccessToken.isEmpty()) {
            response.put("success", false);
            response.put("error", "Personal Access Token is required");
            return ResponseEntity.badRequest().body(response);
        }
        if (repositoryUrl == null || repositoryUrl.isEmpty()) {
            response.put("success", false);
            response.put("error", "Repository URL is required");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Extract repository name from URL
        String repository = repositoryUrl.replace("https://github.com/", "");
        
        // Use MCP service to connect to repository
        try {
            CompletableFuture<ObjectNode> mcpResult = mcpService.connectToRepository(personalAccessToken, repository);
            ObjectNode result = mcpResult.get();
            
            if (result.get("success").asBoolean()) {
                response.put("success", true);
                response.put("repository", repository);
                // Get files using GitHubService for now
                Map<String, Object> filesResult = githubService.connectToRepository(personalAccessToken, repositoryUrl);
                if ((Boolean) filesResult.get("success")) {
                    response.put("files", filesResult.get("files"));
                }
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("error", result.get("error").asText());
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            // Fallback to GitHubService if MCP fails
            Map<String, Object> result = githubService.connectToRepository(personalAccessToken, repositoryUrl);
            return ResponseEntity.ok(result);
        }
    }

    @PostMapping("/coding-chat")
    public ResponseEntity<ChatResponse> codingChat(@RequestBody ChatRequest request) {
        try {
            String personalAccessToken = (String) request.getMetadata().get("personalAccessToken");
            String repositoryUrl = (String) request.getMetadata().get("repositoryUrl");
            List<String> selectedFiles = request.getMetadata().get("selectedFiles") != null ? 
                (List<String>) request.getMetadata().get("selectedFiles") : null;

            // Extract repository name from URL
            String repository = repositoryUrl != null ? repositoryUrl.replace("https://github.com/", "") : null;

            // Build the payload for the smart /execute endpoint
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode payload = mapper.createObjectNode();
            payload.put("message", request.getMessage());
            payload.put("repository", repository);
            payload.put("token", personalAccessToken);
            if (selectedFiles != null && !selectedFiles.isEmpty()) {
                payload.putPOJO("selectedFiles", selectedFiles);
            }

            // Call the new smart /execute endpoint via MCPService
            ObjectNode result = mcpService.execute(payload).get();
            ChatResponse response = new ChatResponse();
            response.setMessage(result.has("message") ? result.get("message").asText() : result.toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setMessage("Sorry, I encountered an error while processing your request: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
} 