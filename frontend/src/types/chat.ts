export interface ChatMessage {
  id: string;
  content: string;
  sender: 'user' | 'assistant';
  timestamp: Date;
  type?: 'text' | 'image' | 'code' | 'file';
  imageUrl?: string;
  prompt?: string;
  fileName?: string;
  fileContent?: string;
}

export interface ChatSession {
  id: string;
  title: string;
  model: string;
  imageModel: string;
  context?: string;
  createdAt: Date;
  updatedAt: Date;
}

export interface RepositoryConnection {
  repositoryUrl: string;
  files: string[];
  isConnected: boolean;
  lastSync?: Date;
} 