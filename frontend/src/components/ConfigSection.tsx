'use client';

import { useState } from 'react';
import { Config } from './ChatInterface';

interface ConfigSectionProps {
  onSubmit: (config: Config) => void;
  availableModels: string[];
}

export default function ConfigSection({ onSubmit, availableModels }: ConfigSectionProps) {
  const [apiKey, setApiKey] = useState('');
  const [model, setModel] = useState('gpt-4');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (apiKey.trim()) {
      onSubmit({
        apiKey: apiKey.trim(),
        model,
        imageModel: 'dall-e-3'
      });
    }
  };

  return (
    <div className="bg-gray-50 p-6 border-b border-gray-200 flex-shrink-0">
      <form onSubmit={handleSubmit} className="flex gap-3 items-center flex-wrap">
        <input
          type="password"
          value={apiKey}
          onChange={(e) => setApiKey(e.target.value)}
          placeholder="Enter your OpenAI API key"
          className="flex-1 min-w-[200px] px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500"
          required
        />
        <select
          value={model}
          onChange={(e) => setModel(e.target.value)}
          className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500 min-w-[150px]"
        >
          {availableModels.map((modelOption) => (
            <option key={modelOption} value={modelOption}>
              {modelOption}
            </option>
          ))}
        </select>
        <button
          type="submit"
          className="bg-green-500 text-white px-5 py-2 rounded-lg hover:bg-green-600 transition-colors"
        >
          Configure
        </button>
      </form>
    </div>
  );
} 