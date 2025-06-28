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
            // Extract GitHub token and repository info from the request
            String personalAccessToken = (String) request.getMetadata().get("personalAccessToken");
            String repositoryUrl = (String) request.getMetadata().get("repositoryUrl");
            List<String> selectedFiles = request.getMetadata().get("selectedFiles") != null ? 
                (List<String>) request.getMetadata().get("selectedFiles") : null;
            
            // Check if the user is asking to create an issue
            String message = request.getMessage().toLowerCase();
            if (message.contains("create issue") || message.contains("create an issue") || 
                message.contains("open issue") || message.contains("file issue") ||
                message.contains("report issue") || message.contains("submit issue")) {
                
                // Extract repository name from URL
                String repository = repositoryUrl.replace("https://github.com/", "");
                
                // Try to extract title and body from the message
                String title = "Issue from AI Assistant";
                String body = request.getMessage();
                
                // Use MCP service to create issue
                try {
                    CompletableFuture<ObjectNode> mcpResult = mcpService.createIssue(repository, title, body, personalAccessToken, null);
                    ObjectNode result = mcpResult.get();
                    
                    if (result.get("success").asBoolean()) {
                        ChatResponse response = new ChatResponse();
                        response.setMessage("✅ Issue created successfully!\n\n" +
                            "Issue #" + result.get("issueNumber").asText() + "\n" +
                            "URL: " + result.get("issueUrl").asText() + "\n\n" +
                            "I've created an issue in your repository with your request. You can view and edit it at the link above.");
                        return ResponseEntity.ok(response);
                    } else {
                        ChatResponse response = new ChatResponse();
                        response.setMessage("❌ Failed to create issue: " + result.get("error").asText());
                        return ResponseEntity.ok(response);
                    }
                } catch (Exception e) {
                    ChatResponse response = new ChatResponse();
                    response.setMessage("❌ Error creating issue: " + e.getMessage());
                    return ResponseEntity.ok(response);
                }
            }
            
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