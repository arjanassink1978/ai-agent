'use client';

import { useState, useEffect } from 'react';
import ChatTab from './ChatTab';
import ImageTab from './ImageTab';
import CodingBuddyTab from './CodingBuddyTab';
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
  const [activeTab, setActiveTab] = useState<'chat' | 'image' | 'coding'>('chat');
  const [isConfigured, setIsConfigured] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
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

  // Load current configuration on startup
  useEffect(() => {
    const loadConfig = async () => {
      try {
        console.log('Loading configuration from backend...');
        const response = await fetch('/api/models');
        console.log('Response status:', response.status);
        
        if (response.ok) {
          const data = await response.json();
          console.log('Configuration data:', data);
          const currentConfig = {
            apiKey: '', // Always start with empty API key
            model: data.selectedModel || 'gpt-4',
            imageModel: data.selectedImageModel || 'dall-e-3'
          };
          setConfig(currentConfig);
          // Await API key check
          await checkApiKeyStatus();
        } else {
          console.log('Failed to load configuration, status:', response.status);
          const errorText = await response.text();
          console.log('Error response:', errorText);
          setIsConfigured(false);
        }
      } catch (error) {
        console.error('Error loading configuration:', error);
        setIsConfigured(false);
      } finally {
        setIsLoading(false);
      }
    };

    const checkApiKeyStatus = async () => {
      try {
        const testResponse = await fetch('/api/chat', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            message: 'test',
            sessionId: 'test-session'
          })
        });
        
        const testData = await testResponse.json();
        console.log('Test response:', testData);
        
        if (testResponse.ok && !testData.error && testData.message !== 'AI service is not configured. Please set the OpenAI API key.') {
          setIsConfigured(true);
          console.log('API key is configured');
        } else {
          setIsConfigured(false);
          console.log('API key not configured');
        }
      } catch (testError) {
        console.log('Test call failed, assuming API key not configured');
        setIsConfigured(false);
      }
    };

    loadConfig();
  }, []);

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
        console.log('Configuration successful!');
      } else {
        const error = await response.text();
        console.error(`Configuration failed: ${error}`);
      }
    } catch (error) {
      console.error(`Error: ${error}`);
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
        console.log(`Model updated to: ${model}`);
      } else {
        const error = await response.text();
        console.error(`Failed to update model: ${error}`);
      }
    } catch (error) {
      console.error(`Error: ${error}`);
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
        console.log(`Image model updated to: ${imageModel}`);
      } else {
        const error = await response.text();
        console.error(`Failed to update image model: ${error}`);
      }
    } catch (error) {
      console.error(`Error: ${error}`);
    }
  };

  if (isLoading) {
    return (
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-7xl max-h-[95vh] flex flex-col overflow-hidden">
        <div className="bg-gradient-to-r from-blue-500 to-purple-600 text-white p-6 text-center text-2xl font-bold">
          AI Agent Chat & Image Generator
        </div>
        <div className="flex-1 flex items-center justify-center">
          <div className="text-gray-600">Loading configuration...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-2xl shadow-2xl w-full max-w-7xl max-h-[95vh] flex flex-col overflow-hidden">
      {/* Header */}
      <div className="bg-gradient-to-r from-blue-500 to-purple-600 text-white p-6 text-center text-2xl font-bold flex-shrink-0">
        <div className="flex items-center justify-center gap-3">
          <div className={`w-3 h-3 rounded-full ${isConfigured ? 'bg-green-400' : 'bg-red-400'}`}></div>
          AI Agent Chat & Image Generator
          {isConfigured && (
            <button
              onClick={() => setIsConfigured(false)}
              className="ml-4 px-3 py-1 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-all"
            >
              Reconfigure
            </button>
          )}
        </div>
      </div>

      {/* Configuration Section */}
      {!isConfigured && (
        <ConfigSection 
          onSubmit={handleConfigSubmit}
          availableModels={availableModels}
          currentConfig={config}
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
      <div className="bg-white border-b border-gray-200 flex-shrink-0 p-4">
        <div className="flex gap-2">
          <button
            onClick={() => setActiveTab('chat')}
            className={`tab-button ${activeTab === 'chat' ? 'tab-button-active' : 'tab-button-inactive'}`}
          >
            <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M18 10c0 3.866-3.582 7-8 7a8.841 8.841 0 01-4.083-.98L2 17l1.338-3.123C2.493 12.767 2 11.434 2 10c0-3.866 3.582-7 8-7s8 3.134 8 7zM7 9H5v2h2V9zm8 0h-2v2h2V9zM9 9h2v2H9V9z" clipRule="evenodd" />
            </svg>
            Chat
          </button>
          <button
            onClick={() => setActiveTab('image')}
            className={`tab-button ${activeTab === 'image' ? 'tab-button-active' : 'tab-button-inactive'}`}
          >
            <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z" clipRule="evenodd" />
            </svg>
            Generate Images
          </button>
          <button
            onClick={() => setActiveTab('coding')}
            className={`tab-button ${activeTab === 'coding' ? 'tab-button-active' : 'tab-button-inactive'}`}
          >
            <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M12.316 3.051a1 1 0 01.633 1.265l-4 12a1 1 0 11-1.898-.632l4-12a1 1 0 011.265-.633zM5.707 6.293a1 1 0 010 1.414L3.414 10l2.293 2.293a1 1 0 11-1.414 1.414l-3-3a1 1 0 010-1.414l3-3a1 1 0 011.414 0zm8.586 0a1 1 0 011.414 0l3 3a1 1 0 010 1.414l-3 3a1 1 0 11-1.414-1.414L16.586 10l-2.293-2.293a1 1 0 010-1.414z" clipRule="evenodd" />
            </svg>
            Coding Buddy
          </button>
        </div>
      </div>

      {/* Tab Content */}
      <div className="flex-1 overflow-hidden">
        {activeTab === 'chat' && (
          <ChatTab isConfigured={isConfigured} />
        )}
        {activeTab === 'image' && (
          <ImageTab isConfigured={isConfigured} />
        )}
        {activeTab === 'coding' && (
          <CodingBuddyTab isConfigured={isConfigured} />
        )}
      </div>
    </div>
  );
} 