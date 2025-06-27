'use client';

import { useState, useRef, useEffect } from 'react';
import { Message } from './ChatInterface';

interface ImageTabProps {
  isConfigured: boolean;
}

export default function ImageTab({ isConfigured }: ImageTabProps) {
  const [messages, setMessages] = useState<Message[]>([
    {
      id: '1',
      content: "Welcome to the image generator! Describe what you'd like to create and I'll generate it for you.",
      sender: 'assistant',
      timestamp: new Date(),
      type: 'text'
    }
  ]);
  const [prompt, setPrompt] = useState('');
  const [size, setSize] = useState('1024x1024');
  const [quality, setQuality] = useState('standard');
  const [style, setStyle] = useState('vivid');
  const [isGenerating, setIsGenerating] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

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

  const generateImage = async (promptText: string, sizeValue: string, qualityValue: string, styleValue: string) => {
    if (!isConfigured) {
      addMessage('Please configure the AI service first.', 'assistant');
      return;
    }

    try {
      setIsGenerating(true);
      addMessage(`Generating image: "${promptText}"`, 'user');

      const response = await fetch('/api/generate-image', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          prompt: promptText,
          size: sizeValue,
          quality: qualityValue,
          style: styleValue
        })
      });

      const data = await response.json();

      if (data.error) {
        addMessage(`Error: ${data.error}`, 'assistant');
      } else if (data.imageUrls && data.imageUrls.length > 0) {
        addMessage('', 'assistant', 'image', data.imageUrls[0], data.prompt);
      }
    } catch (error) {
      addMessage(`Error: ${error}`, 'assistant');
    } finally {
      setIsGenerating(false);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const promptText = prompt.trim();
    if (promptText) {
      generateImage(promptText, size, quality, style);
      setPrompt('');
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
              {message.type === 'image' ? (
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
                    {message.timestamp.toLocaleTimeString()}
                  </div>
                </div>
              ) : (
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
                    {message.timestamp.toLocaleTimeString()}
                  </div>
                </div>
              )}
            </div>
          ))}
          
          {isGenerating && (
            <div className="flex justify-start">
              <div className="bg-white text-gray-600 border border-gray-200 rounded-2xl rounded-bl-md px-4 py-3 italic">
                Generating image...
              </div>
            </div>
          )}
        </div>
        <div ref={messagesEndRef} />
      </div>

      {/* Image Generation Form */}
      <div className="p-6 bg-white border-t border-gray-200 flex-shrink-0">
        <form onSubmit={handleSubmit} className="space-y-4">
          <textarea
            value={prompt}
            onChange={(e) => setPrompt(e.target.value)}
            placeholder="Describe the image you want to generate..."
            className="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:outline-none focus:border-blue-500 resize-vertical min-h-[80px]"
            disabled={!isConfigured}
            required
          />
          
          <div className="flex gap-4 flex-wrap">
            <div className="flex flex-col gap-2">
              <label className="text-sm font-medium text-gray-700">Size:</label>
              <select
                value={size}
                onChange={(e) => setSize(e.target.value)}
                className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:border-blue-500"
                disabled={!isConfigured}
              >
                <option value="1024x1024">1024x1024 (Square)</option>
                <option value="1792x1024">1792x1024 (Landscape)</option>
                <option value="1024x1792">1024x1792 (Portrait)</option>
              </select>
            </div>
            
            <div className="flex flex-col gap-2">
              <label className="text-sm font-medium text-gray-700">Quality:</label>
              <select
                value={quality}
                onChange={(e) => setQuality(e.target.value)}
                className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:border-blue-500"
                disabled={!isConfigured}
              >
                <option value="standard">Standard</option>
                <option value="hd">HD</option>
              </select>
            </div>
            
            <div className="flex flex-col gap-2">
              <label className="text-sm font-medium text-gray-700">Style:</label>
              <select
                value={style}
                onChange={(e) => setStyle(e.target.value)}
                className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:border-blue-500"
                disabled={!isConfigured}
              >
                <option value="vivid">Vivid</option>
                <option value="natural">Natural</option>
              </select>
            </div>
          </div>
          
          <button
            type="submit"
            disabled={!prompt.trim() || !isConfigured || isGenerating}
            className="bg-gradient-to-r from-blue-500 to-purple-600 text-white px-6 py-3 rounded-full font-medium hover:scale-105 transition-transform disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:scale-100"
          >
            🎨 Generate Image
          </button>
        </form>
      </div>
    </div>
  );
} 