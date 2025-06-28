# GitHub MCP Server

A Model Context Protocol (MCP) server that provides comprehensive GitHub API integration for the AI Agent application.

## Overview

The GitHub MCP Server acts as a bridge between the AI Agent backend and GitHub's REST API, enabling AI-powered GitHub operations through natural language requests.

## Features

### Repository Management
- **List repositories**: Get all user repositories with metadata
- **Repository details**: Fetch detailed information about specific repositories
- **Branch operations**: Create new branches from existing ones
- **Default branch detection**: Automatically detect and use repository's default branch

### Issue Management
- **Create issues**: Generate new GitHub issues with titles and descriptions
- **Issue templates**: Support for structured issue creation
- **Issue linking**: Reference existing issues and pull requests

### Branch Operations
- **Branch creation**: Create new branches from any existing branch
- **Branch validation**: Verify branch existence before operations
- **Fallback handling**: Automatically use default branch if specified branch doesn't exist

### Authentication
- **Token-based auth**: Uses GitHub Personal Access Tokens
- **Secure transmission**: Tokens passed securely between services
- **Permission validation**: Validates token permissions for operations

## Architecture

### MCP Protocol Implementation
- **Tool-based interface**: Exposes GitHub operations as MCP tools
- **JSON-RPC communication**: Standard MCP protocol implementation
- **Error handling**: Comprehensive error reporting and recovery

### GitHub API Integration
- **REST API v3**: Full GitHub REST API integration
- **Rate limiting**: Respects GitHub API rate limits
- **Error mapping**: Maps GitHub API errors to user-friendly messages

## API Endpoints

### MCP Tools

#### `list_repositories`
Lists all repositories for the authenticated user.

**Parameters:**
- `token` (string): GitHub Personal Access Token

**Returns:**
- Array of repository objects with metadata

#### `create_branch`
Creates a new branch from an existing branch.

**Parameters:**
- `repository` (string): Repository name (owner/repo format)
- `newBranchName` (string): Name of the new branch
- `fromBranch` (string): Source branch name
- `username` (string): GitHub username for authentication

**Returns:**
- Success message with branch details
- Error message if operation fails

#### `create_issue`
Creates a new GitHub issue.

**Parameters:**
- `repository` (string): Repository name (owner/repo format)
- `title` (string): Issue title
- `body` (string): Issue description
- `username` (string): GitHub username for authentication

**Returns:**
- Success message with issue details
- Error message if operation fails

## Configuration

### Server Configuration
- **Port**: Default 8081 (configurable via `--server.port`)
- **Host**: Default localhost
- **CORS**: Configured for development environment

### GitHub API Configuration
- **Base URL**: `https://api.github.com`
- **User Agent**: Custom user agent for API requests
- **Rate Limiting**: Automatic rate limit handling

## Usage

### Starting the Server

```bash
# Build the server
mvn clean package

# Start with default port (8081)
java -jar target/github-mcp-server-1.0.0.jar

# Start with custom port
java -jar target/github-mcp-server-1.0.0.jar --server.port=8082
```

### Integration with AI Agent

The MCP server is automatically started by the `start-all.sh` script and communicates with the main backend through HTTP requests.

### Example MCP Tool Call

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/call",
  "params": {
    "name": "create_branch",
    "arguments": {
      "repository": "owner/repo",
      "newBranchName": "feature-branch",
      "fromBranch": "main",
      "username": "github-username"
    }
  }
}
```

## Error Handling

### GitHub API Errors
- **404 Not Found**: Repository or branch doesn't exist
- **401 Unauthorized**: Invalid or expired token
- **403 Forbidden**: Insufficient permissions
- **422 Unprocessable Entity**: Invalid request parameters

### MCP Protocol Errors
- **Invalid parameters**: Missing or invalid tool parameters
- **Tool not found**: Requested tool doesn't exist
- **Internal errors**: Server-side processing errors

## Security

### Token Management
- **No persistence**: Tokens are not stored on the server
- **Secure transmission**: Tokens passed securely between services
- **Minimal scope**: Uses only required GitHub API scopes

### API Security
- **Rate limiting**: Respects GitHub API rate limits
- **Error sanitization**: Prevents sensitive information leakage
- **Input validation**: Validates all input parameters

## Development

### Project Structure
```
mcp-server/
├── src/main/java/techchamps/io/aiagent/mcp/
│   └── GitHubMCPServer.java    # Main MCP server implementation
├── pom.xml                     # Maven configuration
└── target/                     # Build output
```

### Building
```bash
mvn clean package
```

### Testing
```bash
# Test the server is running
curl http://localhost:8081/health

# Test MCP tool call
curl -X POST http://localhost:8081/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"list_repositories","arguments":{"token":"your-token"}}}'
```

## Troubleshooting

### Common Issues

1. **Port conflicts**: Ensure port 8081 is available
2. **GitHub API errors**: Check token permissions and validity
3. **CORS errors**: Verify CORS configuration for frontend requests
4. **Rate limiting**: Monitor GitHub API rate limit usage

### Debug Mode
Enable debug logging by setting the log level to DEBUG in the application configuration.

## Contributing

1. Follow the existing code style and patterns
2. Add proper error handling for new tools
3. Test with various GitHub API scenarios
4. Update documentation for new features

## License

This project is licensed under the MIT License. 