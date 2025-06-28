'use client';

import { useState, useRef, useEffect } from 'react';
import FileUpload from './FileUpload';
import { useSession } from '../hooks/useSession';

interface ImageTabProps {
  isConfigured: boolean;
}

interface ImageMessage {
  id: string;
  content: string;
  imageUrl: string;
  prompt: string;
  timestamp: Date;
  sender: 'user' | 'assistant';
  type: 'image';
}

export default function ImageTab({ isConfigured }: ImageTabProps) {
  const { sessionData } = useSession();
  const [messages, setMessages] = useState<ImageMessage[]>([]);
  const [prompt, setPrompt] = useState('');
  const [size, setSize] = useState('1024x1024');
  const [quality, setQuality] = useState('standard');
  const [style, setStyle] = useState('vivid');
  const [isGenerating, setIsGenerating] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Load persisted state from session data when it becomes available
  useEffect(() => {
    if (sessionData) {
      try {
        // For now, we'll keep image messages and settings in localStorage since they're more frequent
        // and we don't want to overload the database with every message
        const savedMessages = localStorage.getItem('image-tab-messages');
        if (savedMessages) {
          const parsedMessages = JSON.parse(savedMessages);
          // Convert timestamp strings back to Date objects
          const messagesWithDates = parsedMessages.map((msg: { timestamp: string; [key: string]: unknown }) => ({
            ...msg,
            timestamp: new Date(msg.timestamp)
          }));
          setMessages(messagesWithDates);
        }

        // Load form settings
        const savedPrompt = localStorage.getItem('image-tab-prompt');
        if (savedPrompt) {
          setPrompt(savedPrompt);
        }

        const savedSize = localStorage.getItem('image-tab-size');
        if (savedSize) {
          setSize(savedSize);
        }

        const savedQuality = localStorage.getItem('image-tab-quality');
        if (savedQuality) {
          setQuality(savedQuality);
        }

        const savedStyle = localStorage.getItem('image-tab-style');
        if (savedStyle) {
          setStyle(savedStyle);
        }
      } catch (error) {
        console.error('Error loading image tab state:', error);
      }
    }
  }, [sessionData]); // Run when session data becomes available

  // Save state to localStorage whenever it changes
  useEffect(() => {
    try {
      if (messages.length > 0) {
        localStorage.setItem('image-tab-messages', JSON.stringify(messages));
      }
    } catch (error) {
      console.error('Error saving image messages:', error);
    }
  }, [messages]);

  useEffect(() => {
    try {
      localStorage.setItem('image-tab-prompt', prompt);
    } catch (error) {
      console.error('Error saving prompt:', error);
    }
  }, [prompt]);

  useEffect(() => {
    try {
      localStorage.setItem('image-tab-size', size);
    } catch (error) {
      console.error('Error saving size:', error);
    }
  }, [size]);

  useEffect(() => {
    try {
      localStorage.setItem('image-tab-quality', quality);
    } catch (error) {
      console.error('Error saving quality:', error);
    }
  }, [quality]);

  useEffect(() => {
    try {
      localStorage.setItem('image-tab-style', style);
    } catch (error) {
      console.error('Error saving style:', error);
    }
  }, [style]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleGenerateImage = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!prompt.trim() || !isConfigured) return;

    const userMessage: ImageMessage = {
      id: Date.now().toString(),
      content: `Generate image: ${prompt}`,
      imageUrl: '',
      prompt: prompt,
      timestamp: new Date(),
      sender: 'user',
      type: 'image'
    };

    setMessages(prev => [...prev, userMessage]);
    setIsGenerating(true);

    try {
      const response = await fetch('/api/generate-image', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          prompt: prompt,
          size: size,
          quality: quality,
          style: style
        }),
      });

      if (!response.ok) {
        throw new Error('Failed to generate image');
      }

      const data = await response.json();
      
      const assistantMessage: ImageMessage = {
        id: (Date.now() + 1).toString(),
        content: `Generated image for: ${prompt}`,
        imageUrl: data.imageUrl,
        prompt: prompt,
        timestamp: new Date(),
        sender: 'assistant',
        type: 'image'
      };

      setMessages(prev => [...prev, assistantMessage]);
      setPrompt('');
    } catch (error) {
      console.error('Error generating image:', error);
      const errorMessage: ImageMessage = {
        id: (Date.now() + 1).toString(),
        content: 'Sorry, I encountered an error while generating your image. Please try again.',
        imageUrl: '',
        prompt: prompt,
        timestamp: new Date(),
        sender: 'assistant',
        type: 'image'
      };
      setMessages(prev => [...prev, errorMessage]);
    } finally {
      setIsGenerating(false);
    }
  };

  const handleFileUpload = async (file: File, context: 'chat' | 'image', prompt?: string) => {
    if (!isConfigured) return;

    const formData = new FormData();
    formData.append('file', file);
    if (prompt) {
      formData.append('prompt', prompt);
    }

    try {
      const response = await fetch('/api/upload-image', {
        method: 'POST',
        body: formData,
      });

      if (!response.ok) {
        throw new Error('Failed to upload image');
      }

      const data = await response.json();
      
      const userMessage: ImageMessage = {
        id: Date.now().toString(),
        content: `Uploaded image: ${file.name}`,
        imageUrl: data.imageUrl,
        prompt: prompt || 'Uploaded image',
        timestamp: new Date(),
        sender: 'user',
        type: 'image'
      };

      setMessages(prev => [...prev, userMessage]);
    } catch (error) {
      console.error('Error uploading image:', error);
      const errorMessage: ImageMessage = {
        id: (Date.now() + 1).toString(),
        content: 'Sorry, I encountered an error while uploading your image. Please try again.',
        imageUrl: '',
        prompt: 'Upload error',
        timestamp: new Date(),
        sender: 'assistant',
        type: 'image'
      };
      setMessages(prev => [...prev, errorMessage]);
    }
  };

  return (
    <div className="flex flex-col h-full">
      <div className="flex-1 p-6 overflow-y-auto bg-gray-50">
        <div className="space-y-4">
          {messages.map((message, index) => (
            <div key={`${message.sender}-${index}-${message.timestamp.getTime()}`} className={`flex ${message.sender === 'user' ? 'justify-end' : 'justify-start'}`}>
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
            </div>
          ))}
          
          {isGenerating && (
            <div className="flex justify-start">
              <div className="typing-indicator">
                <div className="w-2 h-2 bg-blue-500 rounded-full animate-bounce"></div>
                <div className="w-2 h-2 bg-blue-500 rounded-full animate-bounce" style={{ animationDelay: '0.1s' }}></div>
                <div className="w-2 h-2 bg-blue-500 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }}></div>
                <span className="text-sm text-gray-600 ml-2">Generating image...</span>
              </div>
            </div>
          )}
        </div>
        <div ref={messagesEndRef} />
      </div>
      
      <div className="p-6 bg-white border-t border-gray-200 flex-shrink-0">
        <form onSubmit={handleGenerateImage} className="space-y-4">
          <div className="flex gap-3">
            <input
              type="text"
              placeholder="Describe the image you want to generate..."
              value={prompt}
              onChange={(e) => setPrompt(e.target.value)}
              disabled={!isConfigured}
              className="image-input"
            />
            <button
              type="submit"
              disabled={!isConfigured || !prompt.trim() || isGenerating}
              className="bg-gradient-to-r from-blue-500 to-purple-600 text-white rounded-full w-12 h-12 flex items-center justify-center hover:scale-105 transition-transform disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:scale-100"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <polygon points="13,2 3,14 12,14 11,22 21,10 12,10 13,2"></polygon>
              </svg>
            </button>
          </div>
          
          <div className="grid grid-cols-3 gap-3">
            <select
              value={size}
              onChange={(e) => setSize(e.target.value)}
              disabled={!isConfigured}
              className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500 text-sm text-gray-700"
            >
              <option value="1024x1024">1024x1024</option>
              <option value="1792x1024">1792x1024</option>
              <option value="1024x1792">1024x1792</option>
            </select>
            
            <select
              value={quality}
              onChange={(e) => setQuality(e.target.value)}
              disabled={!isConfigured}
              className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500 text-sm text-gray-700"
            >
              <option value="standard">Standard</option>
              <option value="hd">HD</option>
            </select>
            
            <select
              value={style}
              onChange={(e) => setStyle(e.target.value)}
              disabled={!isConfigured}
              className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500 text-sm text-gray-700"
            >
              <option value="vivid">Vivid</option>
              <option value="natural">Natural</option>
            </select>
          </div>
          
          <div className="border-t pt-4">
            <FileUpload
              onFileUpload={handleFileUpload}
              context="image"
              disabled={!isConfigured}
              accept="image/*"
              className="justify-center"
            />
          </div>
        </form>
      </div>
    </div>
  );
} 