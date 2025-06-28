'use client';

import { useState, useRef, useEffect } from 'react';
import { Message } from './ChatInterface';
import FileUpload from './FileUpload';
import { useSession } from '../hooks/useSession';

interface ChatTabProps {
  isConfigured: boolean;
}

export default function ChatTab({ isConfigured }: ChatTabProps) {
  const { sessionData } = useSession();
  const [messages, setMessages] = useState<Message[]>([]);
  const [inputMessage, setInputMessage] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Load persisted messages from session data when it becomes available
  useEffect(() => {
    if (sessionData) {
      try {
        // For now, we'll keep chat messages in localStorage since they're more frequent
        // and we don't want to overload the database with every message
        const savedMessages = localStorage.getItem('chat-tab-messages');
        if (savedMessages) {
          const parsedMessages = JSON.parse(savedMessages);
          // Convert timestamp strings back to Date objects
          const messagesWithDates = parsedMessages.map((msg: { timestamp: string; [key: string]: unknown }) => ({
            ...msg,
            timestamp: new Date(msg.timestamp)
          }));
          setMessages(messagesWithDates);
        } else {
          // Initialize with welcome message if no saved messages
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
      } catch (error) {
        console.error('Error loading chat messages:', error);
        // Fallback to welcome message if loading fails
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
    }
  }, [sessionData]); // Run when session data becomes available

  // Save messages to localStorage whenever they change
  useEffect(() => {
    try {
      if (messages.length > 0) {
        localStorage.setItem('chat-tab-messages', JSON.stringify(messages));
      }
    } catch (error) {
      console.error('Error saving chat messages:', error);
    }
  }, [messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages.length]);

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
    e.stopPropagation();
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
          {messages.map((message, index) => (
            <div key={`${message.sender}-${index}-${message.timestamp.getTime()}`} className={`flex ${message.sender === 'user' ? 'justify-end' : 'justify-start'}`}>
              {message.type === 'image' && message.imageUrl ? (
                <div className="chat-bubble-image">
                  <div className="image-prompt-text">
                    Generated: &ldquo;{message.prompt}&rdquo;
                  </div>
                  <img
                    src={message.imageUrl}
                    alt="Generated image"
                    className="w-full rounded-lg shadow-md"
                    onLoad={scrollToBottom}
                  />
                  <div className="chat-timestamp-assistant">
                    {message.timestamp.toLocaleTimeString('en-US', { 
                      hour: '2-digit', 
                      minute: '2-digit',
                      hour12: false 
                    })}
                  </div>
                </div>
              ) : (
                <div className={message.sender === 'user' ? 'chat-bubble-user' : 'chat-bubble-assistant'}>
                  <div className="whitespace-pre-wrap">{message.content}</div>
                  <div className={message.sender === 'user' ? 'chat-timestamp-user' : 'chat-timestamp-assistant'}>
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
              <div className="typing-indicator">
                <div className="w-2 h-2 bg-blue-500 rounded-full animate-bounce"></div>
                <div className="w-2 h-2 bg-blue-500 rounded-full animate-bounce" style={{ animationDelay: '0.1s' }}></div>
                <div className="w-2 h-2 bg-blue-500 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }}></div>
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
            className="chat-input"
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