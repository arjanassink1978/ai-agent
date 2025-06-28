# AI Agent - Chat, Image Generator & GitHub Coding Assistant

A modern AI-powered chat, image generation, and GitHub coding assistant application built with Spring Boot and Next.js, featuring LinkedIn-inspired styling and comprehensive session management.

## Features

### 🤖 AI Chat
- **Multi-model support**: GPT-4, GPT-3.5-turbo, and other OpenAI models
- **Conversation history**: Persistent chat sessions with H2 database
- **Smart context**: Maintains conversation context across messages
- **Real-time responses**: Streaming chat interface with typing indicators
- **Session persistence**: All chat history and settings saved to database

### 🎨 Image Generation
- **DALL-E integration**: Generate images from text descriptions
- **Multiple models**: DALL-E 2 and DALL-E 3 support
- **Image history**: View and manage generated images with localStorage persistence
- **High-quality output**: Professional image generation capabilities
- **Multiple formats**: Square, landscape, and portrait orientations
- **Style options**: Vivid and natural styles

### 👨‍💻 Coding Buddy (GitHub Integration)
- **GitHub MCP Server**: Full GitHub API integration via Model Context Protocol
- **Repository Management**: Browse, select, and connect to GitHub repositories
- **Branch Operations**: Create new branches from any existing branch
- **Issue Management**: Create and manage GitHub issues
- **Code Analysis**: AI-powered code review and suggestions
- **Secure Authentication**: Uses GitHub Personal Access Tokens for secure API access
- **Session Persistence**: GitHub token, user info, and repository selection saved to database

### 💾 Session Management
- **Cross-tab persistence**: All settings and state preserved when switching tabs
- **Database storage**: User sessions, GitHub tokens, and configurations stored in H2 database
- **Automatic restoration**: Seamless state restoration on page reload
- **Secure storage**: Sensitive data encrypted and stored securely

## Quick Start

### Prerequisites
- Java 17 or higher
- Node.js 18 or higher
- OpenAI API key
- GitHub Personal Access Token (for Coding Buddy feature)

### One-Command Startup
Use the unified startup script to launch all services:

```bash
./start-all.sh
```

This script will:
- Stop any existing services on required ports
- Build and start the MCP server (port 8081)
- Build and start the Spring Boot backend (port 8080)
- Build and start the Next.js frontend (port 3000)
- Show real-time logs from all services
- Clean up processes on exit

### Manual Setup

#### Backend Setup
1. Clone the repository
2. Navigate to the project directory
3. Start the Spring Boot backend:
   ```bash
   mvn spring-boot:run
   ```
4. The backend will start on `http://localhost:8080`

#### MCP Server Setup
1. Navigate to the MCP server directory:
   ```bash
   cd mcp-server
   ```
2. Build and start the MCP server:
   ```bash
   mvn clean package
   java -jar target/github-mcp-server-1.0.0.jar --server.port=8081
   ```
3. The MCP server will start on `http://localhost:8081`

#### Frontend Setup
1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm run dev
   ```
4. The frontend will start on `http://localhost:3000`

### Using the Application

#### 1. Configure AI Service
- Click the gear icon (Configuration) in the top right
- Enter your OpenAI API key
- Select a model (recommended: GPT-4)
- Click "Save Configuration"

#### 2. Chat with AI
- Use the Chat tab for general conversations
- Upload files for context-aware responses
- All conversations are automatically saved

#### 3. Generate Images
- Use the Image tab for text-to-image generation
- Choose size, quality, and style options
- Generated images are saved in your browser

#### 4. GitHub Coding Assistant
1. **Get a GitHub Personal Access Token**:
   - Go to [GitHub Settings > Developer settings > Personal access tokens](https://github.com/settings/tokens)
   - Click "Generate new token (classic)"
   - Select scopes:
     - `repo` (for private repositories)
     - `public_repo` (for public repositories only)
   - Copy the generated token

2. **Connect to GitHub**:
   - Go to the "Coding Buddy" tab
   - Enter your GitHub Personal Access Token
   - Click "Authenticate with GitHub"
   - Select a repository from the dropdown

3. **Start Coding with AI**:
   - Ask questions about your code, request reviews, or get refactoring suggestions
   - Create new branches or issues directly from the interface
   - The AI will provide context-aware responses based on your codebase

## API Endpoints

### Chat Endpoints
- `POST /api/chat` - Send a chat message
- `GET /api/sessions` - Get chat sessions
- `POST /api/sessions` - Create a new chat session
- `PUT /api/sessions/{sessionId}/context` - Update session context
- `DELETE /api/sessions/{sessionId}` - Delete session

### Image Generation Endpoints
- `POST /api/image` - Generate an image from text prompt

### Session Management Endpoints
- `POST /api/session/generate` - Generate new session ID
- `GET /api/session/{sessionId}` - Get session data
- `PUT /api/session/{sessionId}/token` - Save GitHub token
- `PUT /api/session/{sessionId}/user-info` - Save user information
- `PUT /api/session/{sessionId}/repositories` - Save repository list
- `PUT /api/session/{sessionId}/selected-repository` - Save selected repository
- `PUT /api/session/{sessionId}/config` - Save OpenAI configuration

### GitHub Integration Endpoints
- `POST /api/github/authenticate` - Authenticate with GitHub
- `POST /api/github/repositories` - Get user repositories
- `POST /api/agent` - Execute GitHub operations via MCP server

### File Upload Endpoints
- `POST /api/upload/chat` - Upload file for chat context
- `POST /api/upload/image` - Upload image for processing

## Architecture

### Backend Services
- **Spring Boot Application**: Main REST API server
- **GitHub MCP Server**: Model Context Protocol server for GitHub operations
- **H2 Database**: In-memory database for session storage
- **OpenAI Integration**: GPT and DALL-E API integration

### Frontend Architecture
- **Next.js 15**: React framework with App Router
- **TypeScript**: Type-safe development
- **Tailwind CSS**: Utility-first styling
- **Session Management**: Comprehensive state persistence
- **Real-time Updates**: Live chat and status updates

### Data Flow
1. **Frontend** → **Backend** → **OpenAI API** (Chat & Images)
2. **Frontend** → **Backend** → **MCP Server** → **GitHub API** (Coding Assistant)
3. **Session Data**: Frontend ↔ Backend ↔ H2 Database

## Configuration

### Backend Configuration
- **Database**: H2 in-memory database (configurable in `application.properties`)
- **CORS**: Configured for development and production
- **File Upload**: Configurable upload directory
- **MCP Server**: Runs on port 8081 by default

### Frontend Configuration
- **API Proxy**: Automatically proxies to backend on port 8080
- **Session Storage**: Database-backed session management
- **Local Storage**: Image history and temporary data

## Security Features

- **GitHub Authentication**: Uses Personal Access Tokens
- **Session Security**: Secure session management with database storage
- **CORS Configuration**: Properly configured for development and production
- **Input Validation**: All API endpoints validate input parameters
- **Token Encryption**: Sensitive tokens encrypted in database

## Development

### Project Structure
```
ai-agent/
├── src/main/java/techchamps/io/aiagent/
│   ├── controller/          # REST API controllers
│   ├── service/            # Business logic services
│   ├── model/              # Data models
│   ├── repository/         # Data access layer
│   └── config/             # Configuration classes
├── mcp-server/             # GitHub MCP server
│   └── src/main/java/techchamps/io/aiagent/mcp/
│       └── GitHubMCPServer.java
├── frontend/
│   ├── src/
│   │   ├── app/            # Next.js app directory
│   │   ├── components/     # React components
│   │   ├── hooks/          # Custom React hooks
│   │   └── types/          # TypeScript type definitions
│   └── public/             # Static assets
├── uploads/                # Generated images storage
├── start-all.sh           # Unified startup script
└── package.json           # Root package.json for concurrent startup
```

### Technologies Used
- **Backend**: Spring Boot 3.2.0, Spring Data JPA, H2 Database
- **MCP Server**: Spring Boot with Model Context Protocol
- **Frontend**: Next.js 15, React 18, TypeScript, Tailwind CSS
- **AI Integration**: OpenAI GPT and DALL-E APIs
- **GitHub Integration**: GitHub REST API v3 via MCP server

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License.

## Support

For issues and questions, please create an issue in the GitHub repository. 