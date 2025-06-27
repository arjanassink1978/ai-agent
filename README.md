# AI Agent

A modern AI chat and image generation application built with Spring Boot backend and Next.js frontend, featuring persistent chat sessions, LinkedIn-inspired styling, and file upload capabilities.

## 🚀 Features

- **💬 AI Chat**: Real-time conversation with OpenAI GPT models (GPT-4, GPT-3.5, etc.)
- **🎨 Image Generation**: Create stunning images using DALL-E models
- **💾 Persistent Chat Sessions**: Save and manage chat conversations with H2 database
- **📁 File Upload**: Upload files for chat context
- **🔧 Model Configuration**: Switch between different AI models dynamically
- **📱 Responsive Design**: Modern, LinkedIn-inspired interface
- **⚡ Real-time Updates**: Live chat with typing indicators
- **🔄 Tabbed Interface**: Two tabs for chat and image generation

## 🏗️ Architecture

This project uses a modern microservices architecture:

- **Backend**: Spring Boot 3.2 with Java 17
- **Frontend**: Next.js 15 with TypeScript and Tailwind CSS
- **Database**: H2 in-memory database with JPA/Hibernate
- **AI Services**: OpenAI GPT & DALL-E APIs
- **API**: RESTful API with CORS support
- **Styling**: LinkedIn-inspired UI with clean, professional design

## 📁 Project Structure

```
ai-agent/
├── src/                    # Spring Boot backend
│   ├── main/java/techchamps/io/aiagent/
│   │   ├── controller/     # REST API controllers
│   │   ├── service/        # Business logic (AI services)
│   │   ├── model/          # Data models and entities
│   │   ├── repository/     # JPA repositories
│   │   └── config/         # Configuration classes
│   └── main/resources/     # Application properties
├── frontend/               # Next.js frontend
│   ├── src/
│   │   ├── app/           # Next.js app router
│   │   └── components/    # React components (Chat, Image)
│   ├── package.json       # Frontend dependencies
│   └── next.config.js     # Next.js configuration
├── uploads/               # File upload directory
├── pom.xml                # Maven configuration
└── package.json           # Root package.json for scripts
```

## 🛠️ Tech Stack

### Backend
- **Framework**: Spring Boot 3.2
- **Language**: Java 17
- **Build Tool**: Maven
- **Database**: H2 in-memory database
- **ORM**: JPA/Hibernate
- **AI Integration**: OpenAI GPT & DALL-E APIs
- **JSON Processing**: Jackson
- **HTTP Client**: OpenAiService + WebClient

### Frontend
- **Framework**: Next.js 15 (App Router)
- **Language**: TypeScript
- **Styling**: Tailwind CSS with LinkedIn-inspired design system
- **State Management**: React Hooks
- **HTTP Client**: Fetch API

## 🗄️ Database (H2)

The application uses **H2 in-memory database** for persistent storage:

### Database Features
- **In-Memory Storage**: Fast, lightweight database that runs in memory
- **Automatic Schema Creation**: Tables are created automatically on startup
- **Web Console**: Access database at `http://localhost:8080/h2-console`
- **Persistent Sessions**: Chat sessions and messages are stored between restarts

### Database Schema
- **`chat_sessions`**: Stores chat session metadata
  - `id`, `session_id`, `title`, `context`, `model`, `image_model`
  - `created_at`, `updated_at`
- **`chat_messages`**: Stores individual chat messages
  - `id`, `chat_session_id`, `content`, `sender`, `timestamp`
  - `file_content`, `file_name`, `image_url`

### H2 Console Access
1. Start the application
2. Navigate to `http://localhost:8080/h2-console`
3. Use these connection settings:
   - **JDBC URL**: `jdbc:h2:mem:aiagent`
   - **Username**: `SA`
   - **Password**: (leave empty)

## 🚀 Quick Start

### Prerequisites

- **Java 17+**
- **Node.js 18+**
- **Maven 3.6+**
- **OpenAI API Key** (for chat and image generation)

### Installation

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd ai-agent
   ```

2. **Install frontend dependencies**:
   ```bash
   npm run install:frontend
   ```

3. **Install root dependencies**:
   ```bash
   npm install
   ```

### Development

#### Option 1: Run Both Frontend and Backend Together
```bash
npm run dev
```

This will start:
- Spring Boot backend on `http://localhost:8080`
- Next.js frontend on `http://localhost:3000`
- H2 database console at `http://localhost:8080/h2-console`

#### Option 2: Run Separately

**Backend only**:
```bash
npm run dev:backend
# or
mvn spring-boot:run
```

**Frontend only**:
```bash
npm run dev:frontend
# or
cd frontend && npm run dev
```

### Production Build

```bash
# Build both frontend and backend
npm run build

# Start production servers
npm start
```

## 🔧 Configuration

### API Keys

You can provide your API keys in several ways:

1. **Environment Variables**:
   ```bash
   export OPENAI_API_KEY=your_openai_api_key_here
   ```

2. **Application Properties**:
   ```properties
   # src/main/resources/application.properties
   openai.api.key=your_openai_api_key_here
   ```

3. **Web Interface**: Use the configuration form in the application

### Available Models

**Chat Models**:
- `gpt-4` (default)
- `gpt-4-turbo`
- `gpt-4o`
- `gpt-3.5-turbo`
- `gpt-3.5-turbo-16k`

**Image Models**:
- `dall-e-3` (default)
- `dall-e-2`

## 📱 Usage

### Two Main Tabs

1. **💬 Chat Tab**
   - Real-time AI conversations
   - Persistent chat sessions
   - File upload for context
   - Session management

2. **🎨 Generate Images Tab**
   - Create images from text prompts
   - Multiple size and quality options
   - Style customization (Vivid/Natural)

### Getting Started

1. **Open the application**: Navigate to `http://localhost:3000`
2. **Configure API**: Enter your OpenAI API key and select models
3. **Start chatting**: Use the Chat tab for text conversations
4. **Generate images**: Use the Image tab to create images with DALL-E

## 🔌 API Endpoints

### Chat & Sessions
- `POST /api/chat` - Send a chat message
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

## 🎨 Features

### Chat Interface
- **Real-time messaging** with typing indicators
- **Persistent sessions** stored in H2 database
- **File upload** for chat context (.txt, .md, .json, etc.)
- **Session management** with search and organization
- **Message timestamps** and sender identification
- **Responsive design** with auto-scroll
- **LinkedIn-inspired styling** with professional appearance

### Image Generation
- **Multiple size options**: Square (1024x1024), Landscape (1792x1024), Portrait (1024x1792)
- **Quality settings**: Standard and HD
- **Style options**: Vivid and Natural
- **Image preview** with prompts
- **Download functionality** for generated images

### Session Management
- **Persistent storage** in H2 database
- **Session titles** and context
- **Search functionality** across sessions
- **Session organization** with timestamps
- **Message history** preservation

### File Upload
- **Chat context files**: Upload documents for AI analysis
- **Supported formats**: Text files, documents
- **Secure storage** in uploads directory

## 🎨 Design System

The application features a **LinkedIn-inspired design system**:

- **Color Palette**: Professional blues, neutral grays, and clean whites
- **Typography**: Clean, readable fonts with proper hierarchy
- **Components**: Rounded corners, subtle shadows, and consistent spacing
- **Layout**: Clean, organized interface with intuitive navigation
- **Responsive**: Works seamlessly on desktop and mobile devices

## 🗄️ Database Management

### H2 Console
Access the database console at `http://localhost:8080/h2-console`:
- **JDBC URL**: `jdbc:h2:mem:aiagent`
- **Username**: `SA`
- **Password**: (empty)

### Database Operations
- View all chat sessions: `SELECT * FROM chat_sessions;`
- View messages for a session: `SELECT * FROM chat_messages WHERE chat_session_id = ?;`
- Search sessions: `SELECT * FROM chat_sessions WHERE title LIKE '%keyword%';`

### Data Persistence
- **In-memory storage**: Fast access, data persists during application runtime
- **Automatic cleanup**: Data is cleared when application restarts
- **Schema creation**: Tables are created automatically on startup

## 🐛 Troubleshooting

### Common Issues

1. **CORS Errors**: Ensure the backend is running on port 8080
2. **API Key Issues**: Verify your OpenAI API key is valid
3. **Port Conflicts**: Check that ports 8080 and 3000 are available
4. **Build Errors**: Ensure you have the correct Java and Node.js versions
5. **Database Issues**: Check H2 console at `http://localhost:8080/h2-console`
6. **File Upload Errors**: Ensure the `uploads/` directory exists and is writable

### Logs

- **Backend logs**: Check Maven/Spring Boot console output
- **Frontend logs**: Check browser developer console
- **Database logs**: Check H2 console for SQL errors
- **Network issues**: Verify API proxy configuration in `next.config.js`

### Performance Tips

- **H2 Database**: In-memory database provides fast access
- **File Uploads**: Large files may take time to process
- **Image Generation**: DALL-E processing can take 10-30 seconds
- **Session Management**: Large chat histories may impact performance

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly (including database operations)
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 🙏 Acknowledgments

- **OpenAI** for providing the GPT and DALL-E APIs
- **Spring Boot team** for the excellent framework
- **Next.js team** for the modern React framework
- **H2 Database** for lightweight, fast database solution
- **Tailwind CSS** for the utility-first styling approach
- **LinkedIn** for design inspiration 