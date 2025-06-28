package techchamps.io.aiagent.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.List;
import java.util.Map;

public class GitHubMCPService {
    private static final Logger logger = LoggerFactory.getLogger(GitHubMCPService.class);
    private static final String GITHUB_API_BASE = "https://api.github.com";
    
    private final ObjectMapper objectMapper;
    private final WebClient webClient;
    private String currentToken;
    private String currentRepository;

    public GitHubMCPService() {
        this.objectMapper = new ObjectMapper();
        this.webClient = WebClient.builder()
                .baseUrl(GITHUB_API_BASE)
                .defaultHeader("Accept", "application/vnd.github.v3+json")
                .build();
    }

    public ObjectNode getToolsList() {
        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode tools = result.putArray("tools");
        
        // Tool: list_repositories
        tools.add(createToolDefinition(
            "list_repositories",
            "List all repositories for the authenticated user",
            Map.of(
                "token", Map.of("type", "string", "description", "GitHub Personal Access Token")
            )
        ));
        
        // Tool: connect_repository
        tools.add(createToolDefinition(
            "connect_repository",
            "Connect to a specific repository",
            Map.of(
                "token", Map.of("type", "string", "description", "GitHub Personal Access Token"),
                "repository_url", Map.of("type", "string", "description", "Repository URL (e.g., https://github.com/owner/repo)")
            )
        ));
        
        // Tool: list_files
        tools.add(createToolDefinition(
            "list_files",
            "List files in the connected repository",
            Map.of(
                "path", Map.of("type", "string", "description", "Directory path (optional, defaults to root)")
            )
        ));
        
        // Tool: read_file
        tools.add(createToolDefinition(
            "read_file",
            "Read the contents of a file",
            Map.of(
                "file_path", Map.of("type", "string", "description", "Path to the file")
            )
        ));
        
        // Tool: analyze_code
        tools.add(createToolDefinition(
            "analyze_code",
            "Analyze code in the repository",
            Map.of(
                "file_paths", Map.of("type", "array", "description", "Array of file paths to analyze"),
                "analysis_type", Map.of("type", "string", "description", "Type of analysis: 'structure', 'complexity', 'security', 'best_practices'")
            )
        ));
        
        // Tool: search_code
        tools.add(createToolDefinition(
            "search_code",
            "Search for code patterns in the repository",
            Map.of(
                "query", Map.of("type", "string", "description", "Search query"),
                "file_extension", Map.of("type", "string", "description", "File extension filter (optional)")
            )
        ));
        
        return result;
    }

    public ObjectNode getResourcesList() {
        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode resources = result.putArray("resources");
        
        if (currentRepository != null) {
            // Resource: repository_info
            resources.add(createResourceDefinition(
                "github://repository/info",
                "Repository information and metadata"
            ));
            
            // Resource: repository_structure
            resources.add(createResourceDefinition(
                "github://repository/structure",
                "Repository file structure and organization"
            ));
        }
        
        return result;
    }

    public ObjectNode callTool(String toolName, ObjectNode arguments) throws Exception {
        switch (toolName) {
            case "list_repositories":
                return listRepositories(arguments.get("token").asText());
            case "connect_repository":
                return connectRepository(
                    arguments.get("token").asText(),
                    arguments.get("repository_url").asText()
                );
            case "list_files":
                return listFiles(arguments.has("path") ? arguments.get("path").asText() : "");
            case "read_file":
                return readFile(arguments.get("file_path").asText());
            case "analyze_code":
                return analyzeCode(arguments);
            case "search_code":
                return searchCode(
                    arguments.get("query").asText(),
                    arguments.has("file_extension") ? arguments.get("file_extension").asText() : null
                );
            default:
                throw new IllegalArgumentException("Unknown tool: " + toolName);
        }
    }

    public ObjectNode readResource(String uri) throws Exception {
        if (currentRepository == null) {
            throw new IllegalStateException("No repository connected");
        }
        
        switch (uri) {
            case "github://repository/info":
                return getRepositoryInfo();
            case "github://repository/structure":
                return getRepositoryStructure();
            default:
                throw new IllegalArgumentException("Unknown resource: " + uri);
        }
    }

    private ObjectNode listRepositories(String token) {
        try {
            String response = webClient.get()
                    .uri("/user/repos?sort=updated&per_page=100")
                    .header("Authorization", "token " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            ObjectNode result = objectMapper.createObjectNode();
            result.set("repositories", objectMapper.readTree(response));
            result.put("count", objectMapper.readTree(response).size());
            return result;
        } catch (Exception e) {
            logger.error("Error listing repositories", e);
            throw new RuntimeException("Failed to list repositories: " + e.getMessage());
        }
    }

    private ObjectNode connectRepository(String token, String repositoryUrl) {
        try {
            this.currentToken = token;
            this.currentRepository = repositoryUrl.replace("https://github.com/", "");
            
            String[] parts = currentRepository.split("/");
            String owner = parts[0];
            String repo = parts[1];
            
            String response = webClient.get()
                    .uri("/repos/{owner}/{repo}", owner, repo)
                    .header("Authorization", "token " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            ObjectNode result = objectMapper.createObjectNode();
            result.put("success", true);
            result.put("repository", currentRepository);
            result.set("info", objectMapper.readTree(response));
            return result;
        } catch (Exception e) {
            logger.error("Error connecting to repository", e);
            throw new RuntimeException("Failed to connect to repository: " + e.getMessage());
        }
    }

    private ObjectNode listFiles(String path) {
        if (currentRepository == null) {
            throw new IllegalStateException("No repository connected");
        }
        
        try {
            String[] parts = currentRepository.split("/");
            String owner = parts[0];
            String repo = parts[1];
            
            String uri = "/repos/{owner}/{repo}/contents";
            if (!path.isEmpty()) {
                uri += "/" + path;
            }
            
            String response = webClient.get()
                    .uri(uri, owner, repo)
                    .header("Authorization", "token " + currentToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            ObjectNode result = objectMapper.createObjectNode();
            result.set("files", objectMapper.readTree(response));
            result.put("path", path.isEmpty() ? "/" : path);
            return result;
        } catch (Exception e) {
            logger.error("Error listing files", e);
            throw new RuntimeException("Failed to list files: " + e.getMessage());
        }
    }

    private ObjectNode readFile(String filePath) {
        if (currentRepository == null) {
            throw new IllegalStateException("No repository connected");
        }
        
        try {
            String[] parts = currentRepository.split("/");
            String owner = parts[0];
            String repo = parts[1];
            
            String response = webClient.get()
                    .uri("/repos/{owner}/{repo}/contents/{path}", owner, repo, filePath)
                    .header("Authorization", "token " + currentToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            ObjectNode fileInfo = (ObjectNode) objectMapper.readTree(response);
            String content = fileInfo.get("content").asText();
            String encoding = fileInfo.get("encoding").asText();
            
            if ("base64".equals(encoding)) {
                content = new String(Base64.getDecoder().decode(content));
            }
            
            ObjectNode result = objectMapper.createObjectNode();
            result.put("file_path", filePath);
            result.put("content", content);
            result.put("size", fileInfo.get("size").asInt());
            result.put("sha", fileInfo.get("sha").asText());
            return result;
        } catch (Exception e) {
            logger.error("Error reading file", e);
            throw new RuntimeException("Failed to read file: " + e.getMessage());
        }
    }

    private ObjectNode analyzeCode(ObjectNode arguments) {
        if (currentRepository == null) {
            throw new IllegalStateException("No repository connected");
        }
        
        try {
            ArrayNode filePaths = (ArrayNode) arguments.get("file_paths");
            String analysisType = arguments.get("analysis_type").asText();
            
            ObjectNode result = objectMapper.createObjectNode();
            result.put("analysis_type", analysisType);
            ArrayNode analysisResults = result.putArray("results");
            
            for (int i = 0; i < filePaths.size(); i++) {
                String filePath = filePaths.get(i).asText();
                ObjectNode fileResult = objectMapper.createObjectNode();
                fileResult.put("file_path", filePath);
                
                // Read file content
                ObjectNode fileContent = readFile(filePath);
                String content = fileContent.get("content").asText();
                
                // Perform analysis based on type
                switch (analysisType) {
                    case "structure":
                        fileResult.set("analysis", analyzeStructure(content, filePath));
                        break;
                    case "complexity":
                        fileResult.set("analysis", analyzeComplexity(content, filePath));
                        break;
                    case "security":
                        fileResult.set("analysis", analyzeSecurity(content, filePath));
                        break;
                    case "best_practices":
                        fileResult.set("analysis", analyzeBestPractices(content, filePath));
                        break;
                    default:
                        fileResult.put("error", "Unknown analysis type: " + analysisType);
                }
                
                analysisResults.add(fileResult);
            }
            
            return result;
        } catch (Exception e) {
            logger.error("Error analyzing code", e);
            throw new RuntimeException("Failed to analyze code: " + e.getMessage());
        }
    }

    private ObjectNode searchCode(String query, String fileExtension) {
        if (currentRepository == null) {
            throw new IllegalStateException("No repository connected");
        }
        
        try {
            String searchQuery = "repo:" + currentRepository + " " + query;
            if (fileExtension != null) {
                searchQuery += " extension:" + fileExtension;
            }
            
            String response = webClient.get()
                    .uri("/search/code?q={query}", searchQuery)
                    .header("Authorization", "token " + currentToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            ObjectNode result = objectMapper.createObjectNode();
            result.put("query", query);
            result.set("search_results", objectMapper.readTree(response));
            return result;
        } catch (Exception e) {
            logger.error("Error searching code", e);
            throw new RuntimeException("Failed to search code: " + e.getMessage());
        }
    }

    private ObjectNode getRepositoryInfo() {
        if (currentRepository == null) {
            throw new IllegalStateException("No repository connected");
        }
        
        try {
            String[] parts = currentRepository.split("/");
            String owner = parts[0];
            String repo = parts[1];
            
            String response = webClient.get()
                    .uri("/repos/{owner}/{repo}", owner, repo)
                    .header("Authorization", "token " + currentToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return (ObjectNode) objectMapper.readTree(response);
        } catch (Exception e) {
            logger.error("Error getting repository info", e);
            throw new RuntimeException("Failed to get repository info: " + e.getMessage());
        }
    }

    private ObjectNode getRepositoryStructure() {
        if (currentRepository == null) {
            throw new IllegalStateException("No repository connected");
        }
        
        try {
            ObjectNode result = objectMapper.createObjectNode();
            result.set("structure", listFiles("").get("files"));
            return result;
        } catch (Exception e) {
            logger.error("Error getting repository structure", e);
            throw new RuntimeException("Failed to get repository structure: " + e.getMessage());
        }
    }

    // Analysis helper methods
    private ObjectNode analyzeStructure(String content, String filePath) {
        ObjectNode analysis = objectMapper.createObjectNode();
        analysis.put("file_type", getFileExtension(filePath));
        analysis.put("line_count", content.split("\n").length);
        analysis.put("character_count", content.length());
        analysis.put("has_functions", content.contains("function") || content.contains("def ") || content.contains("public "));
        analysis.put("has_classes", content.contains("class "));
        return analysis;
    }

    private ObjectNode analyzeComplexity(String content, String filePath) {
        ObjectNode analysis = objectMapper.createObjectNode();
        String[] lines = content.split("\n");
        int complexity = 0;
        
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("if ") || trimmed.startsWith("for ") || 
                trimmed.startsWith("while ") || trimmed.startsWith("switch ") ||
                trimmed.contains(" && ") || trimmed.contains(" || ")) {
                complexity++;
            }
        }
        
        analysis.put("cyclomatic_complexity", complexity);
        analysis.put("complexity_level", complexity < 5 ? "low" : complexity < 10 ? "medium" : "high");
        return analysis;
    }

    private ObjectNode analyzeSecurity(String content, String filePath) {
        ObjectNode analysis = objectMapper.createObjectNode();
        ArrayNode issues = analysis.putArray("security_issues");
        
        // Check for common security issues
        if (content.contains("password") && content.contains("=")) {
            issues.add("Potential hardcoded password");
        }
        if (content.contains("SELECT *")) {
            issues.add("Potential SQL injection vulnerability");
        }
        if (content.contains("eval(")) {
            issues.add("Use of eval() - security risk");
        }
        
        analysis.put("security_score", 100 - (issues.size() * 20));
        return analysis;
    }

    private ObjectNode analyzeBestPractices(String content, String filePath) {
        ObjectNode analysis = objectMapper.createObjectNode();
        ArrayNode suggestions = analysis.putArray("suggestions");
        
        // Check for best practices
        if (content.length() > 1000) {
            suggestions.add("Consider breaking down large file into smaller modules");
        }
        if (content.contains("TODO") || content.contains("FIXME")) {
            suggestions.add("Remove TODO/FIXME comments before production");
        }
        if (content.contains("console.log")) {
            suggestions.add("Remove console.log statements for production");
        }
        
        return analysis;
    }

    private String getFileExtension(String filePath) {
        int lastDot = filePath.lastIndexOf('.');
        return lastDot > 0 ? filePath.substring(lastDot + 1) : "unknown";
    }

    private ObjectNode createToolDefinition(String name, String description, Map<String, Map<String, String>> parameters) {
        ObjectNode tool = objectMapper.createObjectNode();
        tool.put("name", name);
        tool.put("description", description);
        
        ObjectNode inputSchema = objectMapper.createObjectNode();
        inputSchema.put("type", "object");
        ObjectNode properties = inputSchema.putObject("properties");
        ArrayNode required = inputSchema.putArray("required");
        
        for (Map.Entry<String, Map<String, String>> param : parameters.entrySet()) {
            ObjectNode property = properties.putObject(param.getKey());
            Map<String, String> paramInfo = param.getValue();
            property.put("type", paramInfo.get("type"));
            property.put("description", paramInfo.get("description"));
            required.add(param.getKey());
        }
        
        tool.set("inputSchema", inputSchema);
        return tool;
    }

    private ObjectNode createResourceDefinition(String uri, String description) {
        ObjectNode resource = objectMapper.createObjectNode();
        resource.put("uri", uri);
        resource.put("description", description);
        resource.put("mimeType", "application/json");
        return resource;
    }
} 