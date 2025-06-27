'use client';

import { useState } from 'react';
import ChatTab from './ChatTab';
import ImageTab from './ImageTab';
import ConfigSection from './ConfigSection';
import ModelSelector from './ModelSelector';

export interface Message {
  id: string;
  content: string;
  sender: 'user' | 'assistant';
  timestamp: Date;
  type?: 'text' | 'image';
  imageUrl?: string;
  prompt?: string;
}

export interface Config {
  apiKey: string;
  model: string;
  imageModel: string;
}

export default function ChatInterface() {
  const [activeTab, setActiveTab] = useState<'chat' | 'image'>('chat');
  const [isConfigured, setIsConfigured] = useState(false);
  const [config, setConfig] = useState<Config>({
    apiKey: '',
    model: 'gpt-4',
    imageModel: 'dall-e-3'
  });
  const [availableModels] = useState([
    'gpt-4',
    'gpt-4-turbo',
    'gpt-4o',
    'gpt-3.5-turbo',
    'gpt-3.5-turbo-16k'
  ]);
  const [imageModels] = useState([
    'dall-e-3',
    'dall-e-2'
  ]);

  const handleConfigSubmit = async (newConfig: Config) => {
    try {
      const response = await fetch('/api/configure', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(newConfig)
      });

      if (response.ok) {
        setConfig(newConfig);
        setIsConfigured(true);
      } else {
        const error = await response.text();
        alert(`Configuration failed: ${error}`);
      }
    } catch (error) {
      alert(`Error: ${error}`);
    }
  };

  const handleModelChange = async (model: string) => {
    try {
      const response = await fetch('/api/set-model', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `model=${encodeURIComponent(model)}`
      });

      if (response.ok) {
        setConfig(prev => ({ ...prev, model }));
        alert(`Model updated to: ${model}`);
      } else {
        const error = await response.text();
        alert(`Failed to update model: ${error}`);
      }
    } catch (error) {
      alert(`Error: ${error}`);
    }
  };

  const handleImageModelChange = async (imageModel: string) => {
    try {
      const response = await fetch('/api/set-image-model', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `model=${encodeURIComponent(imageModel)}`
      });

      if (response.ok) {
        setConfig(prev => ({ ...prev, imageModel }));
        alert(`Image model updated to: ${imageModel}`);
      } else {
        const error = await response.text();
        alert(`Failed to update image model: ${error}`);
      }
    } catch (error) {
      alert(`Error: ${error}`);
    }
  };

  return (
    <div className="bg-white rounded-2xl shadow-2xl w-full max-w-4xl max-h-[90vh] flex flex-col overflow-hidden">
      {/* Header */}
      <div className="bg-gradient-to-r from-blue-500 to-purple-600 text-white p-6 text-center text-2xl font-bold flex-shrink-0">
        <div className="flex items-center justify-center gap-3">
          <div className={`w-3 h-3 rounded-full ${isConfigured ? 'bg-green-400' : 'bg-red-400'}`}></div>
          AI Agent Chat & Image Generator
        </div>
      </div>

      {/* Configuration Section */}
      {!isConfigured && (
        <ConfigSection 
          onSubmit={handleConfigSubmit}
          availableModels={availableModels}
        />
      )}

      {/* Model Selector */}
      {isConfigured && (
        <ModelSelector
          currentModel={config.model}
          currentImageModel={config.imageModel}
          availableModels={availableModels}
          imageModels={imageModels}
          onModelChange={handleModelChange}
          onImageModelChange={handleImageModelChange}
        />
      )}

      {/* Tab Navigation */}
      <div className="flex bg-gray-50 border-b border-gray-200 flex-shrink-0">
        <button
          className={`flex-1 py-4 text-center font-medium transition-all ${
            activeTab === 'chat'
              ? 'bg-white text-blue-600 border-b-2 border-blue-600'
              : 'text-gray-600 hover:bg-gray-100'
          }`}
          onClick={() => setActiveTab('chat')}
        >
          💬 Chat
        </button>
        <button
          className={`flex-1 py-4 text-center font-medium transition-all ${
            activeTab === 'image'
              ? 'bg-white text-blue-600 border-b-2 border-blue-600'
              : 'text-gray-600 hover:bg-gray-100'
          }`}
          onClick={() => setActiveTab('image')}
        >
          🎨 Generate Images
        </button>
      </div>

      {/* Tab Content */}
      <div className="flex-1 overflow-hidden">
        {activeTab === 'chat' && (
          <ChatTab isConfigured={isConfigured} />
        )}
        {activeTab === 'image' && (
          <ImageTab isConfigured={isConfigured} />
        )}
      </div>
    </div>
  );
} 