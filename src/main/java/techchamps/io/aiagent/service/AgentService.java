package techchamps.io.aiagent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import techchamps.io.aiagent.model.AgentRequest;
import techchamps.io.aiagent.model.AgentResponse;

import java.util.*;
import java.util.UUID;

@Service
public class AgentService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String openaiApiUrl;

    @Value("${mcp.server.url:http://localhost:8081}")
    private String mcpServerUrl;

    public AgentResponse processNaturalLanguageRequest(AgentRequest request) {
        try {
            // Step 0: Authenticate user with MCP server if token is provided
            if (request.getPersonalAccessToken() != null && !request.getPersonalAccessToken().isEmpty()) {
                boolean authSuccess = authenticateUserWithMCP(request.getPersonalAccessToken());
                if (!authSuccess) {
                    AgentResponse errorResponse = new AgentResponse();
                    errorResponse.setSuccess(false);
                    errorResponse.setMessage("❌ Failed to authenticate with GitHub. Please check your Personal Access Token.");
                    return errorResponse;
                }
            }
            
            // Step 1: Use LLM to analyze the request and determine the action
            String reasoning = analyzeRequestWithLLM(request);
            
            // Step 2: Extract tool and parameters from LLM response
            Map<String, Object> toolCall = parseToolCall(reasoning);
            
            // Step 3: Execute the tool via MCP server
            Map<String, Object> result = executeTool(toolCall, request.getUsername(), request.getRepository());
            
            // Step 4: Generate a natural language response
            String response = generateResponse(result, reasoning);
            
            // Step 5: Create and return the response
            AgentResponse agentResponse = new AgentResponse();
            agentResponse.setSuccess(true);
            agentResponse.setMessage(response);
            agentResponse.setAction((String) toolCall.get("tool"));
            agentResponse.setData(result);
            agentResponse.setReasoning(reasoning);
            
            // Add any relevant links
            List<String> links = extractLinks(result);
            agentResponse.setLinks(links);
            
            return agentResponse;
            
        } catch (Exception e) {
            AgentResponse errorResponse = new AgentResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Failed to process request: " + e.getMessage());
            return errorResponse;
        }
    }

    private String analyzeRequestWithLLM(AgentRequest request) {
        if (openaiApiKey.isEmpty()) {
            // Fallback to simple rule-based parsing if no OpenAI key
            return simpleRuleBasedParsing(request.getMessage());
        }

        try {
            String prompt = buildPrompt(request);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4");
            requestBody.put("messages", Arrays.asList(
                Map.of("role", "system", "content", getSystemPrompt()),
                Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("temperature", 0.1);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + openaiApiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(openaiApiUrl, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode responseJson = objectMapper.readTree(response.getBody());
                return responseJson.get("choices").get(0).get("message").get("content").asText();
            } else {
                throw new RuntimeException("OpenAI API request failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            // Fallback to simple parsing
            return simpleRuleBasedParsing(request.getMessage());
        }
    }

    private String getSystemPrompt() {
        return """
            You are an AI coding assistant that helps users interact with GitHub repositories through natural language.
            
            Available tools:
            - create_issue: Create a new issue (parameters: title, body, labels[])
            - create_pull_request: Create a new pull request (parameters: title, body, headBranch, baseBranch)
            - commit_file_change: Commit changes to a file (parameters: branch, filePath, content, commitMessage)
            - search_code: Search for code in the repository (parameters: query)
            - get_issues: List issues (parameters: state)
            - get_pull_requests: List pull requests (parameters: state)
            - get_branches: List branches
            - create_branch: Create a new branch (parameters: newBranchName, fromBranch)
            - merge_pull_request: Merge a pull request (parameters: pullNumber)
            - close_issue: Close an issue (parameters: issueNumber)
            - comment_issue_pr: Add comment to issue/PR (parameters: number, body, type)
            
            Respond with a JSON object in this format:
            {
                "tool": "tool_name",
                "parameters": {
                    "param1": "value1",
                    "param2": "value2"
                },
                "reasoning": "Brief explanation of why this tool was chosen"
            }
            
            If the request is unclear or requires multiple steps, respond with:
            {
                "tool": "clarify",
                "parameters": {},
                "reasoning": "Explanation of what clarification is needed"
            }
            """;
    }

    private String buildPrompt(AgentRequest request) {
        return String.format("""
            User message: %s
            Repository: %s
            Username: %s
            
            Determine which GitHub tool to use and provide the parameters.
            """, request.getMessage(), request.getRepository(), request.getUsername());
    }

    private String simpleRuleBasedParsing(String message) {
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("create") && lowerMessage.contains("issue")) {
            return "{\n" +
                "    \"tool\": \"create_issue\",\n" +
                "    \"parameters\": {\n" +
                "        \"title\": \"Issue from natural language request\",\n" +
                "        \"body\": \"User requested: " + message.replace("\"", "\\\"") + "\",\n" +
                "        \"labels\": [\"enhancement\"]\n" +
                "    },\n" +
                "    \"reasoning\": \"User asked to create an issue\"\n" +
                "}";
        } else if (lowerMessage.contains("pull request") || lowerMessage.contains("pr")) {
            return "{\n" +
                "    \"tool\": \"create_pull_request\",\n" +
                "    \"parameters\": {\n" +
                "        \"title\": \"PR from natural language request\",\n" +
                "        \"body\": \"User requested: " + message.replace("\"", "\\\"") + "\",\n" +
                "        \"headBranch\": \"feature-branch\",\n" +
                "        \"baseBranch\": \"main\"\n" +
                "    },\n" +
                "    \"reasoning\": \"User asked to create a pull request\"\n" +
                "}";
        } else if (lowerMessage.contains("create") && lowerMessage.contains("branch")) {
            // Extract branch name from the message
            String branchName = "bug-fixes";
            if (lowerMessage.contains("bug")) {
                branchName = "bug-fixes";
            } else if (lowerMessage.contains("feature")) {
                branchName = "feature-branch";
            } else if (lowerMessage.contains("hotfix")) {
                branchName = "hotfix";
            }
            
            return "{\n" +
                "    \"tool\": \"create_branch\",\n" +
                "    \"parameters\": {\n" +
                "        \"newBranchName\": \"" + branchName + "\",\n" +
                "        \"fromBranch\": \"main\"\n" +
                "    },\n" +
                "    \"reasoning\": \"User asked to create a new branch for development work\"\n" +
                "}";
        } else if (lowerMessage.contains("search") || lowerMessage.contains("find")) {
            return "{\n" +
                "    \"tool\": \"search_code\",\n" +
                "    \"parameters\": {\n" +
                "        \"query\": \"code search\"\n" +
                "    },\n" +
                "    \"reasoning\": \"User asked to search for code\"\n" +
                "}";
        } else if (lowerMessage.contains("list") || lowerMessage.contains("show")) {
            if (lowerMessage.contains("issue")) {
                return "{\n" +
                    "    \"tool\": \"get_issues\",\n" +
                    "    \"parameters\": {\n" +
                    "        \"state\": \"open\"\n" +
                    "    },\n" +
                    "    \"reasoning\": \"User asked to list issues\"\n" +
                    "}";
            } else if (lowerMessage.contains("branch")) {
                return "{\n" +
                    "    \"tool\": \"get_branches\",\n" +
                    "    \"parameters\": {}\n" +
                    "    \"reasoning\": \"User asked to list branches\"\n" +
                    "}";
            } else if (lowerMessage.contains("pull request") || lowerMessage.contains("pr")) {
                return "{\n" +
                    "    \"tool\": \"get_pull_requests\",\n" +
                    "    \"parameters\": {\n" +
                    "        \"state\": \"open\"\n" +
                    "    },\n" +
                    "    \"reasoning\": \"User asked to list pull requests\"\n" +
                    "}";
            }
        } else {
            return "{\n" +
                "    \"tool\": \"clarify\",\n" +
                "    \"parameters\": {},\n" +
                "    \"reasoning\": \"I understand you want to work with the repository, but I need more specific information. What exactly would you like to do? For example:\\n- Create an issue about a bug\\n- Create a pull request for a new feature\\n- Search for specific code\\n- List all branches\\n- Create a new branch for development\"\n" +
                "}";
        }
        
        // Default fallback
        return "{\n" +
            "    \"tool\": \"clarify\",\n" +
            "    \"parameters\": {},\n" +
            "    \"reasoning\": \"Could not determine the specific action needed. Please clarify what you want to do with the repository.\"\n" +
            "}";
    }

    private Map<String, Object> parseToolCall(String reasoning) {
        try {
            // Extract JSON from the reasoning
            int start = reasoning.indexOf('{');
            int end = reasoning.lastIndexOf('}') + 1;
            if (start >= 0 && end > start) {
                String jsonStr = reasoning.substring(start, end);
                return objectMapper.readValue(jsonStr, Map.class);
            }
        } catch (Exception e) {
            // If parsing fails, return a clarification request
        }
        
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("tool", "clarify");
        fallback.put("parameters", new HashMap<>());
        fallback.put("reasoning", "Could not parse the tool call from: " + reasoning);
        return fallback;
    }

    public Map<String, Object> executeTool(Map<String, Object> toolCall, String username, String repository) {
        String tool = (String) toolCall.get("tool");
        
        if ("clarify".equals(tool)) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", (String) toolCall.get("reasoning"));
            return result;
        }

        try {
            // Call the MCP server
            String mcpUrl = mcpServerUrl + "/mcp/tools/call";
            
            // Add username and repository to arguments
            Map<String, Object> arguments = new HashMap<>((Map<String, Object>) toolCall.get("parameters"));
            arguments.put("username", username);
            arguments.put("repository", repository);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("name", tool);
            requestBody.put("arguments", arguments);
            requestBody.put("callId", UUID.randomUUID().toString());
            
            System.out.println("DEBUG: Sending request to MCP server:");
            System.out.println("  URL: " + mcpUrl);
            System.out.println("  Tool: " + tool);
            System.out.println("  Arguments: " + arguments);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // First try to get the response as a Map
            try {
                ResponseEntity<Map> response = restTemplate.postForEntity(mcpUrl, entity, Map.class);
                
                System.out.println("DEBUG: MCP server response status: " + response.getStatusCode());
                System.out.println("DEBUG: MCP server response body: " + response.getBody());
                
                if (response.getStatusCode() == HttpStatus.OK) {
                    Map<String, Object> responseBody = response.getBody();
                    if (responseBody != null && responseBody.containsKey("content")) {
                        // Extract the actual result from the MCP response
                        List<Map<String, Object>> content = (List<Map<String, Object>>) responseBody.get("content");
                        if (!content.isEmpty()) {
                            String resultText = (String) content.get(0).get("text");
                            System.out.println("DEBUG: MCP result text: " + resultText);
                            try {
                                Map<String, Object> parsedResult = objectMapper.readValue(resultText, Map.class);
                                System.out.println("DEBUG: Parsed result: " + parsedResult);
                                return parsedResult;
                            } catch (Exception e) {
                                System.out.println("DEBUG: Failed to parse result as JSON: " + e.getMessage());
                                // If the result text is not JSON, check if it's an error message
                                if (resultText != null && resultText.contains("User not authenticated")) {
                                    Map<String, Object> error = new HashMap<>();
                                    error.put("success", false);
                                    error.put("message", "❌ User not authenticated. Please check your GitHub token.");
                                    return error;
                                } else if (resultText != null && resultText.contains("Branch not found")) {
                                    Map<String, Object> error = new HashMap<>();
                                    error.put("success", false);
                                    error.put("message", "❌ Branch not found. The repository might use 'master' instead of 'main' as the default branch.");
                                    return error;
                                } else if (resultText != null && resultText.startsWith("Error:")) {
                                    Map<String, Object> error = new HashMap<>();
                                    error.put("success", false);
                                    error.put("message", "❌ " + resultText);
                                    return error;
                                }
                                // Return the raw text as a message
                                Map<String, Object> result = new HashMap<>();
                                result.put("success", true);
                                result.put("message", resultText);
                                return result;
                            }
                        }
                    }
                    System.out.println("DEBUG: Returning raw response body: " + responseBody);
                    return responseBody;
                } else {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("error", "MCP server returned: " + response.getStatusCode());
                    return error;
                }
            } catch (Exception e) {
                System.out.println("DEBUG: Exception when calling MCP server: " + e.getMessage());
                e.printStackTrace();
                // If parsing as Map fails, try to get the raw response
                try {
                    ResponseEntity<String> stringResponse = restTemplate.postForEntity(mcpUrl, entity, String.class);
                    String responseBody = stringResponse.getBody();
                    
                    System.out.println("DEBUG: Raw string response: " + responseBody);
                    
                    if (responseBody != null && responseBody.contains("User not authenticated")) {
                        Map<String, Object> error = new HashMap<>();
                        error.put("success", false);
                        error.put("message", "❌ User not authenticated. Please check your GitHub token.");
                        return error;
                    }
                    
                    // Try to parse as JSON
                    try {
                        Map<String, Object> parsedResult = objectMapper.readValue(responseBody, Map.class);
                        System.out.println("DEBUG: Parsed string response: " + parsedResult);
                        return parsedResult;
                    } catch (Exception jsonError) {
                        System.out.println("DEBUG: Failed to parse string response as JSON: " + jsonError.getMessage());
                        // Return as plain text message
                        Map<String, Object> result = new HashMap<>();
                        result.put("success", true);
                        result.put("message", responseBody);
                        return result;
                    }
                } catch (Exception stringError) {
                    System.out.println("DEBUG: Exception when getting string response: " + stringError.getMessage());
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("error", "Failed to execute tool: " + e.getMessage());
                    return error;
                }
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Outer exception: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to execute tool: " + e.getMessage());
            return error;
        }
    }

    private String generateResponse(Map<String, Object> result, String reasoning) {
        if (result.containsKey("success") && !(Boolean) result.get("success")) {
            String errorMessage = (String) result.get("message");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                return "❌ " + errorMessage;
            } else {
                return "❌ " + result.get("error");
            }
        }
        
        // Extract the reasoning explanation
        try {
            Map<String, Object> toolCall = parseToolCall(reasoning);
            String tool = (String) toolCall.get("tool");
            
            // Handle different tool types with user-friendly messages
            switch (tool) {
                case "clarify":
                    String toolReasoning = (String) toolCall.get("reasoning");
                    return "🤔 I need more information to help you. " + toolReasoning;
                    
                case "create_branch":
                    if (result.containsKey("branchName")) {
                        return "✅ Branch '" + result.get("branchName") + "' created successfully!";
                    } else {
                        return "✅ Branch created successfully!";
                    }
                    
                case "create_issue":
                    if (result.containsKey("issueNumber")) {
                        return "✅ Issue #" + result.get("issueNumber") + " created successfully!";
                    } else {
                        return "✅ Issue created successfully!";
                    }
                    
                case "create_pull_request":
                    if (result.containsKey("pullRequestNumber")) {
                        return "✅ Pull Request #" + result.get("pullRequestNumber") + " created successfully!";
                    } else {
                        return "✅ Pull Request created successfully!";
                    }
                    
                case "search_code":
                    if (result.containsKey("results")) {
                        return "🔍 Found " + result.get("results") + " matching results.";
                    } else {
                        return "🔍 Search completed.";
                    }
                    
                case "get_issues":
                    if (result.containsKey("issues")) {
                        return "📋 Found " + result.get("issues") + " issues.";
                    } else {
                        return "📋 Retrieved issues.";
                    }
                    
                case "get_pull_requests":
                    if (result.containsKey("pullRequests")) {
                        return "📋 Found " + result.get("pullRequests") + " pull requests.";
                    } else {
                        return "📋 Retrieved pull requests.";
                    }
                    
                case "get_branches":
                    if (result.containsKey("branches")) {
                        return "🌿 Found " + result.get("branches") + " branches.";
                    } else {
                        return "🌿 Retrieved branches.";
                    }
                    
                default:
                    if (result.containsKey("message")) {
                        return "✅ " + result.get("message");
                    } else {
                        return "✅ Action completed successfully!";
                    }
            }
        } catch (Exception e) {
            return "✅ Action completed successfully!";
        }
    }

    private List<String> extractLinks(Map<String, Object> result) {
        List<String> links = new ArrayList<>();
        
        if (result.containsKey("issueUrl")) {
            links.add((String) result.get("issueUrl"));
        }
        if (result.containsKey("pullRequestUrl")) {
            links.add((String) result.get("pullRequestUrl"));
        }
        
        return links;
    }

    private boolean authenticateUserWithMCP(String personalAccessToken) {
        try {
            String authUrl = mcpServerUrl + "/mcp/github/authenticate";
            
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("token", personalAccessToken);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(authUrl, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                return responseBody != null && Boolean.TRUE.equals(responseBody.get("success"));
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error authenticating with MCP server: " + e.getMessage());
            return false;
        }
    }
} 