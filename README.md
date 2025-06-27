# AI Agent

A modern AI chat and image generation application built with Spring Boot backend and Next.js frontend.

## 🚀 Features

- **💬 AI Chat**: Real-time conversation with OpenAI GPT models (GPT-4, GPT-3.5, etc.)
- **🎨 Image Generation**: Create stunning images using DALL-E models
- **🔧 Model Configuration**: Switch between different AI models dynamically
- **📱 Responsive Design**: Modern, mobile-friendly interface
- **⚡ Real-time Updates**: Live chat with typing indicators
- **🔄 Tabbed Interface**: Separate tabs for chat and image generation

## 🏗️ Architecture

This project uses a modern microservices architecture:

- **Backend**: Spring Boot 3.2 with Java 17
- **Frontend**: Next.js 14 with TypeScript and Tailwind CSS
- **API**: RESTful API with CORS support
- **Styling**: Modern UI with gradient backgrounds and smooth animations

## 📁 Project Structure

```
ai-agent/
├── src/                    # Spring Boot backend
│   ├── main/java/techchamps/io/aiagent/
│   │   ├── controller/     # REST API controllers
│   │   ├── service/        # Business logic
│   │   ├── model/          # Data models
│   │   └── config/         # Configuration classes
│   └── main/resources/     # Application properties
├── frontend/               # Next.js frontend
│   ├── src/
│   │   ├── app/           # Next.js app router
│   │   └── components/    # React components
│   ├── package.json       # Frontend dependencies
│   └── next.config.js     # Next.js configuration
├── pom.xml                # Maven configuration
└── package.json           # Root package.json for scripts
```

## 🛠️ Tech Stack

### Backend
- **Framework**: Spring Boot 3.2
- **Language**: Java 17
- **Build Tool**: Maven
- **AI Integration**: OpenAI GPT & DALL-E APIs
- **JSON Processing**: Jackson
- **HTTP Client**: OpenAiService

### Frontend
- **Framework**: Next.js 14 (App Router)
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **State Management**: React Hooks
- **HTTP Client**: Fetch API

## 🚀 Quick Start

### Prerequisites

- **Java 17+**
- **Node.js 18+**
- **Maven 3.6+**
- **OpenAI API Key**

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

### OpenAI API Key

You can provide your OpenAI API key in several ways:

1. **Environment Variable**:
   ```bash
   export OPENAI_API_KEY=your_api_key_here
   ```

2. **Application Properties**:
   ```properties
   # src/main/resources/application.properties
   openai.api.key=your_api_key_here
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

1. **Open the application**: Navigate to `http://localhost:3000`
2. **Configure API**: Enter your OpenAI API key and select models
3. **Start chatting**: Use the Chat tab for text conversations
4. **Generate images**: Use the Image tab to create images with DALL-E

## 🔌 API Endpoints

### Chat
- `POST /api/chat` - Send a chat message
- `POST /api/configure` - Configure API key and models
- `POST /api/set-model` - Change chat model

### Image Generation
- `POST /api/generate-image` - Generate an image
- `POST /api/set-image-model` - Change image model

### Models
- `GET /api/models` - Get current model configuration

## 🎨 Features

### Chat Interface
- Real-time message exchange
- Typing indicators
- Message timestamps
- Responsive design
- Auto-scroll to latest messages

### Image Generation
- Multiple size options (Square, Landscape, Portrait)
- Quality settings (Standard, HD)
- Style options (Vivid, Natural)
- Image preview with prompts
- Download generated images

### Model Management
- Dynamic model switching
- Separate chat and image models
- Configuration persistence
- Real-time model updates

## 🐛 Troubleshooting

### Common Issues

1. **CORS Errors**: Ensure the backend is running on port 8080
2. **API Key Issues**: Verify your OpenAI API key is valid
3. **Port Conflicts**: Check that ports 8080 and 3000 are available
4. **Build Errors**: Ensure you have the correct Java and Node.js versions

### Logs

- **Backend logs**: Check Maven/Spring Boot console output
- **Frontend logs**: Check browser developer console
- **Network issues**: Verify API proxy configuration in `next.config.js`

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 🙏 Acknowledgments

- OpenAI for providing the GPT and DALL-E APIs
- Spring Boot team for the excellent framework
- Next.js team for the modern React framework
- Tailwind CSS for the utility-first styling approach 