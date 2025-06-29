package techchamps.io.aiagent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import techchamps.io.aiagent.model.ChatRequest;
import techchamps.io.aiagent.model.ChatResponse;

import java.util.concurrent.CompletableFuture;

@Service
public class MCPService {
    
    private static final Logger logger = LoggerFactory.getLogger(MCPService.class);
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient = WebClient.builder().build();
    
    @Autowired
    private AiService aiService;
    
    public CompletableFuture<ObjectNode> getMCPStatus() {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("status", "connected");
        response.put("server", "AI-Powered GitHub Assistant");
        return CompletableFuture.completedFuture(response);
    }
    
    public CompletableFuture<ObjectNode> listRepositories(String token, String username) {
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
        
        // Use LLM to understand what the user wants and generate the API call
        String systemPrompt = String.format(
            "You are a GitHub API assistant. The user wants to do something with repository: %s. " +
            "Respond with ONLY a JSON object in this exact format:\n" +
            "{\n" +
            "  \"method\": \"GET|POST|PUT|DELETE\",\n" +
            "  \"endpoint\": \"/repos/{owner}/{repo}/...\",\n" +
            "  \"data\": { /* POST/PUT data if needed */ },\n" +
            "  \"description\": \"What this operation does\"\n" +
            "}\n" +
            "Examples:\n" +
            "- List issues: {\"method\":\"GET\",\"endpoint\":\"/repos/%s/issues?state=open\",\"description\":\"List open issues\"}\n" +
            "- Create issue: {\"method\":\"POST\",\"endpoint\":\"/repos/%s/issues\",\"data\":{\"title\":\"Bug fix\",\"body\":\"Description\"},\"description\":\"Create new issue\"}\n" +
            "- List PRs: {\"method\":\"GET\",\"endpoint\":\"/repos/%s/pulls?state=open\",\"description\":\"List open pull requests\"}\n" +
            "- List branches: {\"method\":\"GET\",\"endpoint\":\"/repos/%s/branches\",\"description\":\"List all branches\"}\n" +
            "User request: %s",
            repository, repository, repository, repository, repository, message
        );
        
        try {
            ChatRequest chatRequest = new ChatRequest();
            chatRequest.setMessage(systemPrompt);
            
            ChatResponse aiResponse = aiService.chat(chatRequest);
            String responseText = aiResponse.getMessage().trim();
            
            // Try to parse the JSON response from the LLM
            try {
                JsonNode apiCall = objectMapper.readTree(responseText);
                return executeGitHubApiCall(apiCall, repository, token);
            } catch (Exception e) {
                logger.error("Failed to parse LLM response as JSON", e);
                ObjectNode response = objectMapper.createObjectNode();
                response.put("success", true);
                response.put("message", "I'm not sure what you'd like me to do. Try:\n" +
                    "- 'List open issues'\n" +
                    "- 'Create an issue about the login bug'\n" +
                    "- 'Show pull requests'\n" +
                    "- 'List branches'\n" +
                    "- 'Show recent commits'");
                return CompletableFuture.completedFuture(response);
            }
            
        } catch (Exception e) {
            logger.error("Error processing request", e);
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to process request: " + e.getMessage());
            return CompletableFuture.completedFuture(errorResponse);
        }
    }
    
    private CompletableFuture<ObjectNode> executeGitHubApiCall(JsonNode apiCall, String repository, String token) {
        String method = apiCall.get("method").asText();
        String endpoint = apiCall.get("endpoint").asText();
        String description = apiCall.get("description").asText();
        
        // Replace {owner}/{repo} with actual repository
        endpoint = endpoint.replace("{owner}/{repo}", repository);
        
        WebClient.RequestBodySpec request = webClient.method(org.springframework.http.HttpMethod.valueOf(method))
            .uri("https://api.github.com" + endpoint)
            .header("Authorization", "token " + token)
            .header("Accept", "application/vnd.github.v3+json");
        
        // Add data for POST/PUT requests
        if ((method.equals("POST") || method.equals("PUT")) && apiCall.has("data")) {
            request.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                   .bodyValue(apiCall.get("data").toString());
        }
        
        return request.retrieve()
            .bodyToMono(String.class)
            .map(jsonResponse -> {
                try {
                    JsonNode data = objectMapper.readTree(jsonResponse);
                    String formattedResponse = formatGitHubResponse(data, description);
                    
                    ObjectNode response = objectMapper.createObjectNode();
                    response.put("success", true);
                    response.put("message", formattedResponse);
                    return response;
                } catch (Exception e) {
                    logger.error("Error parsing GitHub API response", e);
                    ObjectNode errorResponse = objectMapper.createObjectNode();
                    errorResponse.put("success", false);
                    errorResponse.put("error", "Failed to parse response: " + e.getMessage());
                    return errorResponse;
                }
            })
            .onErrorResume(e -> {
                logger.error("Error executing GitHub API call", e);
                ObjectNode errorResponse = objectMapper.createObjectNode();
                errorResponse.put("success", false);
                errorResponse.put("error", "Failed to execute operation: " + e.getMessage());
                return Mono.just(errorResponse);
            })
            .toFuture();
    }
    
    private String formatGitHubResponse(JsonNode data, String description) {
        // If it's a single item (not an array), and the description matches a known action, return a natural language response
        if (!data.isArray() && data.has("number")) {
            String action = description.toLowerCase();
            String number = data.get("number").asText();
            String title = data.has("title") ? data.get("title").asText() : "";
            String url = data.has("html_url") ? data.get("html_url").asText() : null;
            String type = "item";
            if (action.contains("issue")) type = "issue";
            else if (action.contains("pull request") || action.contains("pr")) type = "pull request";

            StringBuilder response = new StringBuilder();
            if (action.contains("close") && type.equals("issue")) {
                response.append("I closed issue #").append(number).append(" successfully!");
            } else if (action.contains("open") && type.equals("issue")) {
                response.append("I opened issue #").append(number).append(": ").append(title);
            } else if (action.contains("create") && type.equals("issue")) {
                response.append("I created issue #").append(number).append(": ").append(title);
            } else if (action.contains("reopen") && type.equals("issue")) {
                response.append("I reopened issue #").append(number).append(" successfully!");
            } else if (action.contains("merge") && type.equals("pull request")) {
                response.append("I merged pull request #").append(number).append(": ").append(title);
            } else if (action.contains("close") && type.equals("pull request")) {
                response.append("I closed pull request #").append(number).append(": ").append(title);
            } else if (action.contains("open") && type.equals("pull request")) {
                response.append("I opened pull request #").append(number).append(": ").append(title);
            } else if (action.contains("create") && type.equals("pull request")) {
                response.append("I created pull request #").append(number).append(": ").append(title);
            } else {
                // Fallback to a generic message
                response.append("I performed the following action: ").append(description);
                if (!title.isEmpty()) {
                    response.append(" (Title: ").append(title).append(")");
                }
            }
            if (url != null) {
                response.append("\n").append(url);
            }
            return response.toString();
        }
        // For lists and other responses, keep the current markdown format
        StringBuilder response = new StringBuilder();
        response.append("✅ **").append(description).append(":**\n\n");
        if (data.isArray()) {
            if (data.size() == 0) {
                response.append("No items found.");
            } else {
                for (JsonNode item : data) {
                    if (item.has("number")) {
                        // Issue or PR
                        response.append("🔸 **#").append(item.get("number").asText())
                              .append("** ").append(item.get("title").asText()).append("\n");
                        if (item.has("user")) {
                            response.append("   👤 ").append(item.get("user").get("login").asText());
                        }
                        if (item.has("created_at")) {
                            response.append(" • ").append(item.get("created_at").asText().substring(0, 10));
                        }
                        response.append("\n");
                        if (item.has("html_url")) {
                            response.append("   🔗 ").append(item.get("html_url").asText()).append("\n");
                        }
                        response.append("\n");
                    } else if (item.has("name")) {
                        // Branch
                        response.append("🌿 **").append(item.get("name").asText()).append("**\n");
                        if (item.has("commit")) {
                            response.append("   📝 ").append(item.get("commit").get("sha").asText().substring(0, 7));
                            if (item.get("commit").has("commit")) {
                                response.append(" • ").append(item.get("commit").get("commit").get("message").asText());
                            }
                            response.append("\n");
                        }
                        response.append("\n");
                    } else if (item.has("sha")) {
                        // Commit
                        response.append("🔸 **").append(item.get("sha").asText().substring(0, 7)).append("**\n");
                        if (item.has("commit")) {
                            response.append("   📝 ").append(item.get("commit").get("message").asText()).append("\n");
                            if (item.get("commit").has("author")) {
                                response.append("   👤 ").append(item.get("commit").get("author").get("name").asText());
                                if (item.get("commit").get("author").has("date")) {
                                    response.append(" • ").append(item.get("commit").get("author").get("date").asText().substring(0, 10));
                                }
                                response.append("\n");
                            }
                        }
                        response.append("\n");
                    }
                }
            }
        } else {
            // Single item fallback (not an issue/PR)
            if (data.has("number")) {
                response.append("🔸 **#").append(data.get("number").asText())
                      .append("** ").append(data.get("title").asText()).append("\n");
                if (data.has("html_url")) {
                    response.append("🔗 ").append(data.get("html_url").asText());
                }
            }
        }
        return response.toString();
    }
} 