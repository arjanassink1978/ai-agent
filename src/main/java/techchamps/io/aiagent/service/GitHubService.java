package techchamps.io.aiagent.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

@Service
public class GitHubService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${github.api.base-url:https://api.github.com}")
    private String githubApiBaseUrl;

    public GitHubService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> authenticateUser(String personalAccessToken) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Create headers with the token
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "token " + personalAccessToken);
            headers.set("Accept", "application/vnd.github.v3+json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // Get user information from GitHub API
            ResponseEntity<String> response = restTemplate.exchange(
                githubApiBaseUrl + "/user",
                HttpMethod.GET,
                entity,
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode userData = objectMapper.readTree(response.getBody());
                
                result.put("success", true);
                result.put("username", userData.get("login").asText());
                result.put("name", userData.get("name").asText());
                result.put("id", userData.get("id").asLong());
                result.put("avatar_url", userData.get("avatar_url").asText());
                
                return result;
            } else {
                result.put("success", false);
                result.put("error", "Authentication failed: " + response.getStatusCode());
                return result;
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Authentication failed: " + e.getMessage());
            return result;
        }
    }

    public Map<String, Object> getUserRepositories(String personalAccessToken) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Create headers with the token
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "token " + personalAccessToken);
            headers.set("Accept", "application/vnd.github.v3+json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // Get user repositories from GitHub API
            ResponseEntity<String> response = restTemplate.exchange(
                githubApiBaseUrl + "/user/repos?sort=updated&per_page=100",
                HttpMethod.GET,
                entity,
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode reposData = objectMapper.readTree(response.getBody());
                List<Map<String, Object>> repositories = new ArrayList<>();
                
                for (JsonNode repo : reposData) {
                    Map<String, Object> repoInfo = new HashMap<>();
                    repoInfo.put("name", repo.get("name").asText());
                    repoInfo.put("full_name", repo.get("full_name").asText());
                    repoInfo.put("description", repo.get("description") != null ? repo.get("description").asText() : "");
                    repoInfo.put("private", repo.get("private").asBoolean());
                    repoInfo.put("html_url", repo.get("html_url").asText());
                    repoInfo.put("clone_url", repo.get("clone_url").asText());
                    repoInfo.put("language", repo.get("language") != null ? repo.get("language").asText() : "Unknown");
                    repoInfo.put("updated_at", repo.get("updated_at").asText());
                    
                    repositories.add(repoInfo);
                }
                
                result.put("success", true);
                result.put("repositories", repositories);
                return result;
            } else {
                result.put("success", false);
                result.put("error", "Failed to fetch repositories: " + response.getStatusCode());
                return result;
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Failed to fetch repositories: " + e.getMessage());
            return result;
        }
    }

    public Map<String, Object> connectToRepository(String personalAccessToken, String repositoryUrl) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Extract owner and repo name from URL
            String[] urlParts = repositoryUrl.replace("https://github.com/", "").split("/");
            if (urlParts.length < 2) {
                result.put("success", false);
                result.put("error", "Invalid repository URL format");
                return result;
            }
            
            String owner = urlParts[0];
            String repo = urlParts[1];
            
            // Create headers with the token
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "token " + personalAccessToken);
            headers.set("Accept", "application/vnd.github.v3+json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // Get repository contents
            ResponseEntity<String> response = restTemplate.exchange(
                githubApiBaseUrl + "/repos/" + owner + "/" + repo + "/contents",
                HttpMethod.GET,
                entity,
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode contentsData = objectMapper.readTree(response.getBody());
                List<String> files = new ArrayList<>();
                
                // Recursively get all files
                extractFiles(contentsData, files, owner, repo, personalAccessToken, "");
                
                result.put("success", true);
                result.put("files", files);
                result.put("repository", owner + "/" + repo);
                return result;
            } else {
                result.put("success", false);
                result.put("error", "Failed to connect to repository: " + response.getStatusCode());
                return result;
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Failed to connect to repository: " + e.getMessage());
            return result;
        }
    }

    private void extractFiles(JsonNode contents, List<String> files, String owner, String repo, String token, String path) {
        for (JsonNode item : contents) {
            String name = item.get("name").asText();
            String type = item.get("type").asText();
            String currentPath = path.isEmpty() ? name : path + "/" + name;
            
            if ("file".equals(type)) {
                // Only include common code files
                if (isCodeFile(name)) {
                    files.add(currentPath);
                }
            } else if ("dir".equals(type)) {
                // Recursively get contents of subdirectories
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("Authorization", "token " + token);
                    headers.set("Accept", "application/vnd.github.v3+json");
                    
                    HttpEntity<String> entity = new HttpEntity<>(headers);
                    
                    ResponseEntity<String> response = restTemplate.exchange(
                        githubApiBaseUrl + "/repos/" + owner + "/" + repo + "/contents/" + currentPath,
                        HttpMethod.GET,
                        entity,
                        String.class
                    );
                    
                    if (response.getStatusCode() == HttpStatus.OK) {
                        JsonNode subContents = objectMapper.readTree(response.getBody());
                        extractFiles(subContents, files, owner, repo, token, currentPath);
                    }
                } catch (Exception e) {
                    // Skip directories that can't be accessed
                    System.err.println("Could not access directory: " + currentPath + " - " + e.getMessage());
                }
            }
        }
    }

    private boolean isCodeFile(String fileName) {
        String[] codeExtensions = {
            ".java", ".js", ".ts", ".jsx", ".tsx", ".py", ".cpp", ".c", ".h", ".hpp",
            ".cs", ".php", ".rb", ".go", ".rs", ".swift", ".kt", ".scala", ".clj",
            ".html", ".css", ".scss", ".sass", ".less", ".xml", ".json", ".yaml", ".yml",
            ".md", ".txt", ".sql", ".sh", ".bash", ".zsh", ".fish", ".ps1", ".bat",
            ".dockerfile", ".dockerignore", ".gitignore", ".env", ".properties",
            ".gradle", ".mvn", ".pom", ".sbt", ".cabal", ".hs", ".ml", ".fs", ".fsx"
        };
        
        String lowerFileName = fileName.toLowerCase();
        for (String ext : codeExtensions) {
            if (lowerFileName.endsWith(ext)) {
                return true;
            }
        }
        
        // Include common configuration files without extensions
        String[] configFiles = {
            "dockerfile", "makefile", "readme", "license", "changelog", "contributing",
            "package.json", "package-lock.json", "yarn.lock", "composer.json", "pom.xml",
            "build.gradle", "build.sbt", "cargo.toml", "go.mod", "requirements.txt",
            "gemfile", "rakefile", "gruntfile", "gulpfile", "webpack.config", "tsconfig",
            "babel.config", "eslintrc", "prettierrc", "gitignore", "dockerignore"
        };
        
        for (String configFile : configFiles) {
            if (lowerFileName.equals(configFile) || lowerFileName.startsWith(configFile + ".")) {
                return true;
            }
        }
        
        return false;
    }

    public String getFileContent(String personalAccessToken, String repositoryUrl, String filePath) {
        try {
            // Extract owner and repo name from URL
            String[] urlParts = repositoryUrl.replace("https://github.com/", "").split("/");
            if (urlParts.length < 2) {
                return null;
            }
            
            String owner = urlParts[0];
            String repo = urlParts[1];
            
            // Create headers with the token
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "token " + personalAccessToken);
            headers.set("Accept", "application/vnd.github.v3+json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // Get file content
            ResponseEntity<String> response = restTemplate.exchange(
                githubApiBaseUrl + "/repos/" + owner + "/" + repo + "/contents/" + filePath,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode fileData = objectMapper.readTree(response.getBody());
                String content = fileData.get("content").asText();
                String encoding = fileData.get("encoding").asText();
                
                if ("base64".equals(encoding)) {
                    return new String(java.util.Base64.getDecoder().decode(content));
                } else {
                    return content;
                }
            }
            
            return null;
            
        } catch (Exception e) {
            System.err.println("Error getting file content: " + e.getMessage());
            return null;
        }
    }
} 