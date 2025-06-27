'use client';

import { useState, useRef, useEffect } from 'react';
import { Message } from './ChatInterface';

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

  const addMessage = (content: string, sender: 'user' | 'assistant') => {
    const newMessage: Message = {
      id: Date.now().toString(),
      content,
      sender,
      timestamp: new Date(),
      type: 'text'
    };
    setMessages(prev => [...prev, newMessage]);
  };

  const sendMessage = async (message: string) => {
    if (!isConfigured) {
      addMessage('Please configure the AI service first.', 'assistant');
      return;
    }

    try {
      setIsTyping(true);
      addMessage(message, 'user');

      const response = await fetch('/api/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ message })
      });

      const data = await response.json();

      if (data.error) {
        addMessage(`Error: ${data.error}`, 'assistant');
      } else {
        addMessage(data.message, 'assistant');
      }
    } catch (error) {
      addMessage(`Error: ${error}`, 'assistant');
    } finally {
      setIsTyping(false);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const message = inputMessage.trim();
    if (message) {
      sendMessage(message);
      setInputMessage('');
    }
  };

  return (
    <div className="flex flex-col h-full">
      {/* Messages */}
      <div className="flex-1 p-6 overflow-y-auto bg-gray-50">
        <div className="space-y-4">
          {messages.map((message) => (
            <div
              key={message.id}
              className={`flex ${message.sender === 'user' ? 'justify-end' : 'justify-start'}`}
            >
              <div
                className={`max-w-[70%] px-4 py-3 rounded-2xl ${
                  message.sender === 'user'
                    ? 'bg-gradient-to-r from-blue-500 to-purple-600 text-white rounded-br-md'
                    : 'bg-white text-gray-800 border border-gray-200 rounded-bl-md'
                }`}
              >
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
            </div>
          ))}
          
          {isTyping && (
            <div className="flex justify-start">
              <div className="bg-white text-gray-600 border border-gray-200 rounded-2xl rounded-bl-md px-4 py-3 italic">
                AI is typing...
              </div>
            </div>
          )}
        </div>
        <div ref={messagesEndRef} />
      </div>

      {/* Input */}
      <div className="p-6 bg-white border-t border-gray-200 flex-shrink-0">
        <form onSubmit={handleSubmit} className="flex gap-3">
          <input
            type="text"
            value={inputMessage}
            onChange={(e) => setInputMessage(e.target.value)}
            placeholder="Type your message..."
            className="flex-1 px-4 py-3 border-2 border-gray-200 rounded-full focus:outline-none focus:border-blue-500"
            disabled={!isConfigured}
          />
          <button
            type="submit"
            disabled={!inputMessage.trim() || !isConfigured || isTyping}
            className="bg-gradient-to-r from-blue-500 to-purple-600 text-white rounded-full w-12 h-12 flex items-center justify-center hover:scale-105 transition-transform disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:scale-100"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <line x1="22" y1="2" x2="11" y2="13"></line>
              <polygon points="22,2 15,22 11,13 2,9"></polygon>
            </svg>
          </button>
        </form>
      </div>
    </div>
  );
} 