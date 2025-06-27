# AI Agent Frontend

This is the Next.js frontend for the AI Agent application. It provides a modern, responsive interface for chatting with AI and generating images.

## Features

- 💬 **Chat Interface**: Real-time chat with OpenAI GPT models
- 🎨 **Image Generation**: Generate images using DALL-E models
- 🔧 **Model Configuration**: Switch between different AI models
- 📱 **Responsive Design**: Works on desktop and mobile devices
- ⚡ **Modern UI**: Built with Next.js 14, TypeScript, and Tailwind CSS

## Tech Stack

- **Framework**: Next.js 14 (App Router)
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **State Management**: React Hooks
- **API**: REST API calls to Spring Boot backend

## Getting Started

### Prerequisites

- Node.js 18+ 
- npm or yarn
- Spring Boot backend running on port 8080

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
│   ├── page.tsx        # Main page component
│   └── layout.tsx      # Root layout
├── components/         # React components
│   ├── ChatInterface.tsx    # Main chat interface
│   ├── ChatTab.tsx         # Chat functionality
│   ├── ImageTab.tsx        # Image generation
│   ├── ConfigSection.tsx   # API configuration
│   └── ModelSelector.tsx   # Model selection
└── types/             # TypeScript type definitions
```

### API Integration

The frontend communicates with the Spring Boot backend through:

- **Proxy Configuration**: API requests are proxied to `http://localhost:8080`
- **CORS**: Backend is configured to accept requests from `http://localhost:3000`
- **Endpoints**:
  - `POST /api/chat` - Send chat messages
  - `POST /api/generate-image` - Generate images
  - `POST /api/configure` - Configure API key and models
  - `POST /api/set-model` - Change chat model
  - `POST /api/set-image-model` - Change image model

### Building for Production

```bash
npm run build
```

This creates an optimized production build in the `.next` folder.

## Configuration

The frontend automatically proxies API requests to the backend. Make sure:

1. The Spring Boot backend is running on port 8080
2. CORS is properly configured on the backend
3. Your OpenAI API key is configured through the web interface

## Contributing

1. Follow the existing code style
2. Use TypeScript for all new components
3. Add proper error handling
4. Test on both desktop and mobile devices

## Learn More

To learn more about Next.js, take a look at the following resources:

- [Next.js Documentation](https://nextjs.org/docs) - learn about Next.js features and API.
- [Learn Next.js](https://nextjs.org/learn) - an interactive Next.js tutorial.

You can check out [the Next.js GitHub repository](https://github.com/vercel/next.js) - your feedback and contributions are welcome!

## Deploy on Vercel

The easiest way to deploy your Next.js app is to use the [Vercel Platform](https://vercel.com/new?utm_medium=default-template&filter=next.js&utm_source=create-next-app&utm_campaign=create-next-app-readme) from the creators of Next.js.

Check out our [Next.js deployment documentation](https://nextjs.org/docs/app/building-your-application/deploying) for more details.
