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

## Quick Start

### Prerequisites
- Java 17 or higher
- Node.js 18 or higher
- OpenAI API key
- GitHub Personal Access Token (for Coding Buddy feature)

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
3. Start the development server:
   ```bash
   npm run dev
   ```
4. The frontend will start on `http://localhost:3000`

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

### Backend (Spring Boot) on Railway (No Docker)
1. Remove or rename the Dockerfile from the project root.
2. In Railway, set the **root directory** to the project root (not backend/).
3. Set the **Build Command** to:
   ```
   mvn clean package -DskipTests -pl backend -am
   ```
4. Set the **Start Command** to:
   ```
   java -jar backend/target/backend-1.0.0.jar
   ```
5. Add any required environment variables (e.g., `OPENAI_API_KEY`).
6. Deploy!

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