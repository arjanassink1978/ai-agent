# GitHub MCP Integration - AI-Powered Coding Buddy

## Overview

Your AI Agent now includes a powerful GitHub MCP (Model Context Protocol) server that enables natural language interaction with GitHub repositories. The Coding Buddy can understand your requests in plain English and automatically execute the appropriate GitHub actions.

## Features

### 🤖 Natural Language Processing
- **Smart Intent Recognition**: The AI understands what you want to do based on your natural language input
- **Automatic Tool Selection**: Automatically chooses the right GitHub API tool for your request
- **Context-Aware Responses**: Provides reasoning and relevant links for each action

### 🛠️ Available GitHub Actions

#### Issues & Pull Requests
- **Create Issues**: "Create an issue about slow performance"
- **Create Pull Requests**: "Make a PR for the new feature"
- **Close Issues**: "Close issue #123"
- **Add Comments**: "Add a comment to PR #45"

#### Repository Management
- **Search Code**: "Search for all API endpoints"
- **Create Branches**: "Create a new branch for bug fixes"
- **List Branches**: "Show me all branches"
- **List Issues/PRs**: "Show me all open issues"

#### File Operations
- **Commit Changes**: "Commit the changes to fix the bug"
- **Read Files**: "Show me the main.js file"

## Setup Instructions

### 1. Start the MCP Server
```bash
./start-mcp-server.sh
```
This will start the GitHub MCP server on port 8081.

### 2. Configure OpenAI API (Optional)
For advanced natural language processing, add your OpenAI API key to `application.properties`:
```properties
openai.api.key=your_openai_api_key_here
```

If no OpenAI key is provided, the system will use rule-based parsing as a fallback.

### 3. Start the Main Application
```bash
mvn spring-boot:run
```

### 4. Access the Frontend
Navigate to `http://localhost:8080` and go to the Coding Buddy tab.

## Usage Examples

### Creating Issues
```
User: "Create an issue about the slow performance in the login API"
AI: ✅ Issue #15 created successfully! User asked to create an issue
🔗 https://github.com/username/repo/issues/15
```

### Making Pull Requests
```
User: "Make a pull request for the new authentication feature"
AI: ✅ Pull Request #8 created successfully! User asked to create a pull request
🔗 https://github.com/username/repo/pull/8
```

### Searching Code
```
User: "Search for all API endpoints in the codebase"
AI: ✅ Found 12 API endpoints in the codebase
```

### Managing Branches
```
User: "Create a new branch called feature/user-dashboard"
AI: ✅ Branch 'feature/user-dashboard' created successfully!
```

## Architecture

### Backend Components

1. **AgentController** (`/api/agent/coding-buddy`)
   - Receives natural language requests
   - Orchestrates the AI reasoning and tool execution

2. **AgentService**
   - Uses LLM to analyze requests and determine actions
   - Calls the MCP server to execute GitHub tools
   - Generates natural language responses

3. **GitHub MCP Server** (Port 8081)
   - Exposes GitHub API as MCP tools
   - Handles authentication and API calls
   - Returns structured responses

### Frontend Integration

The Coding Buddy tab now:
- Sends natural language requests to `/api/agent/coding-buddy`
- Displays reasoning and relevant links
- Shows success/error messages with context

## API Endpoints

### Agent Endpoints
- `POST /api/agent/coding-buddy` - Process natural language requests
- `POST /api/agent/tools/execute` - Execute specific tools directly

### MCP Server Endpoints
- `POST /mcp/tools/list` - List available tools
- `POST /mcp/tools/call` - Execute a specific tool
- `POST /mcp/initialize` - Initialize the MCP server

## Available MCP Tools

1. `create_issue` - Create a new issue
2. `create_pull_request` - Create a new pull request
3. `commit_file_change` - Commit changes to a file
4. `search_code` - Search code in the repository
5. `get_issues` - List issues
6. `get_pull_requests` - List pull requests
7. `get_branches` - List branches
8. `create_branch` - Create a new branch
9. `merge_pull_request` - Merge a pull request
10. `close_issue` - Close an issue
11. `comment_issue_pr` - Add comment to issue/PR

## Error Handling

The system includes comprehensive error handling:
- **Authentication Errors**: Clear messages for invalid tokens
- **API Rate Limits**: Graceful handling of GitHub API limits
- **Network Issues**: Fallback responses for connectivity problems
- **Invalid Requests**: Helpful clarification messages

## Security

- Personal Access Tokens are handled securely
- No tokens are stored permanently
- All GitHub API calls use proper authentication
- CORS is configured for local development

## Troubleshooting

### Common Issues

1. **MCP Server Not Starting**
   - Check if port 8081 is available
   - Ensure Maven is installed
   - Check Java version (requires Java 17+)

2. **Authentication Failures**
   - Verify your GitHub Personal Access Token
   - Ensure the token has the necessary permissions
   - Check if the token has expired

3. **Natural Language Not Working**
   - Check if OpenAI API key is configured (optional)
   - The system will fall back to rule-based parsing
   - Try more specific language in your requests

### Debug Mode

To enable debug logging, add to `application.properties`:
```properties
logging.level.techchamps.io.aiagent=DEBUG
logging.level.org.springframework.web=DEBUG
```

## Future Enhancements

- [ ] Support for multiple repositories
- [ ] Advanced code analysis and suggestions
- [ ] Integration with CI/CD pipelines
- [ ] Support for GitHub Actions
- [ ] Real-time collaboration features
- [ ] Advanced search and filtering
- [ ] Custom workflow automation

## Contributing

To extend the MCP server with new tools:
1. Add the tool method to `GitHubMCPServer.java`
2. Update the tool list in the `getTools()` method
3. Add the tool to the `executeTool()` switch statement
4. Update the system prompt in `AgentService.java` 