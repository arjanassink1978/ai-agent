'use client';

import { useState, useRef } from 'react';

interface FileUploadProps {
  onFileUpload: (file: File, context: 'chat' | 'image', prompt?: string) => void;
  context: 'chat' | 'image';
  disabled?: boolean;
  accept?: string;
  className?: string;
}

export default function FileUpload({ 
  onFileUpload, 
  context, 
  disabled = false, 
  accept = "*/*",
  className = ""
}: FileUploadProps) {
  const [isUploading, setIsUploading] = useState(false);
  const [prompt, setPrompt] = useState('');
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileSelect = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    setIsUploading(true);
    try {
      if (context === 'image') {
        onFileUpload(file, context, prompt);
      } else {
        onFileUpload(file, context);
      }
    } catch (error) {
      console.error('File upload error:', error);
    } finally {
      setIsUploading(false);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  const handleClick = () => {
    fileInputRef.current?.click();
  };

  return (
    <div className={`flex items-center gap-2 ${className}`}>
      {context === 'image' && (
        <input
          type="text"
          placeholder="Optional prompt for image generation..."
          value={prompt}
          onChange={(e) => setPrompt(e.target.value)}
          className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500 text-sm"
          disabled={disabled}
        />
      )}
      
      <input
        ref={fileInputRef}
        type="file"
        accept={accept}
        onChange={handleFileSelect}
        className="hidden"
        disabled={disabled}
      />
      
      <button
        type="button"
        onClick={handleClick}
        disabled={disabled || isUploading}
        className="bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed text-sm flex items-center gap-2"
      >
        {isUploading ? (
          <>
            <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
            Uploading...
          </>
        ) : (
          <>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
              <polyline points="7,10 12,15 17,10"></polyline>
              <line x1="12" y1="15" x2="12" y2="3"></line>
            </svg>
            {context === 'chat' ? 'Upload File' : 'Upload Image'}
          </>
        )}
      </button>
    </div>
  );
} 