'use client';

import { useState, useRef, useEffect } from 'react';
import { Message } from './ChatInterface';
import FileUpload from './FileUpload';

interface ChatTabProps {
  isConfigured: boolean;
}

export default function ChatTab({ isConfigured }: ChatTabProps) {
  const [messages, setMessages] = useState<Message[]>([]);
  const [inputMessage, setInputMessage] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Initialize welcome message on client side to avoid hydration issues
  useEffect(() => {
    if (messages.length === 0) {
      setMessages([
        {
          id: '1',
          content: "Hello! I'm your AI assistant. How can I help you today?",
          sender: 'assistant',
          timestamp: new Date(),
          type: 'text'
        }
      ]);
    }
  }, []); // Empty dependency array is intentional - we only want this to run once

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const addMessage = (content: string, sender: 'user' | 'assistant', type: 'text' | 'image' = 'text', imageUrl?: string) => {
    const newMessage: Message = {
      id: Date.now().toString(),
      content,
      sender,
      timestamp: new Date(),
      type,
      imageUrl
    };
    setMessages(prev => [...prev, newMessage]);
  };

  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputMessage.trim() || !isConfigured) return;

    const userMessage = inputMessage.trim();
    addMessage(userMessage, 'user');
    setInputMessage('');
    setIsTyping(true);

    try {
      const response = await fetch('/api/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ message: userMessage }),
      });

      const data = await response.json();
      
      if (data.error) {
        addMessage(`Error: ${data.error}`, 'assistant');
      } else {
        addMessage(data.message, 'assistant');
      }
    } catch {
      addMessage('Sorry, there was an error processing your request.', 'assistant');
    } finally {
      setIsTyping(false);
    }
  };

  const handleFileUpload = async (file: File) => {
    if (!isConfigured) return;

    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await fetch('/api/upload/chat', {
        method: 'POST',
        body: formData,
      });

      const data = await response.json();
      
      if (data.error) {
        addMessage(`Upload error: ${data.error}`, 'assistant');
      } else {
        addMessage(`File &ldquo;${data.fileName}&rdquo; uploaded successfully. I can now analyze its contents.`, 'assistant');
      }
    } catch {
      addMessage('Sorry, there was an error uploading your file.', 'assistant');
    }
  };

  return (
    <div className="flex flex-col h-full">
      <div className="flex-1 p-6 overflow-y-auto bg-gray-50">
        <div className="space-y-4">
          {messages.map((message) => (
            <div
              key={message.id}
              className={`flex ${message.sender === 'user' ? 'justify-end' : 'justify-start'}`}
            >
              {message.type === 'image' && message.imageUrl ? (
                <div className="max-w-[70%] bg-white border border-gray-200 rounded-2xl rounded-bl-md p-4">
                  <div className="text-sm italic text-gray-600 mb-3">
                    Generated: &ldquo;{message.prompt}&rdquo;
                  </div>
                  <img
                    src={message.imageUrl}
                    alt="Generated image"
                    className="w-full rounded-lg shadow-md"
                    onLoad={scrollToBottom}
                  />
                  <div className="text-xs text-gray-500 mt-2">
                    {message.timestamp.toLocaleTimeString('en-US', { 
                      hour: '2-digit', 
                      minute: '2-digit',
                      hour12: false 
                    })}
                  </div>
                </div>
              ) : (
                <div className={`max-w-[70%] rounded-2xl px-4 py-3 ${
                  message.sender === 'user' 
                    ? 'bg-gradient-to-r from-blue-500 to-purple-600 text-white rounded-br-md' 
                    : 'bg-white border border-gray-200 rounded-bl-md'
                }`}>
                  <div className="whitespace-pre-wrap">{message.content}</div>
                  <div className={`text-xs mt-2 ${
                    message.sender === 'user' ? 'text-blue-100' : 'text-gray-500'
                  }`}>
                    {message.timestamp.toLocaleTimeString('en-US', { 
                      hour: '2-digit', 
                      minute: '2-digit',
                      hour12: false 
                    })}
                  </div>
                </div>
              )}
            </div>
          ))}
          
          {isTyping && (
            <div className="flex justify-start">
              <div className="bg-white border border-gray-200 rounded-2xl rounded-bl-md px-4 py-3">
                <div className="flex items-center gap-2">
                  <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></div>
                  <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.1s' }}></div>
                  <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }}></div>
                </div>
              </div>
            </div>
          )}
        </div>
        <div ref={messagesEndRef} />
      </div>
      
      <div className="p-6 bg-white border-t border-gray-200 flex-shrink-0">
        <form onSubmit={handleSendMessage} className="flex gap-3">
          <input
            type="text"
            placeholder="Type your message..."
            value={inputMessage}
            onChange={(e) => setInputMessage(e.target.value)}
            disabled={!isConfigured}
            className="flex-1 px-4 py-3 border-2 border-gray-200 rounded-full focus:outline-none focus:border-blue-500"
          />
          <button
            type="submit"
            disabled={!isConfigured || !inputMessage.trim()}
            className="bg-gradient-to-r from-blue-500 to-purple-600 text-white rounded-full w-12 h-12 flex items-center justify-center hover:scale-105 transition-transform disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:scale-100"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <line x1="22" y1="2" x2="11" y2="13"></line>
              <polygon points="22,2 15,22 11,13 2,9"></polygon>
            </svg>
          </button>
        </form>
        
        <div className="mt-3">
          <FileUpload
            onFileUpload={handleFileUpload}
            context="chat"
            disabled={!isConfigured}
            accept=".txt,.md,.json,.csv,.xml,.html,.css,.js,.py,.java,.cpp,.c,.h,.sql,.log"
            className="justify-center"
          />
        </div>
      </div>
    </div>
  );
} 