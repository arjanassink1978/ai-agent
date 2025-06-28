package techchamps.io.aiagent.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.kohsuke.github.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@RestController
@RequestMapping("/mcp")
public class GitHubMCPServer {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, GitHub> githubClients = new ConcurrentHashMap<>();
    private final Map<String, String> userTokens = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        SpringApplication.run(GitHubMCPServer.class, args);
    }

    // MCP Protocol Handlers

    @PostMapping("/initialize")
    public ResponseEntity<Map<String, Object>> initialize(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        response.put("protocolVersion", "2024-11-05");
        response.put("capabilities", getCapabilities());
        response.put("serverInfo", getServerInfo());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/tools/list")
    public ResponseEntity<Map<String, Object>> listTools(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        response.put("tools", getAvailableTools());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/tools/call")
    public ResponseEntity<Map<String, Object>> callTool(@RequestBody Map<String, Object> request) {
        try {
            String toolName = (String) request.get("name");
            Map<String, Object> arguments = (Map<String, Object>) request.get("arguments");
            String callId = (String) request.get("callId");

            Object result = executeTool(toolName, arguments);

            Map<String, Object> response = new HashMap<>();
            response.put("content", Arrays.asList(Map.of(
                "type", "text",
                "text", objectMapper.writeValueAsString(result)
            )));
            response.put("isError", false);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("content", Arrays.asList(Map.of(
                "type", "text",
                "text", "Error: " + e.getMessage()
            )));
            response.put("isError", true);
            return ResponseEntity.ok(response);
        }
    }

    // GitHub API Tools

    @PostMapping("/github/authenticate")
    public ResponseEntity<Map<String, Object>> authenticateGitHub(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        try {
            GitHub github = GitHub.connectUsingOAuth(token);
            String username = github.getMyself().getLogin();
            githubClients.put(username, github);
            userTokens.put(username, token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("username", username);
            response.put("message", "Successfully authenticated with GitHub");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Authentication failed: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/github/repositories")
    public ResponseEntity<Map<String, Object>> getUserRepositories(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        GitHub github = githubClients.get(username);
        
        if (github == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "User not authenticated");
            return ResponseEntity.ok(response);
        }

        try {
            List<Map<String, Object>> repos = new ArrayList<>();
            for (GHRepository repo : github.getMyself().listRepositories()) {
                Map<String, Object> repoInfo = new HashMap<>();
                repoInfo.put("name", repo.getName());
                repoInfo.put("fullName", repo.getFullName());
                repoInfo.put("description", repo.getDescription());
                repoInfo.put("private", repo.isPrivate());
                repoInfo.put("htmlUrl", repo.getHtmlUrl());
                repoInfo.put("cloneUrl", repo.getHttpTransportUrl());
                repoInfo.put("language", repo.getLanguage());
                repoInfo.put("updatedAt", repo.getUpdatedAt().toString());
                repos.add(repoInfo);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("repositories", repos);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to fetch repositories: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/github/repository/contents")
    public ResponseEntity<Map<String, Object>> getRepositoryContents(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String repoFullName = request.get("repository");
        String path = request.getOrDefault("path", "");
        
        GitHub github = githubClients.get(username);
        if (github == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "User not authenticated");
            return ResponseEntity.ok(response);
        }

        try {
            GHRepository repo = github.getRepository(repoFullName);
            List<Map<String, Object>> contents = new ArrayList<>();
            
            if (path.isEmpty()) {
                for (GHContent content : repo.getDirectoryContent("")) {
                    contents.add(contentToMap(content));
                }
            } else {
                GHContent content = repo.getFileContent(path);
                if (content.isFile()) {
                    contents.add(contentToMap(content));
                } else {
                    for (GHContent item : repo.getDirectoryContent(path)) {
                        contents.add(contentToMap(item));
                    }
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contents", contents);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to get repository contents: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/github/repository/file")
    public ResponseEntity<Map<String, Object>> getFileContent(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String repoFullName = request.get("repository");
        String filePath = request.get("filePath");
        
        GitHub github = githubClients.get(username);
        if (github == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "User not authenticated");
            return ResponseEntity.ok(response);
        }

        try {
            GHRepository repo = github.getRepository(repoFullName);
            GHContent content = repo.getFileContent(filePath);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("content", content.getContent());
            response.put("encoding", content.getEncoding());
            response.put("size", content.getSize());
            response.put("sha", content.getSha());
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to get file content: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/github/repository/issues")
    public ResponseEntity<Map<String, Object>> getIssues(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String repoFullName = request.get("repository");
        String state = request.getOrDefault("state", "open");
        
        GitHub github = githubClients.get(username);
        if (github == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "User not authenticated");
            return ResponseEntity.ok(response);
        }

        try {
            GHRepository repo = github.getRepository(repoFullName);
            List<Map<String, Object>> issues = new ArrayList<>();
            
            for (GHIssue issue : repo.getIssues(GHIssueState.valueOf(state.toUpperCase()))) {
                Map<String, Object> issueInfo = new HashMap<>();
                issueInfo.put("number", issue.getNumber());
                issueInfo.put("title", issue.getTitle());
                issueInfo.put("body", issue.getBody());
                issueInfo.put("state", issue.getState().name());
                issueInfo.put("createdAt", issue.getCreatedAt().toString());
                issueInfo.put("updatedAt", issue.getUpdatedAt().toString());
                issueInfo.put("user", issue.getUser().getLogin());
                issues.add(issueInfo);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("issues", issues);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to get issues: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/github/repository/create-issue")
    public ResponseEntity<Map<String, Object>> createIssue(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        String repoFullName = (String) request.get("repository");
        String title = (String) request.get("title");
        String body = (String) request.get("body");
        List<String> labels = (List<String>) request.getOrDefault("labels", new ArrayList<>());
        
        GitHub github = githubClients.get(username);
        if (github == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "User not authenticated");
            return ResponseEntity.ok(response);
        }

        try {
            GHRepository repo = github.getRepository(repoFullName);
            GHIssueBuilder builder = repo.createIssue(title);
            builder.body(body);
            GHIssue issue = builder.create();
            if (!labels.isEmpty()) {
                issue.addLabels(labels.toArray(new String[0]));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("issueNumber", issue.getNumber());
            response.put("issueUrl", issue.getHtmlUrl());
            response.put("title", issue.getTitle());
            response.put("message", "Issue #" + issue.getNumber() + " created successfully");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to create issue: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/github/repository/pull-requests")
    public ResponseEntity<Map<String, Object>> getPullRequests(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String repoFullName = request.get("repository");
        String state = request.getOrDefault("state", "open");
        
        GitHub github = githubClients.get(username);
        if (github == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "User not authenticated");
            return ResponseEntity.ok(response);
        }

        try {
            GHRepository repo = github.getRepository(repoFullName);
            List<Map<String, Object>> pullRequests = new ArrayList<>();
            
            for (GHPullRequest pr : repo.queryPullRequests().state(GHIssueState.valueOf(state.toUpperCase())).list()) {
                Map<String, Object> prInfo = new HashMap<>();
                prInfo.put("number", pr.getNumber());
                prInfo.put("title", pr.getTitle());
                prInfo.put("body", pr.getBody());
                prInfo.put("state", pr.getState().name());
                prInfo.put("createdAt", pr.getCreatedAt().toString());
                prInfo.put("updatedAt", pr.getUpdatedAt().toString());
                prInfo.put("user", pr.getUser().getLogin());
                prInfo.put("headBranch", pr.getHead().getRef());
                prInfo.put("baseBranch", pr.getBase().getRef());
                pullRequests.add(prInfo);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("pullRequests", pullRequests);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to get pull requests: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/github/repository/branches")
    public ResponseEntity<Map<String, Object>> getBranches(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String repoFullName = request.get("repository");
        
        GitHub github = githubClients.get(username);
        if (github == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "User not authenticated");
            return ResponseEntity.ok(response);
        }

        try {
            GHRepository repo = github.getRepository(repoFullName);
            List<Map<String, Object>> branches = new ArrayList<>();
            
            for (GHBranch branch : repo.getBranches().values()) {
                Map<String, Object> branchInfo = new HashMap<>();
                branchInfo.put("name", branch.getName());
                branchInfo.put("sha", branch.getSHA1());
                branchInfo.put("protected", branch.isProtected());
                branches.add(branchInfo);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("branches", branches);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to get branches: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    // Helper methods

    private Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("tools", new HashMap<>());
        return capabilities;
    }

    private Map<String, Object> getServerInfo() {
        Map<String, Object> serverInfo = new HashMap<>();
        serverInfo.put("name", "github-mcp-server");
        serverInfo.put("version", "1.0.0");
        return serverInfo;
    }

    private List<Map<String, Object>> getAvailableTools() {
        List<Map<String, Object>> tools = new ArrayList<>();
        
        // Add all available tools
        tools.add(createTool("authenticate_github", "Authenticate with GitHub using a personal access token"));
        tools.add(createTool("get_repositories", "Get all repositories for the authenticated user"));
        tools.add(createTool("get_repository_contents", "Get contents of a repository directory"));
        tools.add(createTool("get_file_content", "Get the content of a specific file"));
        tools.add(createTool("get_issues", "Get issues from a repository"));
        tools.add(createTool("create_issue", "Create a new issue in a repository"));
        tools.add(createTool("get_pull_requests", "Get pull requests from a repository"));
        tools.add(createTool("get_branches", "Get all branches in a repository"));
        tools.add(createTool("create_pull_request", "Create a new pull request in a repository"));
        tools.add(createTool("merge_pull_request", "Merge a pull request in a repository"));
        tools.add(createTool("create_branch", "Create a new branch in a repository"));
        tools.add(createTool("commit_file_change", "Commit a file change to a repository"));
        tools.add(createTool("search_code", "Search code in a repository"));
        tools.add(createTool("delete_branch", "Delete a branch in a repository"));
        tools.add(createTool("edit_issue", "Edit an existing issue in a repository"));
        tools.add(createTool("close_issue", "Close an issue in a repository"));
        tools.add(createTool("comment_issue_pr", "Comment on an issue or pull request"));
        
        return tools;
    }

    private Map<String, Object> createTool(String name, String description) {
        Map<String, Object> tool = new HashMap<>();
        tool.put("name", name);
        tool.put("description", description);
        tool.put("inputSchema", new HashMap<>());
        return tool;
    }

    private Object executeTool(String toolName, Map<String, Object> arguments) throws Exception {
        switch (toolName) {
            case "authenticate_github":
                return authenticateGitHubInternal((String) arguments.get("token"));
            case "get_repositories":
                return getUserRepositoriesInternal((String) arguments.get("username"));
            case "get_repository_contents":
                return getRepositoryContentsInternal(
                    (String) arguments.get("username"),
                    (String) arguments.get("repository"),
                    (String) arguments.getOrDefault("path", "")
                );
            case "get_file_content":
                return getFileContentInternal(
                    (String) arguments.get("username"),
                    (String) arguments.get("repository"),
                    (String) arguments.get("filePath")
                );
            case "get_issues":
                return getIssuesInternal(
                    (String) arguments.get("username"),
                    (String) arguments.get("repository"),
                    (String) arguments.getOrDefault("state", "open")
                );
            case "create_issue":
                return createIssueInternal(
                    (String) arguments.get("username"),
                    (String) arguments.get("repository"),
                    (String) arguments.get("title"),
                    (String) arguments.get("body"),
                    (List<String>) arguments.getOrDefault("labels", new ArrayList<>())
                );
            case "get_pull_requests":
                return getPullRequestsInternal(
                    (String) arguments.get("username"),
                    (String) arguments.get("repository"),
                    (String) arguments.getOrDefault("state", "open")
                );
            case "get_branches":
                return getBranchesInternal(
                    (String) arguments.get("username"),
                    (String) arguments.get("repository")
                );
            case "create_pull_request":
                return createPullRequestInternal(
                    (String) arguments.get("username"),
                    (String) arguments.get("repository"),
                    (String) arguments.get("title"),
                    (String) arguments.get("body"),
                    (String) arguments.get("headBranch"),
                    (String) arguments.get("baseBranch")
                );
            case "merge_pull_request":
                return mergePullRequestInternal(
                    (String) arguments.get("username"),
                    (String) arguments.get("repository"),
                    (Integer) arguments.get("pullNumber")
                );
            case "create_branch":
                return createBranchInternal(
                    (String) arguments.get("username"),
                    (String) arguments.get("repository"),
                    (String) arguments.get("newBranchName"),
                    (String) arguments.get("fromBranch")
                );
            case "commit_file_change":
                return commitFileChangeInternal(
                    (String) arguments.get("username"),
                    (String) arguments.get("repository"),
                    (String) arguments.get("branch"),
                    (String) arguments.get("filePath"),
                    (String) arguments.get("content"),
                    (String) arguments.get("commitMessage")
                );
            case "search_code":
                return searchCodeInternal(
                    (String) arguments.get("username"),
                    (String) arguments.get("repository"),
                    (String) arguments.get("query")
                );
            case "delete_branch":
                return deleteBranchInternal(
                    (String) arguments.get("username"),
                    (String) arguments.get("repository"),
                    (String) arguments.get("branchName")
                );
            case "edit_issue":
                return editIssueInternal(
                    (String) arguments.get("username"),
                    (String) arguments.get("repository"),
                    (Integer) arguments.get("issueNumber"),
                    (String) arguments.get("title"),
                    (String) arguments.get("body")
                );
            case "close_issue":
                return closeIssueInternal(
                    (String) arguments.get("username"),
                    (String) arguments.get("repository"),
                    (Integer) arguments.get("issueNumber")
                );
            case "comment_issue_pr":
                return commentIssuePrInternal(
                    (String) arguments.get("username"),
                    (String) arguments.get("repository"),
                    (Integer) arguments.get("number"),
                    (String) arguments.get("body"),
                    (String) arguments.get("type") // "issue" or "pr"
                );
            default:
                throw new IllegalArgumentException("Unknown tool: " + toolName);
        }
    }

    private Map<String, Object> contentToMap(GHContent content) throws IOException {
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("name", content.getName());
        contentMap.put("path", content.getPath());
        contentMap.put("type", content.getType());
        contentMap.put("size", content.getSize());
        contentMap.put("sha", content.getSha());
        contentMap.put("url", content.getUrl());
        return contentMap;
    }

    // Internal tool execution methods
    private Map<String, Object> authenticateGitHubInternal(String token) throws IOException {
        GitHub github = GitHub.connectUsingOAuth(token);
        String username = github.getMyself().getLogin();
        githubClients.put(username, github);
        userTokens.put(username, token);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("username", username);
        return result;
    }

    private Map<String, Object> getUserRepositoriesInternal(String username) throws IOException {
        GitHub github = githubClients.get(username);
        if (github == null) {
            throw new IllegalStateException("User not authenticated");
        }

        List<Map<String, Object>> repos = new ArrayList<>();
        for (GHRepository repo : github.getMyself().listRepositories()) {
            Map<String, Object> repoInfo = new HashMap<>();
            repoInfo.put("name", repo.getName());
            repoInfo.put("fullName", repo.getFullName());
            repoInfo.put("description", repo.getDescription());
            repoInfo.put("private", repo.isPrivate());
            repoInfo.put("htmlUrl", repo.getHtmlUrl());
            repoInfo.put("cloneUrl", repo.getHttpTransportUrl());
            repoInfo.put("language", repo.getLanguage());
            repoInfo.put("updatedAt", repo.getUpdatedAt().toString());
            repos.add(repoInfo);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("repositories", repos);
        return result;
    }

    private Map<String, Object> getRepositoryContentsInternal(String username, String repoFullName, String path) throws IOException {
        GitHub github = githubClients.get(username);
        if (github == null) {
            throw new IllegalStateException("User not authenticated");
        }

        GHRepository repo = github.getRepository(repoFullName);
        List<Map<String, Object>> contents = new ArrayList<>();
        
        if (path.isEmpty()) {
            for (GHContent content : repo.getDirectoryContent("")) {
                contents.add(contentToMap(content));
            }
        } else {
            GHContent content = repo.getFileContent(path);
            if (content.isFile()) {
                contents.add(contentToMap(content));
            } else {
                for (GHContent item : repo.getDirectoryContent(path)) {
                    contents.add(contentToMap(item));
                }
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("contents", contents);
        return result;
    }

    private Map<String, Object> getFileContentInternal(String username, String repoFullName, String filePath) throws IOException {
        GitHub github = githubClients.get(username);
        if (github == null) {
            throw new IllegalStateException("User not authenticated");
        }

        GHRepository repo = github.getRepository(repoFullName);
        GHContent content = repo.getFileContent(filePath);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("content", content.getContent());
        result.put("encoding", content.getEncoding());
        result.put("size", content.getSize());
        result.put("sha", content.getSha());
        return result;
    }

    private Map<String, Object> getIssuesInternal(String username, String repoFullName, String state) throws IOException {
        GitHub github = githubClients.get(username);
        if (github == null) {
            throw new IllegalStateException("User not authenticated");
        }

        GHRepository repo = github.getRepository(repoFullName);
        List<Map<String, Object>> issues = new ArrayList<>();
        
        for (GHIssue issue : repo.getIssues(GHIssueState.valueOf(state.toUpperCase()))) {
            Map<String, Object> issueInfo = new HashMap<>();
            issueInfo.put("number", issue.getNumber());
            issueInfo.put("title", issue.getTitle());
            issueInfo.put("body", issue.getBody());
            issueInfo.put("state", issue.getState().name());
            issueInfo.put("createdAt", issue.getCreatedAt().toString());
            issueInfo.put("updatedAt", issue.getUpdatedAt().toString());
            issueInfo.put("user", issue.getUser().getLogin());
            issues.add(issueInfo);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("issues", issues);
        return result;
    }

    private Map<String, Object> createIssueInternal(String username, String repoFullName, String title, String body, List<String> labels) throws IOException {
        GitHub github = githubClients.get(username);
        if (github == null) {
            throw new IllegalStateException("User not authenticated");
        }

        GHRepository repo = github.getRepository(repoFullName);
        GHIssueBuilder builder = repo.createIssue(title);
        builder.body(body);
        GHIssue issue = builder.create();
        if (!labels.isEmpty()) {
            issue.addLabels(labels.toArray(new String[0]));
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("issueNumber", issue.getNumber());
        result.put("issueUrl", issue.getHtmlUrl());
        result.put("title", issue.getTitle());
        result.put("message", "Issue #" + issue.getNumber() + " created successfully");
        return result;
    }

    private Map<String, Object> getPullRequestsInternal(String username, String repoFullName, String state) throws IOException {
        GitHub github = githubClients.get(username);
        if (github == null) {
            throw new IllegalStateException("User not authenticated");
        }

        GHRepository repo = github.getRepository(repoFullName);
        List<Map<String, Object>> pullRequests = new ArrayList<>();
        
        for (GHPullRequest pr : repo.queryPullRequests().state(GHIssueState.valueOf(state.toUpperCase())).list()) {
            Map<String, Object> prInfo = new HashMap<>();
            prInfo.put("number", pr.getNumber());
            prInfo.put("title", pr.getTitle());
            prInfo.put("body", pr.getBody());
            prInfo.put("state", pr.getState().name());
            prInfo.put("createdAt", pr.getCreatedAt().toString());
            prInfo.put("updatedAt", pr.getUpdatedAt().toString());
            prInfo.put("user", pr.getUser().getLogin());
            prInfo.put("headBranch", pr.getHead().getRef());
            prInfo.put("baseBranch", pr.getBase().getRef());
            pullRequests.add(prInfo);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("pullRequests", pullRequests);
        return result;
    }

    private Map<String, Object> getBranchesInternal(String username, String repoFullName) throws IOException {
        GitHub github = githubClients.get(username);
        if (github == null) {
            throw new IllegalStateException("User not authenticated");
        }

        GHRepository repo = github.getRepository(repoFullName);
        List<Map<String, Object>> branches = new ArrayList<>();
        
        for (GHBranch branch : repo.getBranches().values()) {
            Map<String, Object> branchInfo = new HashMap<>();
            branchInfo.put("name", branch.getName());
            branchInfo.put("sha", branch.getSHA1());
            branchInfo.put("protected", branch.isProtected());
            branches.add(branchInfo);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("branches", branches);
        return result;
    }

    // --- New tool implementations ---
    private Map<String, Object> createPullRequestInternal(String username, String repoFullName, String title, String body, String headBranch, String baseBranch) throws IOException {
        GitHub github = githubClients.get(username);
        if (github == null) throw new IllegalStateException("User not authenticated");
        GHRepository repo = github.getRepository(repoFullName);
        GHPullRequest pr = repo.createPullRequest(title, headBranch, baseBranch, body);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("pullRequestNumber", pr.getNumber());
        result.put("pullRequestUrl", pr.getHtmlUrl());
        result.put("title", pr.getTitle());
        result.put("headBranch", pr.getHead().getRef());
        result.put("baseBranch", pr.getBase().getRef());
        result.put("message", "Pull Request #" + pr.getNumber() + " created successfully");
        return result;
    }

    private Map<String, Object> mergePullRequestInternal(String username, String repoFullName, Integer pullNumber) throws IOException {
        GitHub github = githubClients.get(username);
        if (github == null) throw new IllegalStateException("User not authenticated");
        GHRepository repo = github.getRepository(repoFullName);
        GHPullRequest pr = repo.getPullRequest(pullNumber);
        pr.merge("Merged by MCP server");
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Pull request merged");
        return result;
    }

    private Map<String, Object> createBranchInternal(String username, String repoFullName, String newBranchName, String fromBranch) throws IOException {
        GitHub github = githubClients.get(username);
        if (github == null) {
            throw new IllegalStateException("User not authenticated");
        }
        GHRepository repo = github.getRepository(repoFullName);
        
        // Try to get the specified branch, fall back to default branch if it doesn't exist
        GHBranch branch;
        String actualFromBranch = fromBranch;
        
        try {
            branch = repo.getBranch(fromBranch);
        } catch (IOException e) {
            // If the specified branch doesn't exist, use the repository's default branch
            String defaultBranch = repo.getDefaultBranch();
            branch = repo.getBranch(defaultBranch);
            actualFromBranch = defaultBranch;
        }
        
        repo.createRef("refs/heads/" + newBranchName, branch.getSHA1());
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Branch '" + newBranchName + "' created from '" + actualFromBranch + "'");
        result.put("branchName", newBranchName);
        result.put("fromBranch", actualFromBranch);
        return result;
    }

    private Map<String, Object> commitFileChangeInternal(String username, String repoFullName, String branch, String filePath, String content, String commitMessage) throws IOException {
        GitHub github = githubClients.get(username);
        if (github == null) throw new IllegalStateException("User not authenticated");
        GHRepository repo = github.getRepository(repoFullName);
        GHContent fileContent = repo.getFileContent(filePath, branch);
        fileContent.update(content, commitMessage, branch);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "File updated");
        return result;
    }

    private Map<String, Object> searchCodeInternal(String username, String repoFullName, String query) throws IOException {
        GitHub github = githubClients.get(username);
        if (github == null) throw new IllegalStateException("User not authenticated");
        List<Map<String, Object>> matches = new ArrayList<>();
        PagedSearchIterable<GHContent> results = github.searchContent().q(query).repo(repoFullName).list();
        for (GHContent item : results) {
            Map<String, Object> match = new HashMap<>();
            match.put("name", item.getName());
            match.put("path", item.getPath());
            matches.add(match);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("matches", matches);
        return result;
    }

    private Map<String, Object> deleteBranchInternal(String username, String repoFullName, String branchName) throws IOException {
        GitHub github = githubClients.get(username);
        if (github == null) throw new IllegalStateException("User not authenticated");
        GHRepository repo = github.getRepository(repoFullName);
        repo.getRef("heads/" + branchName).delete();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Branch deleted");
        return result;
    }

    private Map<String, Object> editIssueInternal(String username, String repoFullName, Integer issueNumber, String title, String body) throws IOException {
        GitHub github = githubClients.get(username);
        if (github == null) throw new IllegalStateException("User not authenticated");
        GHRepository repo = github.getRepository(repoFullName);
        GHIssue issue = repo.getIssue(issueNumber);
        issue.setTitle(title);
        issue.setBody(body);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Issue updated");
        return result;
    }

    private Map<String, Object> closeIssueInternal(String username, String repoFullName, Integer issueNumber) throws IOException {
        GitHub github = githubClients.get(username);
        if (github == null) throw new IllegalStateException("User not authenticated");
        GHRepository repo = github.getRepository(repoFullName);
        GHIssue issue = repo.getIssue(issueNumber);
        issue.close();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Issue closed");
        return result;
    }

    private Map<String, Object> commentIssuePrInternal(String username, String repoFullName, Integer number, String body, String type) throws IOException {
        GitHub github = githubClients.get(username);
        if (github == null) throw new IllegalStateException("User not authenticated");
        GHRepository repo = github.getRepository(repoFullName);
        if ("issue".equals(type)) {
            GHIssue issue = repo.getIssue(number);
            issue.comment(body);
        } else if ("pr".equals(type)) {
            GHPullRequest pr = repo.getPullRequest(number);
            pr.comment(body);
        } else {
            throw new IllegalArgumentException("type must be 'issue' or 'pr'");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Comment added");
        return result;
    }
} 