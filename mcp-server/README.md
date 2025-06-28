# GitHub MCP Server

A Model Context Protocol (MCP) server that provides GitHub integration capabilities for AI agents.

## Features

- **Repository Management**: List and connect to GitHub repositories
- **File Operations**: Browse and read files from repositories
- **Code Analysis**: Analyze code for structure, complexity, security, and best practices
- **Code Search**: Search for code patterns within repositories
- **Resource Access**: Access repository metadata and structure information

## Tools Available

### `list_repositories`
Lists all repositories for the authenticated user.

**Parameters:**
- `token` (string): GitHub Personal Access Token

### `connect_repository`
Connects to a specific repository for operations.

**Parameters:**
- `token` (string): GitHub Personal Access Token
- `repository_url` (string): Repository URL (e.g., https://github.com/owner/repo)

### `list_files`
Lists files in the connected repository.

**Parameters:**
- `path` (string, optional): Directory path (defaults to root)

### `read_file`
Reads the contents of a file.

**Parameters:**
- `file_path` (string): Path to the file

### `analyze_code`
Analyzes code in the repository.

**Parameters:**
- `file_paths` (array): Array of file paths to analyze
- `analysis_type` (string): Type of analysis ('structure', 'complexity', 'security', 'best_practices')

### `search_code`
Searches for code patterns in the repository.

**Parameters:**
- `query` (string): Search query
- `file_extension` (string, optional): File extension filter

## Resources Available

### `github://repository/info`
Provides repository information and metadata.

### `github://repository/structure`
Provides repository file structure and organization.

## Setup

1. **Prerequisites:**
   - Java 17 or higher
   - Maven 3.6 or higher
   - GitHub Personal Access Token

2. **Build the server:**
   ```bash
   mvn clean package
   ```

3. **Run the server:**
   ```bash
   java -jar target/mcp-server-1.0.0.jar [port]
   ```
   
   Default port is 3001.

## Configuration

The server can be configured using environment variables:

- `GITHUB_API_BASE`: GitHub API base URL (default: https://api.github.com)

## Integration with AI Agent

The MCP server is integrated with the main AI Agent application through:

1. **MCPService**: Spring Boot service that communicates with the MCP server
2. **MCPController**: REST endpoints that expose MCP functionality
3. **CodingBuddyTab**: Frontend component that uses MCP capabilities

## Usage Example

```bash
# Start the MCP server
./start-mcp-server.sh

# The server will be available on localhost:3001
# The main application can connect to it via the MCP protocol
```

## Protocol

The server implements the Model Context Protocol (MCP) specification:

- **JSON-RPC 2.0**: All communication uses JSON-RPC 2.0
- **Tools**: Function calls for repository operations
- **Resources**: Data access for repository information
- **Streaming**: Support for real-time data streaming

## Security

- GitHub Personal Access Tokens are required for authentication
- Tokens are passed through the MCP protocol but not stored
- All GitHub API calls use HTTPS
- No sensitive data is logged

## Error Handling

The server provides detailed error messages for:
- Authentication failures
- Repository access issues
- File not found errors
- Network connectivity problems
- Invalid parameters

## Development

To contribute to the MCP server:

1. Fork the repository
2. Create a feature branch
3. Implement your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License. 