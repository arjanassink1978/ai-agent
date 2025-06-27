'use client';

interface ModelSelectorProps {
  currentModel: string;
  currentImageModel: string;
  availableModels: string[];
  imageModels: string[];
  onModelChange: (model: string) => void;
  onImageModelChange: (imageModel: string) => void;
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
      <div className="flex items-center gap-4 flex-wrap justify-center">
        <div className="flex items-center gap-2">
          <span className="text-sm text-gray-600">Chat Model:</span>
          <span className="font-medium">{currentModel}</span>
          <select
            onChange={(e) => onModelChange(e.target.value)}
            className="px-2 py-1 border border-gray-300 rounded text-sm focus:outline-none focus:border-blue-500"
          >
            {availableModels.map((model) => (
              <option key={model} value={model} selected={model === currentModel}>
                {model}
              </option>
            ))}
          </select>
          <button
            onClick={() => onModelChange(currentModel)}
            className="bg-blue-500 text-white px-3 py-1 rounded text-sm hover:bg-blue-600 transition-colors"
          >
            Change
          </button>
        </div>
        
        <div className="flex items-center gap-2">
          <span className="text-sm text-gray-600">Image Model:</span>
          <span className="font-medium">{currentImageModel}</span>
          <select
            onChange={(e) => onImageModelChange(e.target.value)}
            className="px-2 py-1 border border-gray-300 rounded text-sm focus:outline-none focus:border-blue-500"
          >
            {imageModels.map((model) => (
              <option key={model} value={model} selected={model === currentImageModel}>
                {model}
              </option>
            ))}
          </select>
          <button
            onClick={() => onImageModelChange(currentImageModel)}
            className="bg-blue-500 text-white px-3 py-1 rounded text-sm hover:bg-blue-600 transition-colors"
          >
            Change
          </button>
        </div>
      </div>
    </div>
  );
} 