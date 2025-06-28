package techchamps.io.aiagent.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import techchamps.io.aiagent.service.MCPService;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/mcp")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "http://localhost:8080"})
public class MCPController {

    private static final Logger logger = LoggerFactory.getLogger(MCPController.class);

    @Autowired
    private MCPService mcpService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/status")
    public CompletableFuture<ResponseEntity<Object>> getMCPStatus() {
        return mcpService.getMCPStatus()
            .thenApply(status -> {
                if (status.has("error")) {
                    return ResponseEntity.status(503).body(status);
                }
                return ResponseEntity.ok(status);
            });
    }

    @PostMapping("/repositories")
    public CompletableFuture<ResponseEntity<Object>> listRepositories(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String username = request.get("username");
        
        if (token == null || username == null) {
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("success", false);
            errorResponse.put("error", "Token and username are required");
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(errorResponse));
        }
        
        return mcpService.listRepositories(token, username)
            .thenApply(response -> {
                if (response.get("success").asBoolean()) {
                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.status(500).body(response);
                }
            });
    }

    @PostMapping("/connect")
    public CompletableFuture<ResponseEntity<Object>> connectToRepository(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String repository = request.get("repository");
        
        if (token == null || repository == null) {
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("success", false);
            errorResponse.put("error", "Token and repository are required");
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(errorResponse));
        }
        
        return mcpService.connectToRepository(token, repository)
            .thenApply(response -> {
                if (response.get("success").asBoolean()) {
                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.status(500).body(response);
                }
            });
    }

    @PostMapping("/issues")
    public CompletableFuture<ResponseEntity<Object>> createIssue(@RequestBody Map<String, Object> request) {
        String repository = (String) request.get("repository");
        String title = (String) request.get("title");
        String body = (String) request.get("body");
        String token = (String) request.get("token");
        Object labelsObj = request.get("labels");
        
        if (repository == null || title == null || body == null || token == null) {
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("success", false);
            errorResponse.put("error", "Repository, title, body, and token are required");
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(errorResponse));
        }
        
        // Convert labels to array if it's a string or array
        String[] labels = null;
        if (labelsObj != null) {
            if (labelsObj instanceof String) {
                labels = new String[]{(String) labelsObj};
            } else if (labelsObj instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<Object> labelsList = (java.util.List<Object>) labelsObj;
                labels = labelsList.stream()
                    .map(Object::toString)
                    .toArray(String[]::new);
            }
        }
        
        return mcpService.createIssue(repository, title, body, token, labels)
            .thenApply(response -> {
                if (response.get("success").asBoolean()) {
                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.status(500).body(response);
                }
            });
    }

    @GetMapping("/files/{repository}/**")
    public CompletableFuture<ResponseEntity<Object>> getFileContent(
            @PathVariable String repository,
            @RequestParam String path,
            @RequestParam String token) {
        
        if (repository == null || path == null || token == null) {
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("success", false);
            errorResponse.put("error", "Repository, path, and token are required");
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(errorResponse));
        }
        
        return mcpService.getFileContent(repository, path, token)
            .thenApply(response -> {
                if (response.get("success").asBoolean()) {
                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.status(500).body(response);
                }
            });
    }
} 