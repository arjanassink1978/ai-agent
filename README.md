# AI Agent - Chat & Image Generator

A modern AI-powered chat and image generation application built with Spring Boot and Next.js, featuring LinkedIn-inspired styling.

## Features

### 🤖 AI Chat
- **Multi-model support**: GPT-4, GPT-3.5-turbo, and other OpenAI models
- **Conversation history**: Persistent chat sessions with H2 database
- **Smart context**: Maintains conversation context across messages
- **Real-time responses**: Streaming chat interface with typing indicators

### 🎨 Image Generation
- **DALL-E integration**: Generate images from text descriptions
- **Multiple models**: DALL-E 2 and DALL-E 3 support
- **Image history**: View and manage generated images
- **High-quality output**: Professional image generation capabilities

### 👨‍💻 Coding Buddy
- **GitHub Integration**: Authenticate with GitHub using Personal Access Tokens
- **Repository Browsing**: Browse and select from your GitHub repositories
- **Code Analysis**: Select specific files for AI-powered code analysis
- **Best Practices**: Get suggestions for code improvements, refactoring, and best practices
- **Development Support**: Ask questions about your codebase and get intelligent responses
- **Secure Authentication**: Uses GitHub Personal Access Tokens for secure API access
- **MCP Server**: Advanced GitHub integration through Model Context Protocol (MCP) server

## Quick Start

### Prerequisites
- Java 17 or higher
- Node.js 18 or higher
- OpenAI API key
- GitHub Personal Access Token (for Coding Buddy feature)

### MCP Server Setup (Optional)
The GitHub MCP server provides advanced repository integration capabilities:

1. **Automatic startup**: The MCP server starts automatically when you run `npm run dev` in the frontend directory
2. **Manual control**: You can also start/stop the MCP server manually:
   ```bash
   # Start MCP server only
   cd frontend && npm run dev:mcp
   
   # Stop MCP server
   cd frontend && npm run stop:mcp
   ```

The MCP server will be available on port 3001 and provides enhanced repository operations through the MCP protocol.

### Backend Setup
1. Clone the repository
2. Navigate to the project directory
3. Start the Spring Boot backend:
   ```bash
   mvn spring-boot:run
   ```
4. The backend will start on `http://localhost:8080`

### Frontend Setup
1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the development server (this will also start the MCP server):
   ```bash
   npm run dev
   ```
4. The frontend will start on `http://localhost:3000` and the MCP server on port 3001

### Using the Coding Buddy Feature

1. **Get a GitHub Personal Access Token**:
   - Go to [GitHub Settings > Developer settings > Personal access tokens](https://github.com/settings/tokens)
   - Click "Generate new token (classic)"
   - Select scopes:
     - `repo` (for private repositories)
     - `public_repo` (for public repositories only)
   - Copy the generated token

2. **Configure the AI Service**:
   - Click the gear icon (Configuration) in the top right
   - Enter your OpenAI API key
   - Select a model (recommended: GPT-4)
   - Click "Save Configuration"

3. **Connect to GitHub**:
   - Go to the "Coding Buddy" tab
   - Enter your GitHub Personal Access Token
   - Click "Authenticate with GitHub"
   - Select a repository from the list

4. **Start Coding with AI**:
   - Choose specific files to analyze (optional)
   - Ask questions about your code, request reviews, or get refactoring suggestions
   - The AI will provide context-aware responses based on your codebase

## API Endpoints

### Chat Endpoints
- `POST /api/chat` - Send a chat message
- `GET /api/chat/sessions` - Get chat sessions
- `POST /api/chat/sessions` - Create a new chat session

### Image Generation Endpoints
- `POST /api/images/generate` - Generate an image from text
- `GET /api/images` - Get generated images

### Coding Buddy Endpoints
- `POST /api/github/authenticate` - Authenticate with GitHub using Personal Access Token
- `POST /api/github/repositories` - Get user repositories
- `POST /api/connect-repository` - Connect to a specific repository
- `POST /api/coding-chat` - Send coding-related questions with repository context

### MCP Server Endpoints
- `POST /api/mcp/connect` - Connect to the GitHub MCP server
- `POST /api/mcp/disconnect` - Disconnect from the MCP server
- `GET /api/mcp/status` - Get MCP server connection status
- `POST /api/mcp/tools/list` - Get available MCP tools
- `POST /api/mcp/resources/list` - Get available MCP resources
- `POST /api/mcp/repositories` - List repositories via MCP
- `POST /api/mcp/connect-repository` - Connect to repository via MCP
- `POST /api/mcp/files` - List files via MCP
- `POST /api/mcp/read-file` - Read file content via MCP
- `POST /api/mcp/analyze-code` - Analyze code via MCP
- `POST /api/mcp/search-code` - Search code via MCP

## Configuration

### Backend Configuration
The application uses H2 in-memory database by default. Configuration can be modified in `src/main/resources/application.properties`.

### Frontend Configuration
- OpenAI API key and model selection are managed through the UI
- GitHub Personal Access Token is stored only in the browser session for security

## Security Features

- **GitHub Authentication**: Uses Personal Access Tokens instead of username/password
- **Token Security**: Tokens are stored only in browser session, never persisted
- **CORS Configuration**: Properly configured for development and production
- **Input Validation**: All API endpoints validate input parameters

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
├── frontend/
│   ├── src/
│   │   ├── app/            # Next.js app directory
│   │   ├── components/     # React components
│   │   └── types/          # TypeScript type definitions
│   └── public/             # Static assets
└── uploads/                # Generated images storage
```

### Technologies Used
- **Backend**: Spring Boot 3.2.0, Spring Data JPA, H2 Database
- **Frontend**: Next.js 15, React 18, TypeScript, Tailwind CSS
- **AI Integration**: OpenAI GPT and DALL-E APIs
- **GitHub Integration**: GitHub REST API v3

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

## Deployment

### Backend (Spring Boot) on Railway
1. Push your latest code to GitHub.
2. Go to [Railway](https://railway.app/) and sign in with your GitHub account.
3. Create a new project and select "Deploy from GitHub repo".
4. Select your repository (`ai-agent`).
5. If using Docker, Railway will auto-detect the Dockerfile. Otherwise, set build command: `./mvnw clean package -DskipTests` and start command: `java -jar target/*.jar`.
6. Set environment variables (e.g., `OPENAI_API_KEY`, `STABILITY_API_KEY`).
7. Expose port 8080 (Railway auto-detects this).

### Frontend (Next.js) on Vercel
1. Go to [Vercel](https://vercel.com/) and sign in with your GitHub account.
2. Import your repository (`ai-agent`).
3. Set the project root to `frontend/`.
4. Set environment variable `NEXT_PUBLIC_API_URL` to your Railway backend URL (e.g., `https://your-backend.up.railway.app`).
5. Vercel will auto-detect Next.js and deploy.

### Environment Variables
- `OPENAI_API_KEY` (backend)
- `STABILITY_API_KEY` (backend)
- `NEXT_PUBLIC_API_URL` (frontend, Vercel)

### Useful Links
- [Railway Dashboard](https://railway.app/dashboard)
- [Vercel Dashboard](https://vercel.com/dashboard) 