# AI Agent Chat Application

A simple Java Spring Boot application that provides a chat interface to interact with an AI agent powered by OpenAI's GPT models.

## Features

- 🚀 Modern web-based chat interface
- 🤖 Integration with OpenAI GPT models
- 💬 Real-time chat with typing indicators
- 🎨 Beautiful, responsive UI design
- ⚙️ Easy configuration through web interface
- 🔒 Secure API key handling

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- OpenAI API key

## Setup Instructions

### 1. Clone and Navigate to Project
```bash
cd ai-agent
```

### 2. Build the Project
```bash
mvn clean install
```

### 3. Configure OpenAI API Key

You have two options to configure your OpenAI API key:

#### Option A: Environment Variable (Recommended)
```bash
export OPENAI_API_KEY="your-openai-api-key-here"
```

#### Option B: Application Properties
Edit `src/main/resources/application.properties` and add:
```properties
openai.api.key=your-openai-api-key-here
```

### 4. Run the Application
```bash
mvn spring-boot:run
```

### 5. Access the Application
Open your browser and navigate to:
```
http://localhost:8080
```

## Usage

1. **First Time Setup**: If no API key is configured, you'll see a configuration form at the top of the chat interface. Enter your OpenAI API key and click "Configure".

2. **Chatting**: Once configured, you can start chatting with the AI agent. Simply type your message in the input field and press Enter or click the send button.

3. **Real-time Interaction**: The interface shows typing indicators when the AI is processing your request.

## Configuration Options

You can customize the application by modifying `src/main/resources/application.properties`:

```properties
# Server port (default: 8080)
server.port=8080

# OpenAI model to use (default: gpt-3.5-turbo)
openai.model=gpt-3.5-turbo

# API key (can also be set via environment variable)
openai.api.key=${OPENAI_API_KEY:}
```

## Project Structure

```
src/
├── main/
│   ├── java/com/example/aiagent/
│   │   ├── AiAgentApplication.java      # Main Spring Boot application
│   │   ├── controller/
│   │   │   └── ChatController.java      # REST endpoints and web controller
│   │   ├── model/
│   │   │   ├── ChatMessage.java         # Message model
│   │   │   ├── ChatRequest.java         # Request model
│   │   │   └── ChatResponse.java        # Response model
│   │   └── service/
│   │       └── AiService.java           # OpenAI integration service
│   └── resources/
│       ├── application.properties       # Configuration
│       └── templates/
│           └── chat.html               # Web interface template
└── test/                               # Test files
```

## API Endpoints

- `GET /` - Main chat interface
- `POST /api/chat` - Send a message to the AI agent
- `POST /api/configure` - Configure the OpenAI API key

## Development

### Running Tests
```bash
mvn test
```

### Building JAR
```bash
mvn clean package
```

### Running JAR
```bash
java -jar target/ai-agent-1.0.0.jar
```

## Security Notes

- The API key is stored in memory only and is not persisted
- In a production environment, consider using a secure configuration management system
- The current implementation is for development/demo purposes

## Troubleshooting

### Common Issues

1. **"AI service is not configured"**: Make sure you've set the OpenAI API key either via environment variable or in the web interface.

2. **Connection errors**: Verify your internet connection and that the OpenAI API is accessible.

3. **Port already in use**: Change the server port in `application.properties` or stop the process using port 8080.

### Logs
Check the application logs for detailed error information. The application runs with DEBUG logging enabled by default.

## Contributing

Feel free to submit issues and enhancement requests!

## License

This project is open source and available under the MIT License. 