# AI Agent Frontend

This is the Next.js frontend for the AI Agent application. It provides a modern, responsive interface for chatting with AI and generating images, featuring LinkedIn-inspired styling and persistent chat sessions.

## Features

- 💬 **Chat Interface**: Real-time chat with OpenAI GPT models and persistent sessions
- 🎨 **Image Generation**: Generate images using DALL-E models
- 📁 **File Upload**: Upload files for chat context
- 💾 **Session Management**: Persistent chat sessions with search and organization
- 🔧 **Model Configuration**: Switch between different AI models
- 📱 **Responsive Design**: Works on desktop and mobile devices
- ⚡ **Modern UI**: Built with Next.js 15, TypeScript, and LinkedIn-inspired styling

## Tech Stack

- **Framework**: Next.js 15 (App Router)
- **Language**: TypeScript
- **Styling**: Tailwind CSS with LinkedIn-inspired design system
- **State Management**: React Hooks
- **API**: REST API calls to Spring Boot backend
- **File Handling**: Native file upload with FormData

## Getting Started

### Prerequisites

- Node.js 18+ 
- npm or yarn
- Spring Boot backend running on port 8080
- OpenAI API key (for chat and image generation)

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
│   └── layout.tsx      # Root layout
├── components/         # React components
│   ├── ChatInterface.tsx    # Main interface with tabs
│   ├── ChatTab.tsx         # Chat functionality with sessions
│   ├── ImageTab.tsx        # Image generation from prompts
│   ├── ConfigSection.tsx   # API configuration
│   ├── ModelSelector.tsx   # Model selection
│   └── FileUpload.tsx      # File upload component
└── types/             # TypeScript type definitions
```

## Two-Tab Interface

### 1. 💬 Chat Tab
- **Real-time messaging** with AI models
- **Persistent sessions** stored in H2 database
- **File upload** for chat context (.txt, .md, .json, etc.)
- **Session management** with search functionality
- **Message history** with timestamps

### 2. 🎨 Generate Images Tab
- **Text-to-image generation** using DALL-E
- **Multiple size options**: Square, Landscape, Portrait
- **Quality settings**: Standard and HD
- **Style options**: Vivid and Natural
- **Image preview** with prompts

## API Integration

The frontend communicates with the Spring Boot backend through:

- **Proxy Configuration**: API requests are proxied to `http://localhost:8080`
- **CORS**: Backend is configured to accept requests from `http://localhost:3000`
- **Endpoints**:

### Chat & Sessions
- `POST /api/chat` - Send chat messages
- `GET /api/sessions` - Get all chat sessions
- `GET /api/sessions/{sessionId}` - Get specific session with messages
- `POST /api/sessions` - Create new chat session
- `PUT /api/sessions/{sessionId}/context` - Update session context
- `PUT /api/sessions/{sessionId}/title` - Update session title
- `DELETE /api/sessions/{sessionId}` - Delete session
- `GET /api/sessions/search` - Search sessions

### Configuration
- `POST /api/configure` - Configure API key and models
- `POST /api/set-model` - Change chat model
- `POST /api/set-image-model` - Change image model
- `GET /api/models` - Get current model configuration

### Image Generation
- `POST /api/image` - Generate image from text prompt

### File Upload
- `POST /api/upload/chat` - Upload file for chat context

## Component Details

### ChatInterface.tsx
Main container component that manages the two-tab interface:
- Tab switching logic
- Global state management
- API configuration handling

### ChatTab.tsx
Handles chat functionality with persistent sessions:
- Real-time messaging
- Session creation and management
- File upload for context
- Message history display

### ImageTab.tsx
Manages image generation from text prompts:
- Prompt input and validation
- Size, quality, and style options
- Image display and download
- Error handling

### FileUpload.tsx
Reusable file upload component:
- Drag-and-drop interface
- File type validation
- Upload progress indication
- Error handling

## Session Management

The frontend includes comprehensive session management:

- **Session Creation**: Automatically creates new sessions for conversations
- **Session Persistence**: Sessions are stored in H2 database
- **Session Search**: Search through existing sessions
- **Session Organization**: Sessions are organized by title and timestamp
- **Message History**: Complete message history for each session

## File Upload Features

### Supported File Types
- **Chat Context**: `.txt`, `.md`, `.json`, `.csv`, `.xml`, `.html`, `.css`, `.js`, `.py`, `.java`, `.cpp`, `.c`, `.h`, `.sql`, `.log`

### Upload Functionality
- **Drag-and-drop** interface
- **File validation** and size limits
- **Progress indication** during upload
- **Error handling** for failed uploads
- **Secure storage** in backend uploads directory

## Design System

The frontend features a **LinkedIn-inspired design system**:

### Color Palette
- **Primary Blue**: Professional LinkedIn blue (#0077B5)
- **Neutral Grays**: Clean, readable text colors
- **White Backgrounds**: Clean, professional appearance

### Components
- **Buttons**: Rounded corners with subtle shadows
- **Cards**: Clean containers with consistent spacing
- **Inputs**: Professional form elements
- **Chat Bubbles**: Distinct user and assistant styling

### Typography
- **Clean fonts** with proper hierarchy
- **Readable text** with appropriate contrast
- **Consistent spacing** throughout the interface

## Configuration

The frontend automatically proxies API requests to the backend. Make sure:

1. The Spring Boot backend is running on port 8080
2. CORS is properly configured on the backend
3. Your API keys are configured through the web interface:
   - **OpenAI API key** for chat and image generation

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

1. **API Connection**: Ensure the Spring Boot backend is running on port 8080
2. **CORS Errors**: Check that the backend CORS configuration includes `http://localhost:3000`
3. **Build Errors**: Verify all dependencies are installed with `npm install`
4. **TypeScript Errors**: Run `npm run type-check` to identify type issues

### Performance Tips

- **Image Optimization**: Next.js automatically optimizes images
- **Code Splitting**: Components are automatically code-split
- **Caching**: API responses are cached appropriately
- **Bundle Size**: Use dynamic imports for large components if needed

## Contributing

1. Follow the existing code style and patterns
2. Test your changes thoroughly
3. Ensure TypeScript types are properly defined
4. Update documentation as needed

## Learn More

To learn more about Next.js, take a look at the following resources:

- [Next.js Documentation](https://nextjs.org/docs) - learn about Next.js features and API.
- [Learn Next.js](https://nextjs.org/learn) - an interactive Next.js tutorial.

You can check out [the Next.js GitHub repository](https://github.com/vercel/next.js) - your feedback and contributions are welcome!

## Deploy on Vercel

The easiest way to deploy your Next.js app is to use the [Vercel Platform](https://vercel.com/new?utm_medium=default-template&filter=next.js&utm_source=create-next-app&utm_campaign=create-next-app-readme) from the creators of Next.js.

Check out our [Next.js deployment documentation](https://nextjs.org/docs/app/building-your-application/deploying) for more details.
