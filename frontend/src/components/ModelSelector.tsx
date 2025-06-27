'use client';

interface ModelSelectorProps {
  currentModel: string;
  currentImageModel: string;
  availableModels: string[];
  imageModels: string[];
  onModelChange: (model: string) => void;
  onImageModelChange: (model: string) => void;
}

export default function ModelSelector({
  currentModel,
  currentImageModel,
  availableModels,
  imageModels,
  onModelChange,
  onImageModelChange
}: ModelSelectorProps) {
  return (
    <div className="bg-gray-50 p-4 border-b border-gray-200 flex-shrink-0">
      <div className="flex items-center gap-6 flex-wrap justify-center">
        <div className="flex items-center gap-2">
          <span className="text-sm font-medium text-gray-700">Chat Model:</span>
          <select
            value={currentModel}
            onChange={(e) => onModelChange(e.target.value)}
            className="px-3 py-1 border border-gray-300 rounded-md focus:outline-none focus:border-blue-500 bg-white text-gray-900"
          >
            {availableModels.map((model) => (
              <option key={model} value={model}>
                {model}
              </option>
            ))}
          </select>
        </div>
        
        <div className="flex items-center gap-2">
          <span className="text-sm font-medium text-gray-700">Image Model:</span>
          <select
            value={currentImageModel}
            onChange={(e) => onImageModelChange(e.target.value)}
            className="px-3 py-1 border border-gray-300 rounded-md focus:outline-none focus:border-blue-500 bg-white text-gray-900"
          >
            {imageModels.map((model) => (
              <option key={model} value={model}>
                {model}
              </option>
            ))}
          </select>
        </div>
      </div>
    </div>
  );
} 