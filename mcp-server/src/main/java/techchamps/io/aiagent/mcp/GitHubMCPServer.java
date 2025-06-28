package techchamps.io.aiagent.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@RestController
@RequestMapping("/api/mcp")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "http://localhost:8080"})
public class GitHubMCPServer {
    
    private static final Logger logger = LoggerFactory.getLogger(GitHubMCPServer.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient = WebClient.builder().build();
    private final Map<String, String> connectedRepos = new ConcurrentHashMap<>();
    
    public static void main(String[] args) {
        SpringApplication.run(GitHubMCPServer.class, args);
    }
    
    @GetMapping("/status")
    public Mono<ObjectNode> getStatus() {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("status", "running");
        response.put("connectedRepos", connectedRepos.size());
        return Mono.just(response);
    }
    
    @PostMapping("/repositories")
    public Mono<ObjectNode> listRepositories(@RequestBody ObjectNode request) {
        try {
            String token = request.get("token").asText();
            String username = request.get("username").asText();
            
            logger.info("Listing repositories for user: {}", username);
            
            return webClient.get()
                .uri("https://api.github.com/users/" + username + "/repos")
                .header("Authorization", "token " + token)
                .header("Accept", "application/vnd.github.v3+json")
                .retrieve()
                .bodyToMono(String.class)
                .map(jsonResponse -> {
                    try {
                        JsonNode repos = objectMapper.readTree(jsonResponse);
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
                        
                        ObjectNode response = objectMapper.createObjectNode();
                        response.put("success", true);
                        response.set("repositories", repoList);
                        return response;
                    } catch (Exception e) {
                        logger.error("Error parsing GitHub response", e);
                        ObjectNode errorResponse = objectMapper.createObjectNode();
                        errorResponse.put("success", false);
                        errorResponse.put("error", "Failed to parse repositories");
                        return errorResponse;
                    }
                })
                .onErrorResume(e -> {
                    logger.error("Error fetching repositories", e);
                    ObjectNode errorResponse = objectMapper.createObjectNode();
                    errorResponse.put("success", false);
                    errorResponse.put("error", "Failed to fetch repositories: " + e.getMessage());
                    return Mono.just(errorResponse);
                });
                
        } catch (Exception e) {
            logger.error("Error processing repository list request", e);
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid request format");
            return Mono.just(errorResponse);
        }
    }
    
    @PostMapping("/connect")
    public Mono<ObjectNode> connectToRepository(@RequestBody ObjectNode request) {
        try {
            String token = request.get("token").asText();
            String repoName = request.get("repository").asText();
            
            logger.info("Connecting to repository: {}", repoName);
            
            // Store the connection
            connectedRepos.put(repoName, token);
            
            ObjectNode response = objectMapper.createObjectNode();
            response.put("success", true);
            response.put("message", "Connected to repository: " + repoName);
            response.put("repository", repoName);
            
            return Mono.just(response);
            
        } catch (Exception e) {
            logger.error("Error connecting to repository", e);
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to connect: " + e.getMessage());
            return Mono.just(errorResponse);
        }
    }
    
    @PostMapping("/issues")
    public Mono<ObjectNode> createIssue(@RequestBody ObjectNode request) {
        try {
            String repoName = request.get("repository").asText();
            String title = request.get("title").asText();
            String body = request.get("body").asText();
            
            String token = connectedRepos.get(repoName);
            if (token == null) {
                ObjectNode errorResponse = objectMapper.createObjectNode();
                errorResponse.put("success", false);
                errorResponse.put("error", "Not connected to repository: " + repoName);
                return Mono.just(errorResponse);
            }
            
            logger.info("Creating issue in repository: {}", repoName);
            
            ObjectNode issueData = objectMapper.createObjectNode();
            issueData.put("title", title);
            issueData.put("body", body);
            
            return webClient.post()
                .uri("https://api.github.com/repos/" + repoName + "/issues")
                .header("Authorization", "token " + token)
                .header("Accept", "application/vnd.github.v3+json")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(issueData.toString())
                .retrieve()
                .bodyToMono(String.class)
                .map(jsonResponse -> {
                    try {
                        JsonNode issue = objectMapper.readTree(jsonResponse);
                        ObjectNode response = objectMapper.createObjectNode();
                        response.put("success", true);
                        response.put("message", "Issue created successfully");
                        response.put("issueNumber", issue.get("number").asInt());
                        response.put("issueUrl", issue.get("html_url").asText());
                        return response;
                    } catch (Exception e) {
                        logger.error("Error parsing issue response", e);
                        ObjectNode errorResponse = objectMapper.createObjectNode();
                        errorResponse.put("success", false);
                        errorResponse.put("error", "Failed to parse issue response");
                        return errorResponse;
                    }
                })
                .onErrorResume(e -> {
                    logger.error("Error creating issue", e);
                    ObjectNode errorResponse = objectMapper.createObjectNode();
                    errorResponse.put("success", false);
                    errorResponse.put("error", "Failed to create issue: " + e.getMessage());
                    return Mono.just(errorResponse);
                });
                
        } catch (Exception e) {
            logger.error("Error processing issue creation request", e);
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid request format");
            return Mono.just(errorResponse);
        }
    }
    
    @GetMapping("/files/{repoName}/**")
    public Mono<ObjectNode> getFileContent(@PathVariable String repoName, 
                                          @RequestParam String path,
                                          @RequestParam String token) {
        try {
            logger.info("Getting file content from repository: {} path: {}", repoName, path);
            
            return webClient.get()
                .uri("https://api.github.com/repos/" + repoName + "/contents/" + path)
                .header("Authorization", "token " + token)
                .header("Accept", "application/vnd.github.v3+json")
                .retrieve()
                .bodyToMono(String.class)
                .map(jsonResponse -> {
                    try {
                        JsonNode file = objectMapper.readTree(jsonResponse);
                        ObjectNode response = objectMapper.createObjectNode();
                        response.put("success", true);
                        response.put("content", file.get("content").asText());
                        response.put("encoding", file.get("encoding").asText());
                        response.put("path", file.get("path").asText());
                        return response;
                    } catch (Exception e) {
                        logger.error("Error parsing file response", e);
                        ObjectNode errorResponse = objectMapper.createObjectNode();
                        errorResponse.put("success", false);
                        errorResponse.put("error", "Failed to parse file response");
                        return errorResponse;
                    }
                })
                .onErrorResume(e -> {
                    logger.error("Error getting file content", e);
                    ObjectNode errorResponse = objectMapper.createObjectNode();
                    errorResponse.put("success", false);
                    errorResponse.put("error", "Failed to get file content: " + e.getMessage());
                    return Mono.just(errorResponse);
                });
                
        } catch (Exception e) {
            logger.error("Error processing file request", e);
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid request format");
            return Mono.just(errorResponse);
        }
    }
} 