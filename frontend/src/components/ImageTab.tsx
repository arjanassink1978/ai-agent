'use client';

import { useState, useRef, useEffect } from 'react';
import { Message } from './ChatInterface';
import FileUpload from './FileUpload';

interface ImageTabProps {
  isConfigured: boolean;
}

export default function ImageTab({ isConfigured }: ImageTabProps) {
  const [messages, setMessages] = useState<Message[]>([]);
  const [prompt, setPrompt] = useState('');
  const [size, setSize] = useState('1024x1024');
  const [quality, setQuality] = useState('standard');
  const [style, setStyle] = useState('vivid');
  const [isGenerating, setIsGenerating] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Initialize welcome message on client side to avoid hydration issues
  useEffect(() => {
    if (messages.length === 0) {
      setMessages([
        {
          id: '1',
          content: "Welcome to the image generator! Describe what you'd like to create and I'll generate it for you.",
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

  const addMessage = (content: string, sender: 'user' | 'assistant', type: 'text' | 'image' = 'text', imageUrl?: string, prompt?: string) => {
    const newMessage: Message = {
      id: Date.now().toString(),
      content,
      sender,
      timestamp: new Date(),
      type,
      imageUrl,
      prompt
    };
    setMessages(prev => [...prev, newMessage]);
  };

  const handleGenerateImage = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!prompt.trim() || !isConfigured) return;

    setIsGenerating(true);
    addMessage(`Generating image: &ldquo;${prompt}&rdquo;`, 'user');

    try {
      const response = await fetch('/api/image', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          prompt: prompt.trim(),
          model: 'dall-e-3',
          size,
          quality,
          style
        }),
      });

      const data = await response.json();
      
      if (data.error) {
        addMessage(`Error: ${data.error}`, 'assistant');
      } else if (data.imageUrls && data.imageUrls.length > 0) {
        addMessage('Here is your generated image:', 'assistant', 'image', data.imageUrls[0], prompt);
      } else {
        addMessage('No image was generated. Please try again.', 'assistant');
      }
    } catch {
      addMessage('Sorry, there was an error generating your image.', 'assistant');
    } finally {
      setIsGenerating(false);
      setPrompt('');
    }
  };

  const handleFileUpload = async (file: File, context: 'chat' | 'image', uploadPrompt?: string) => {
    if (!isConfigured) return;

    const formData = new FormData();
    formData.append('file', file);
    if (uploadPrompt) {
      formData.append('prompt', uploadPrompt);
    }

    try {
      const response = await fetch('/api/upload/image', {
        method: 'POST',
        body: formData,
      });

      const data = await response.json();
      
      if (data.error) {
        addMessage(`Upload error: ${data.error}`, 'assistant');
      } else {
        addMessage(`Image &ldquo;${data.fileName}&rdquo; uploaded successfully for generation. ${uploadPrompt ? `Prompt: &ldquo;${uploadPrompt}&rdquo;` : ''}`, 'assistant');
      }
    } catch {
      addMessage('Sorry, there was an error uploading your image.', 'assistant');
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
          
          {isGenerating && (
            <div className="flex justify-start">
              <div className="bg-white border border-gray-200 rounded-2xl rounded-bl-md px-4 py-3">
                <div className="flex items-center gap-2">
                  <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></div>
                  <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.1s' }}></div>
                  <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }}></div>
                  <span className="text-sm text-gray-600 ml-2">Generating image...</span>
                </div>
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
              className="flex-1 px-4 py-3 border-2 border-gray-200 rounded-full focus:outline-none focus:border-blue-500"
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
              className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500 text-sm"
            >
              <option value="1024x1024">1024x1024</option>
              <option value="1792x1024">1792x1024</option>
              <option value="1024x1792">1024x1792</option>
            </select>
            
            <select
              value={quality}
              onChange={(e) => setQuality(e.target.value)}
              disabled={!isConfigured}
              className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500 text-sm"
            >
              <option value="standard">Standard</option>
              <option value="hd">HD</option>
            </select>
            
            <select
              value={style}
              onChange={(e) => setStyle(e.target.value)}
              disabled={!isConfigured}
              className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500 text-sm"
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