# AI Agent Frontend

This is the Next.js frontend for the AI Agent application. It provides a modern, responsive interface for chatting with AI, generating images, and managing GitHub repositories, featuring LinkedIn-inspired styling and comprehensive session management.

## Features

- 💬 **Chat Interface**: Real-time chat with OpenAI GPT models and persistent sessions
- 🎨 **Image Generation**: Generate images using DALL-E models with localStorage persistence
- 👨‍💻 **GitHub Integration**: Full GitHub repository management via MCP server
- 💾 **Session Management**: Comprehensive session persistence with database storage
- 🔧 **Model Configuration**: Switch between different AI models with persistent settings
- 📱 **Responsive Design**: Works on desktop and mobile devices
- ⚡ **Modern UI**: Built with Next.js 15, TypeScript, and LinkedIn-inspired styling
- 🔄 **Cross-tab Persistence**: All state preserved when switching between tabs

## Tech Stack

- **Framework**: Next.js 15 (App Router)
- **Language**: TypeScript
- **Styling**: Tailwind CSS with LinkedIn-inspired design system
- **State Management**: React Hooks with custom session management
- **API**: REST API calls to Spring Boot backend with proxy configuration
- **File Handling**: Native file upload with FormData
- **Session Storage**: Database-backed session management with localStorage fallback

## Getting Started

### Prerequisites

- Node.js 18+ 
- npm or yarn
- Spring Boot backend running on port 8080
- GitHub MCP server running on port 8081
- OpenAI API key (for chat and image generation)
- GitHub Personal Access Token (for repository management)

### Installation

1. Install dependencies:
   ```bash
   npm install
   ```

2. Start the development server:
   ```bash
   npm run dev
   ```

3. Open [http://localhost:3000](http://localhost:3000) in your browser.

### Development

The frontend is structured as follows:

```
src/
├── app/                 # Next.js app router
│   ├── page.tsx        # Main page with tabbed interface
│   ├── layout.tsx      # Root layout
│   └── globals.css     # Global styles
├── components/         # React components
│   ├── ChatInterface.tsx    # Main interface with tabs
│   ├── ChatTab.tsx         # Chat functionality with sessions
│   ├── ImageTab.tsx        # Image generation from prompts
│   ├── CodingBuddyTab.tsx  # GitHub integration and coding assistant
│   ├── ConfigSection.tsx   # API configuration
│   ├── ModelSelector.tsx   # Model selection
│   └── FileUpload.tsx      # File upload component
├── hooks/             # Custom React hooks
│   └── useSession.ts  # Session management hook
└── types/             # TypeScript type definitions
```

## Three-Tab Interface

### 1. 💬 Chat Tab
- **Real-time messaging** with AI models
- **Persistent sessions** stored in H2 database
- **File upload** for chat context (.txt, .md, .json, etc.)
- **Session management** with search functionality
- **Message history** with timestamps
- **Cross-tab persistence** of chat history

### 2. 🎨 Generate Images Tab
- **Text-to-image generation** using DALL-E
- **Multiple size options**: Square (1024x1024), Landscape (1792x1024), Portrait (1024x1792)
- **Quality settings**: Standard and HD
- **Style options**: Vivid and Natural
- **Image preview** with prompts
- **localStorage persistence** of image history and settings

### 3. 👨‍💻 Coding Buddy Tab
- **GitHub authentication** with Personal Access Tokens
- **Repository browsing** and selection
- **Branch operations** (create new branches)
- **Issue management** (create GitHub issues)
- **AI-powered code analysis** and suggestions
- **Session persistence** of GitHub token, user info, and selected repository
- **Real-time connection status** and error handling

## Session Management

The frontend includes comprehensive session management with database storage:

### Database-Backed Sessions
- **User Sessions**: GitHub tokens, user info, repository lists, and configurations
- **Chat Sessions**: Complete conversation history and context
- **Automatic Restoration**: Seamless state restoration on page reload
- **Cross-tab Persistence**: All settings preserved when switching tabs

### Session Data Structure
```typescript
interface UserSession {
  sessionId: string;
  githubToken?: string;
  githubUsername?: string;
  githubDisplayName?: string;
  repositories?: Repository[];
  selectedRepository?: string;
  openaiApiKey?: string;
  chatModel?: string;
  imageModel?: string;
}
```

### useSession Hook
The `useSession` hook provides:
- **Session initialization** and management
- **Database operations** for saving/loading session data
- **Automatic state restoration** on component mount
- **Error handling** and loading states

## API Integration

The frontend communicates with multiple backend services:

### Backend Services
- **Spring Boot Backend** (port 8080): Main REST API for chat, images, and session management
- **GitHub MCP Server** (port 8081): Model Context Protocol server for GitHub operations

### Proxy Configuration
API requests are automatically proxied through Next.js:
- `/api/*` → `http://localhost:8080/api/*`
- CORS properly configured for development

### Endpoints

#### Chat & Sessions
- `POST /api/chat` - Send chat messages
- `GET /api/sessions` - Get all chat sessions
- `GET /api/sessions/{sessionId}` - Get specific session with messages
- `POST /api/sessions` - Create new chat session
- `PUT /api/sessions/{sessionId}/context` - Update session context
- `PUT /api/sessions/{sessionId}/title` - Update session title
- `DELETE /api/sessions/{sessionId}` - Delete session
- `GET /api/sessions/search` - Search sessions

#### Session Management
- `POST /api/session/generate` - Generate new session ID
- `GET /api/session/{sessionId}` - Get session data
- `PUT /api/session/{sessionId}/token` - Save GitHub token
- `PUT /api/session/{sessionId}/user-info` - Save user information
- `PUT /api/session/{sessionId}/repositories` - Save repository list
- `PUT /api/session/{sessionId}/selected-repository` - Save selected repository
- `PUT /api/session/{sessionId}/config` - Save OpenAI configuration

#### Configuration
- `POST /api/configure` - Configure API key and models
- `POST /api/set-model` - Change chat model
- `POST /api/set-image-model` - Change image model
- `GET /api/models` - Get current model configuration

#### Image Generation
- `POST /api/image` - Generate image from text prompt

#### GitHub Integration
- `POST /api/github/authenticate` - Authenticate with GitHub
- `POST /api/github/repositories` - Get user repositories
- `POST /api/agent` - Execute GitHub operations via MCP server

#### File Upload
- `POST /api/upload/chat` - Upload file for chat context
- `POST /api/upload/image` - Upload image for processing

## Component Details

### ChatInterface.tsx
Main container component that manages the three-tab interface:
- Tab switching logic with persistent state
- Global session management
- API configuration handling
- Responsive design for mobile and desktop

### ChatTab.tsx
Handles chat functionality with persistent sessions:
- Real-time messaging with typing indicators
- Session creation and management
- File upload for context
- Message history display with timestamps
- Search and filter functionality

### ImageTab.tsx
Manages image generation from text prompts:
- Prompt input and validation
- Size, quality, and style options
- Image display with download capability
- Error handling and loading states
- localStorage persistence of settings and history

### CodingBuddyTab.tsx
GitHub integration and coding assistant:
- GitHub authentication with Personal Access Tokens
- Repository browsing and selection
- Branch and issue creation
- AI-powered code analysis
- Connection status and error handling
- Session persistence of GitHub data

### FileUpload.tsx
Reusable file upload component:
- Drag-and-drop interface
- File type validation
- Upload progress indication
- Error handling
- Support for multiple file types

## Session Persistence Features

### Database Storage
- **User Sessions**: All GitHub and configuration data stored in H2 database
- **Chat Sessions**: Complete conversation history with context
- **Automatic Cleanup**: Session data managed efficiently

### localStorage Fallback
- **Image History**: Generated images and settings
- **Temporary Data**: Non-sensitive information
- **Performance**: Fast access for frequently used data

### Cross-tab Synchronization
- **State Sharing**: All tabs share the same session data
- **Real-time Updates**: Changes in one tab reflect in others
- **Automatic Restoration**: State restored when switching tabs

## Design System

The frontend features a **LinkedIn-inspired design system**:

### Color Palette
- **Primary Blue**: Professional LinkedIn blue (#0077B5)
- **Neutral Grays**: Clean, readable text colors
- **White Backgrounds**: Clean, professional appearance
- **Success/Error States**: Clear visual feedback

### Components
- **Buttons**: Rounded corners with subtle shadows and hover effects
- **Cards**: Clean containers with consistent spacing
- **Inputs**: Professional form elements with focus states
- **Chat Bubbles**: Distinct user and assistant styling
- **Tabs**: Clear navigation with active states

### Typography
- **Clean fonts** with proper hierarchy
- **Readable text** with appropriate contrast
- **Consistent spacing** throughout the interface
- **Responsive sizing** for different screen sizes

## Configuration

The frontend automatically proxies API requests to the backend services. Make sure:

1. The Spring Boot backend is running on port 8080
2. The GitHub MCP server is running on port 8081
3. CORS is properly configured on both backend services
4. Your API keys are configured through the web interface:
   - **OpenAI API key** for chat and image generation
   - **GitHub Personal Access Token** for repository management

## Building for Production

```bash
npm run build
```

This creates an optimized production build in the `.next` folder.

## Development Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run start` - Start production server
- `npm run lint` - Run ESLint
- `npm run type-check` - Run TypeScript type checking

## Troubleshooting

### Common Issues
1. **Port conflicts**: Ensure ports 3000, 8080, and 8081 are available
2. **CORS errors**: Check backend CORS configuration
3. **Session persistence**: Verify database connection and session endpoints
4. **GitHub integration**: Ensure MCP server is running and accessible

### Debug Mode
Enable debug logging by setting `NODE_ENV=development` and checking browser console for detailed error messages.

## Contributing

1. Follow the existing code style and patterns
2. Add TypeScript types for new features
3. Test session persistence across tabs
4. Ensure responsive design works on mobile
5. Update documentation for new features

## Learn More

To learn more about Next.js, take a look at the following resources:

- [Next.js Documentation](https://nextjs.org/docs) - learn about Next.js features and API.
- [Learn Next.js](https://nextjs.org/learn) - an interactive Next.js tutorial.

You can check out [the Next.js GitHub repository](https://github.com/vercel/next.js) - your feedback and contributions are welcome!

## Deploy on Vercel

The easiest way to deploy your Next.js app is to use the [Vercel Platform](https://vercel.com/new?utm_medium=default-template&filter=next.js&utm_source=create-next-app&utm_campaign=create-next-app-readme) from the creators of Next.js.

Check out our [Next.js deployment documentation](https://nextjs.org/docs/app/building-your-application/deploying) for more details.
