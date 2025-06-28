'use client';

import { useState, useEffect } from 'react';
import { useSession } from '../hooks/useSession';

interface ConfigSectionProps {
  onSubmit: (config: { apiKey: string; model: string; imageModel: string }) => void;
  availableModels: string[];
  currentConfig?: { apiKey: string; model: string; imageModel: string };
}

export default function ConfigSection({ onSubmit, availableModels, currentConfig }: ConfigSectionProps) {
  const { sessionData, saveOpenAIConfig } = useSession();
  
  const [apiKey, setApiKey] = useState(currentConfig?.apiKey || '');
  const [model, setModel] = useState(currentConfig?.model || availableModels[0] || 'gpt-4o');
  const [imageModel, setImageModel] = useState(currentConfig?.imageModel || 'dall-e-3');
  const [isLoading, setIsLoading] = useState(false);

  // Load persisted config from session data when it becomes available
  useEffect(() => {
    if (sessionData) {
      if (sessionData.openaiApiKey && !currentConfig?.apiKey) {
        setApiKey(sessionData.openaiApiKey);
      }
      if (sessionData.chatModel && !currentConfig?.model) {
        setModel(sessionData.chatModel);
      }
      if (sessionData.imageModel && !currentConfig?.imageModel) {
        setImageModel(sessionData.imageModel);
      }
    }
  }, [sessionData, currentConfig]);

  // Save config to session when it changes
  useEffect(() => {
    if (apiKey && sessionData?.sessionId) {
      saveOpenAIConfig(apiKey, model, imageModel);
    }
  }, [apiKey, model, imageModel, sessionData?.sessionId, saveOpenAIConfig]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    
    try {
      await onSubmit({ apiKey, model, imageModel });
    } catch (error) {
      console.error('Configuration error:', error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="bg-gray-50 p-6 border-b border-gray-200 flex-shrink-0">
      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="flex gap-3 items-center flex-wrap">
          <div className="flex-1 min-w-[200px]">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              OpenAI API Key
            </label>
            <input
              type="password"
              value={apiKey}
              onChange={(e) => setApiKey(e.target.value)}
              placeholder="Enter your OpenAI API key"
              className="config-input"
              required
            />
          </div>
          
          <div className="min-w-[150px]">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Chat Model
            </label>
            <select
              value={model}
              onChange={(e) => setModel(e.target.value)}
              className="config-input"
            >
              {availableModels.map((m) => (
                <option key={m} value={m}>
                  {m}
                </option>
              ))}
            </select>
          </div>

          <div className="min-w-[150px]">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Image Model
            </label>
            <select
              value={imageModel}
              onChange={(e) => setImageModel(e.target.value)}
              className="config-input"
            >
              <option value="dall-e-3">DALL-E 3</option>
              <option value="dall-e-2">DALL-E 2</option>
            </select>
          </div>

          <div className="flex items-end">
            <button
              type="submit"
              disabled={isLoading || !apiKey.trim()}
              className="configure-btn"
            >
              {isLoading ? 'Configuring...' : 'Configure'}
            </button>
          </div>
        </div>
        
        {currentConfig?.apiKey && (
          <div className="text-sm text-gray-600">
            <p>✅ API Key is configured</p>
            <p>Current models: {currentConfig.model} (chat) / {currentConfig.imageModel} (image)</p>
          </div>
        )}
      </form>
    </div>
  );
} 