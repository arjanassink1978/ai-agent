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
    
    // Use official GitHub MCP server
    private static final String OFFICIAL_MCP_URL = "https://api.githubcopilot.com/mcp";
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient = WebClient.builder().build();
    
    public CompletableFuture<ObjectNode> getMCPStatus() {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("status", "connected");
        response.put("server", "Official GitHub MCP Server");
        response.put("url", OFFICIAL_MCP_URL);
        return CompletableFuture.completedFuture(response);
    }
    
    public CompletableFuture<ObjectNode> listRepositories(String token, String username) {
        // For now, keep the existing GitHub API call for repository listing
        // This could be replaced with MCP call if needed
        ObjectNode request = objectMapper.createObjectNode();
        request.put("token", token);
        request.put("username", username);
        
        return webClient.get()
            .uri("https://api.github.com/users/" + username + "/repos")
            .header("Authorization", "token " + token)
            .header("Accept", "application/vnd.github.v3+json")
            .retrieve()
            .bodyToMono(String.class)
            .map(response -> {
                try {
                    JsonNode repos = objectMapper.readTree(response);
                    ArrayNode repoList = objectMapper.createArrayNode();
                    
                    for (JsonNode repo : repos) {
                        ObjectNode repoInfo = objectMapper.createObjectNode();
                        repoInfo.put("id", repo.get("id").asInt());
                        repoInfo.put("name", repo.get("name").asText());
                        repoInfo.put("fullName", repo.get("full_name").asText());
                        repoInfo.put("description", repo.has("description") ? repo.get("description").asText() : "");
                        repoInfo.put("url", repo.get("html_url").asText());
                        repoInfo.put("private", repo.get("private").asBoolean());
                        repoList.add(repoInfo);
                    }
                    
                    ObjectNode responseNode = objectMapper.createObjectNode();
                    responseNode.put("success", true);
                    responseNode.set("repositories", repoList);
                    return responseNode;
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
        ObjectNode response = objectMapper.createObjectNode();
        response.put("success", true);
        response.put("message", "Connected to repository: " + repository);
        response.put("repository", repository);
        return CompletableFuture.completedFuture(response);
    }
    
    public CompletableFuture<ObjectNode> execute(ObjectNode payload) {
        String message = payload.get("message").asText();
        String repository = payload.get("repository").asText();
        String token = payload.get("token").asText();
        
        logger.info("Processing request: {}", message);
        
        // Simple keyword-based routing to GitHub API
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("list") && lowerMessage.contains("issue")) {
            return listIssues(repository, token);
        } else if (lowerMessage.contains("create") && lowerMessage.contains("issue")) {
            return createIssue(message, repository, token);
        } else {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("success", true);
            response.put("message", "I understand: " + message + "\n\nTry:\n- 'List all open issues'\n- 'Create an issue about the login bug'");
            return CompletableFuture.completedFuture(response);
        }
    }
    
    private CompletableFuture<ObjectNode> listIssues(String repository, String token) {
        return webClient.get()
            .uri("https://api.github.com/repos/" + repository + "/issues?state=open")
            .header("Authorization", "token " + token)
            .header("Accept", "application/vnd.github.v3+json")
            .retrieve()
            .bodyToMono(String.class)
            .map(jsonResponse -> {
                try {
                    JsonNode issues = objectMapper.readTree(jsonResponse);
                    StringBuilder responseText = new StringBuilder();
                    responseText.append("📋 **Open Issues:**\n\n");
                    
                    if (issues.size() == 0) {
                        responseText.append("No open issues found.");
                    } else {
                        for (JsonNode issue : issues) {
                            responseText.append("🔸 **#").append(issue.get("number").asText())
                                      .append("** ").append(issue.get("title").asText())
                                      .append("\n");
                            responseText.append("   👤 ").append(issue.get("user").get("login").asText())
                                      .append(" • ").append(issue.get("created_at").asText().substring(0, 10))
                                      .append("\n");
                            responseText.append("   🔗 ").append(issue.get("html_url").asText()).append("\n\n");
                        }
                    }
                    
                    ObjectNode response = objectMapper.createObjectNode();
                    response.put("success", true);
                    response.put("message", responseText.toString());
                    return response;
                } catch (Exception e) {
                    logger.error("Error parsing issues response", e);
                    ObjectNode errorResponse = objectMapper.createObjectNode();
                    errorResponse.put("success", false);
                    errorResponse.put("error", "Failed to parse issues response");
                    return errorResponse;
                }
            })
            .onErrorResume(e -> {
                logger.error("Error listing issues", e);
                ObjectNode errorResponse = objectMapper.createObjectNode();
                errorResponse.put("success", false);
                errorResponse.put("error", "Failed to list issues: " + e.getMessage());
                return Mono.just(errorResponse);
            })
            .toFuture();
    }
    
    private CompletableFuture<ObjectNode> createIssue(String message, String repository, String token) {
        String title = message.replaceAll("(?i).*create.*issue.*about\\s+", "")
                             .replaceAll("(?i).*create.*issue.*for\\s+", "")
                             .replaceAll("(?i).*create.*issue.*", "")
                             .trim();
        
        if (title.isEmpty()) {
            title = "Issue created via AI Agent";
        }
        
        ObjectNode issueData = objectMapper.createObjectNode();
        issueData.put("title", title);
        issueData.put("body", "Issue created via AI Agent\n\nOriginal request: " + message);
        
        return webClient.post()
            .uri("https://api.github.com/repos/" + repository + "/issues")
            .header("Authorization", "token " + token)
            .header("Accept", "application/vnd.github.v3+json")
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .bodyValue(issueData.toString())
            .retrieve()
            .bodyToMono(String.class)
            .map(jsonResponse -> {
                try {
                    JsonNode issue = objectMapper.readTree(jsonResponse);
                    ObjectNode response = objectMapper.createObjectNode();
                    response.put("success", true);
                    response.put("message", "✅ **Issue created successfully!**\n\n" +
                                          "🔸 **#" + issue.get("number").asText() + "** " + issue.get("title").asText() + "\n" +
                                          "🔗 " + issue.get("html_url").asText());
                    return response;
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
} 