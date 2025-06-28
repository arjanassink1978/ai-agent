package techchamps.io.aiagent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class MCPService {
    
    private static final Logger logger = LoggerFactory.getLogger(MCPService.class);
    
    @Value("${mcp.server.url:http://localhost:3001}")
    private String mcpServerUrl;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient = WebClient.builder().build();
    
    public CompletableFuture<ObjectNode> getMCPStatus() {
        return webClient.get()
            .uri(mcpServerUrl + "/api/mcp/status")
            .retrieve()
            .bodyToMono(String.class)
            .map(response -> {
                try {
                    return objectMapper.readValue(response, ObjectNode.class);
                } catch (Exception e) {
                    logger.error("Error parsing MCP status response", e);
                    ObjectNode errorResponse = objectMapper.createObjectNode();
                    errorResponse.put("error", "Failed to parse MCP status");
                    return errorResponse;
                }
            })
            .onErrorResume(e -> {
                logger.error("Error getting MCP status", e);
                ObjectNode errorResponse = objectMapper.createObjectNode();
                errorResponse.put("error", "MCP server not available: " + e.getMessage());
                return Mono.just(errorResponse);
            })
            .toFuture();
    }
    
    public CompletableFuture<ObjectNode> listRepositories(String token, String username) {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("token", token);
        request.put("username", username);
        
        return webClient.post()
            .uri(mcpServerUrl + "/api/mcp/repositories")
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .bodyValue(request.toString())
            .retrieve()
            .bodyToMono(String.class)
            .map(response -> {
                try {
                    return objectMapper.readValue(response, ObjectNode.class);
                } catch (Exception e) {
                    logger.error("Error parsing repository list response", e);
                    ObjectNode errorResponse = objectMapper.createObjectNode();
                    errorResponse.put("success", false);
                    errorResponse.put("error", "Failed to parse repository list");
                    return errorResponse;
                }
            })
            .onErrorResume(e -> {
                logger.error("Error listing repositories", e);
                ObjectNode errorResponse = objectMapper.createObjectNode();
                errorResponse.put("success", false);
                errorResponse.put("error", "Failed to list repositories: " + e.getMessage());
                return Mono.just(errorResponse);
            })
            .toFuture();
    }
    
    public CompletableFuture<ObjectNode> connectToRepository(String token, String repository) {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("token", token);
        request.put("repository", repository);
        
        return webClient.post()
            .uri(mcpServerUrl + "/api/mcp/connect")
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .bodyValue(request.toString())
            .retrieve()
            .bodyToMono(String.class)
            .map(response -> {
                try {
                    return objectMapper.readValue(response, ObjectNode.class);
                } catch (Exception e) {
                    logger.error("Error parsing connect response", e);
                    ObjectNode errorResponse = objectMapper.createObjectNode();
                    errorResponse.put("success", false);
                    errorResponse.put("error", "Failed to parse connect response");
                    return errorResponse;
                }
            })
            .onErrorResume(e -> {
                logger.error("Error connecting to repository", e);
                ObjectNode errorResponse = objectMapper.createObjectNode();
                errorResponse.put("success", false);
                errorResponse.put("error", "Failed to connect to repository: " + e.getMessage());
                return Mono.just(errorResponse);
            })
            .toFuture();
    }
    
    public CompletableFuture<ObjectNode> createIssue(String repository, String title, String body, String token, String[] labels) {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("repository", repository);
        request.put("title", title);
        request.put("body", body);
        request.put("token", token);
        
        if (labels != null && labels.length > 0) {
            ArrayNode labelsArray = request.putArray("labels");
            for (String label : labels) {
                labelsArray.add(label);
            }
        }
        
        return webClient.post()
            .uri(mcpServerUrl + "/api/mcp/issues")
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .bodyValue(request.toString())
            .retrieve()
            .bodyToMono(String.class)
            .map(response -> {
                try {
                    return objectMapper.readValue(response, ObjectNode.class);
                } catch (Exception e) {
                    logger.error("Error parsing issue creation response", e);
                    ObjectNode errorResponse = objectMapper.createObjectNode();
                    errorResponse.put("success", false);
                    errorResponse.put("error", "Failed to parse issue creation response");
                    return errorResponse;
                }
            })
            .onErrorResume(e -> {
                logger.error("Error creating issue", e);
                ObjectNode errorResponse = objectMapper.createObjectNode();
                errorResponse.put("success", false);
                errorResponse.put("error", "Failed to create issue: " + e.getMessage());
                return Mono.just(errorResponse);
            })
            .toFuture();
    }
    
    public CompletableFuture<ObjectNode> getFileContent(String repository, String path, String token) {
        return webClient.get()
            .uri(mcpServerUrl + "/api/mcp/files/" + repository + "/" + path + "?token=" + token)
            .retrieve()
            .bodyToMono(String.class)
            .map(response -> {
                try {
                    return objectMapper.readValue(response, ObjectNode.class);
                } catch (Exception e) {
                    logger.error("Error parsing file content response", e);
                    ObjectNode errorResponse = objectMapper.createObjectNode();
                    errorResponse.put("success", false);
                    errorResponse.put("error", "Failed to parse file content response");
                    return errorResponse;
                }
            })
            .onErrorResume(e -> {
                logger.error("Error getting file content", e);
                ObjectNode errorResponse = objectMapper.createObjectNode();
                errorResponse.put("success", false);
                errorResponse.put("error", "Failed to get file content: " + e.getMessage());
                return Mono.just(errorResponse);
            })
            .toFuture();
    }
} 