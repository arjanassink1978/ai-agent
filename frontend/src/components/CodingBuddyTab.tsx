'use client';

import React, { useState, useRef, useEffect } from 'react';
import { ChatMessage } from '../types/chat';
import { Repository } from '../types/repository';
import RepositorySelector from './RepositorySelector';

interface CodingBuddyTabProps {
  isConfigured: boolean;
}

export default function CodingBuddyTab({ isConfigured }: CodingBuddyTabProps) {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [inputMessage, setInputMessage] = useState('');
  const [personalAccessToken, setPersonalAccessToken] = useState('');
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isConnecting, setIsConnecting] = useState(false);
  const [repositories, setRepositories] = useState<Repository[]>([]);
  const [selectedRepository, setSelectedRepository] = useState<Repository | null>(null);
  const [isConnected, setIsConnected] = useState(false);
  const [connectionStatus, setConnectionStatus] = useState<'idle' | 'connecting' | 'connected' | 'error'>('idle');
  const [selectedFiles, setSelectedFiles] = useState<string[]>([]);
  const [availableFiles, setAvailableFiles] = useState<string[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [authError, setAuthError] = useState('');
  const [userInfo, setUserInfo] = useState<{username: string, name: string} | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Initialize welcome message on client side to avoid hydration issues
  useEffect(() => {
    if (messages.length === 0) {
      setMessages([
        {
          id: '1',
          content: "Hello! I'm your Coding Buddy. Enter your GitHub Personal Access Token to get started. I'll help you with best practices, code reviews, refactoring suggestions, and development tasks.",
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

  const addMessage = (content: string, sender: 'user' | 'assistant', type: 'text' | 'code' | 'file' = 'text') => {
    const newMessage: ChatMessage = {
      id: Date.now().toString(),
      content,
      sender,
      timestamp: new Date(),
      type
    };
    setMessages(prev => [...prev, newMessage]);
  };

  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (!inputMessage.trim() || !isConfigured || !isConnected || !selectedRepository) return;

    const userMessage = inputMessage.trim();
    addMessage(userMessage, 'user');
    setInputMessage('');
    setIsLoading(true);

    try {
      const response = await fetch('/api/coding-chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          message: userMessage,
          model: 'gpt-4',
          metadata: {
            personalAccessToken,
            repositoryUrl: selectedRepository.url || selectedRepository.html_url,
            selectedFiles: selectedFiles.length > 0 ? selectedFiles : null
          }
        })
      });

      if (response.ok) {
        const data = await response.json();
        addMessage(data.message, 'assistant');
      } else {
        addMessage('Sorry, I encountered an error. Please try again.', 'assistant');
      }
    } catch (error) {
      console.error('Error sending message:', error);
      addMessage('Sorry, I encountered an error. Please try again.', 'assistant');
    } finally {
      setIsLoading(false);
    }
  };

  const authenticateGitHub = async () => {
    if (!personalAccessToken.trim()) {
      setAuthError('Personal Access Token is required');
      return;
    }

    setIsConnecting(true);
    setAuthError('');

    try {
      const response = await fetch('/api/github/authenticate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ personalAccessToken }),
      });

      const data = await response.json();

      if (data.success) {
        setIsAuthenticated(true);
        setAuthError('');
        setUserInfo({
          username: data.username,
          name: data.name
        });
        addMessage(`Successfully authenticated as ${data.name} (@${data.username})`, 'assistant');
        await fetchUserRepositories();
      } else {
        setAuthError(data.error || 'Authentication failed');
        setIsAuthenticated(false);
      }
    } catch (error) {
      console.error('Error authenticating:', error);
      setAuthError('Authentication failed. Please check your Personal Access Token.');
      setIsAuthenticated(false);
    } finally {
      setIsConnecting(false);
    }
  };

  const fetchUserRepositories = async () => {
    try {
      const response = await fetch('/api/github/repositories', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ personalAccessToken }),
      });

      const data = await response.json();

      if (data.success) {
        setRepositories(data.repositories || []);
        addMessage(`Found ${data.repositories?.length || 0} repositories. Select one to connect.`, 'assistant');
      } else {
        addMessage('Failed to fetch repositories: ' + (data.error || 'Unknown error'), 'assistant');
      }
    } catch (error) {
      console.error('Error fetching repositories:', error);
      addMessage('Failed to fetch repositories. Please try again.', 'assistant');
    }
  };

  const connectToRepository = async (repo: Repository) => {
    console.log('Connecting to repository:', repo);
    console.log('Repository full_name:', repo.full_name);
    console.log('Repository url:', repo.url);
    console.log('Repository html_url:', repo.html_url);
    console.log('Repository url type:', typeof repo.url);
    console.log('Repository html_url type:', typeof repo.html_url);
    console.log('Personal Access Token length:', personalAccessToken?.length);
    setConnectionStatus('connecting');
    setSelectedRepository(repo);

    // Use url field from MCP server, fallback to html_url
    const repositoryUrl = repo.url || repo.html_url;
    
    if (!repositoryUrl) {
      console.error('No repository URL found in repository object');
      setConnectionStatus('error');
      addMessage('Error: No repository URL found. Please try again.', 'assistant');
      return;
    }

    try {
      const requestBody = { 
        personalAccessToken, 
        repositoryUrl: repositoryUrl
      };
      console.log('Request body being sent:', JSON.stringify(requestBody, null, 2));
      console.log('Request body keys:', Object.keys(requestBody));
      console.log('Request body repositoryUrl value:', requestBody.repositoryUrl);
      console.log('Request body repositoryUrl type:', typeof requestBody.repositoryUrl);
      
      const response = await fetch('/api/connect-repository', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestBody),
      });

      console.log('Response status:', response.status);
      const data = await response.json();
      console.log('Response data:', data);

      if (data.success) {
        setIsConnected(true);
        setConnectionStatus('connected');
        setAvailableFiles(data.files || []);
        
        addMessage(
          `Connected to repository: ${repo.full_name}. I'm your coding buddy and I'll help you with best practices, code reviews, refactoring suggestions, and development tasks. I have access to the codebase context and can analyze files, suggest improvements, and help with debugging.`,
          'assistant'
        );
      } else {
        setConnectionStatus('error');
        addMessage('Failed to connect to repository: ' + (data.error || 'Unknown error'), 'assistant');
      }
    } catch (error) {
      setConnectionStatus('error');
      console.error('Error connecting to repository:', error);
      addMessage('Error connecting to repository. Please try again.', 'assistant');
    }
  };

  const disconnectFromRepository = () => {
    setIsConnected(false);
    setConnectionStatus('idle');
    setSelectedRepository(null);
    setSelectedFiles([]);
    setAvailableFiles([]);
    addMessage('Disconnected from repository. Select a new repository to continue.', 'assistant');
  };

  const logout = () => {
    setIsAuthenticated(false);
    setIsConnected(false);
    setConnectionStatus('idle');
    setSelectedRepository(null);
    setSelectedFiles([]);
    setAvailableFiles([]);
    setRepositories([]);
    setAuthError('');
    setUserInfo(null);
    addMessage('Logged out. Authenticate again to continue.', 'assistant');
  };

  const selectFile = (filePath: string) => {
    setSelectedFiles(prev => 
      prev.includes(filePath) 
        ? prev.filter(f => f !== filePath)
        : [...prev, filePath]
    );
  };

  const analyzeSelectedFiles = () => {
    if (selectedFiles.length > 0) {
      const message = `Please analyze these files and provide insights: ${selectedFiles.join(', ')}`;
      addMessage(message, 'user');
      setInputMessage('');
      setIsLoading(true);

      // Simulate the same API call as handleSendMessage
      fetch('/api/coding-chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          message,
          model: 'gpt-4',
          metadata: {
            personalAccessToken,
            repositoryUrl: selectedRepository?.url || selectedRepository?.html_url,
            selectedFiles
          }
        })
      })
      .then(response => {
        if (response.ok) {
          return response.json();
        }
        throw new Error('Failed to analyze files');
      })
      .then(data => {
        addMessage(data.message, 'assistant');
      })
      .catch(error => {
        console.error('Error analyzing files:', error);
        addMessage('Sorry, I encountered an error analyzing the files.', 'assistant');
      })
      .finally(() => {
        setIsLoading(false);
      });
    }
  };

  if (!isConfigured) {
    return (
      <div className="flex flex-col h-full">
        <div className="flex-1 p-6 overflow-y-auto bg-gray-50">
          <div className="text-center text-gray-600">
            Please configure the AI service to use the Coding Buddy feature.
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full max-h-screen">
      {/* GitHub Authentication Section */}
      {!isAuthenticated ? (
        <div className="bg-white border-b border-gray-200 p-4 flex-shrink-0">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">GitHub Authentication</h3>
          <div className="space-y-4">
            <div className="mb-6">
              <label htmlFor="github-token" className="block text-sm font-medium text-gray-700 mb-2">
                Personal Access Token
              </label>
              <input
                type="password"
                id="github-token"
                value={personalAccessToken}
                onChange={(e) => setPersonalAccessToken(e.target.value)}
                placeholder="ghp_xxxxxxxxxxxxxxxxxxxx"
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                disabled={isConnecting}
              />
              <p className="mt-1 text-xs text-gray-500">
                Enter your GitHub Personal Access Token to connect to repositories
              </p>
            </div>

            {authError && (
              <div className="text-red-600 text-sm">{authError}</div>
            )}
            <button
              onClick={authenticateGitHub}
              disabled={!personalAccessToken.trim() || isConnecting}
              className="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed mb-4"
            >
              {isConnecting ? 'Connecting...' : 'Connect to GitHub'}
            </button>
          </div>
        </div>
      ) : (
        <>
          {/* Repository Selection Section */}
          {!isConnected ? (
            <div className="bg-white border-b border-gray-200 p-4 flex-shrink-0">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold text-gray-900">Select Repository</h3>
                <div className="flex items-center space-x-2">
                  {userInfo && (
                    <span className="text-sm text-gray-600">
                      Logged in as {userInfo.name} (@{userInfo.username})
                    </span>
                  )}
                  <button
                    onClick={logout}
                    className="li-btn bg-red-600 hover:bg-red-700"
                  >
                    Logout
                  </button>
                </div>
              </div>
              
              {connectionStatus === 'connecting' && (
                <div className="mb-4 p-3 bg-blue-50 border border-blue-200 rounded-md">
                  <div className="flex items-center space-x-2">
                    <div className="w-4 h-4 border-2 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
                    <span className="text-sm text-blue-700">
                      Connecting to {selectedRepository?.full_name}...
                    </span>
                  </div>
                </div>
              )}
              
              {repositories.length > 0 ? (
                <div className="space-y-4">
                  <RepositorySelector
                    repositories={repositories}
                    selectedRepository={selectedRepository}
                    onRepositoryChange={(repo) => {
                      setSelectedRepository(repo);
                      if (repo) {
                        connectToRepository(repo);
                      }
                    }}
                    disabled={connectionStatus === 'connecting'}
                    placeholder="Choose a repository to connect..."
                  />
                  
                  {selectedRepository && (
                    <div className="bg-gray-50 p-3 rounded-md">
                      <h4 className="font-medium text-gray-900 mb-1">{selectedRepository.name}</h4>
                      <p className="text-sm text-gray-600 mb-2">{selectedRepository.description || 'No description'}</p>
                      <div className="flex items-center space-x-4 text-xs text-gray-500">
                        <span>{selectedRepository.language || 'Unknown'}</span>
                        <span>{selectedRepository.private ? 'Private' : 'Public'}</span>
                        <span>Updated {new Date(selectedRepository.updated_at).toLocaleDateString()}</span>
                      </div>
                    </div>
                  )}
                </div>
              ) : (
                <div className="text-center text-gray-600 py-4">
                  No repositories found. Please check your GitHub account.
                </div>
              )}
            </div>
          ) : (
            /* Connected Repository Section */
            <div className="bg-white border-b border-gray-200 p-4 flex-shrink-0">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold text-gray-900">Connected Repository</h3>
                <div className="flex items-center space-x-2">
                  <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                    Connected
                  </span>
                  <button
                    onClick={disconnectFromRepository}
                    className="li-btn bg-red-600 hover:bg-red-700"
                  >
                    Disconnect
                  </button>
                </div>
              </div>
              <div>
                <p className="text-sm text-gray-600">Connected to:</p>
                <p className="text-sm font-medium text-gray-900">{selectedRepository?.full_name}</p>
                <p className="text-sm text-gray-600">{selectedRepository?.description}</p>
              </div>
            </div>
          )}

          {/* File Selection Section */}
          {isConnected && availableFiles.length > 0 && (
            <div className="bg-white border-b border-gray-200 p-4 flex-shrink-0">
              <h4 className="text-md font-semibold text-gray-900 mb-3">Select Files to Analyze</h4>
              <div className="max-h-48 overflow-y-auto space-y-2 border border-gray-200 rounded-md p-2">
                {availableFiles.map((file) => (
                  <label key={file} className="flex items-center space-x-2 cursor-pointer hover:bg-gray-50 p-1 rounded">
                    <input
                      type="checkbox"
                      checked={selectedFiles.includes(file)}
                      onChange={() => selectFile(file)}
                      className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                    />
                    <span className="text-sm text-gray-700 truncate">{file}</span>
                  </label>
                ))}
              </div>
              {selectedFiles.length > 0 && (
                <button
                  onClick={analyzeSelectedFiles}
                  className="li-btn mt-3 w-full"
                >
                  Analyze Selected Files ({selectedFiles.length})
                </button>
              )}
            </div>
          )}
        </>
      )}

      {/* Chat Messages */}
      <div className="flex-1 p-6 overflow-y-auto bg-gray-50" style={{ minHeight: '300px', maxHeight: 'calc(100vh - 400px)' }}>
        <div className="space-y-4">
          {messages.map((message, index) => (
            <div key={`${message.sender}-${index}-${message.timestamp.getTime()}`} className={`flex ${message.sender === 'user' ? 'justify-end' : 'justify-start'}`}>
              <div className={message.sender === 'user' ? 'chat-bubble-user' : 'chat-bubble-assistant'}>
                <div className="whitespace-pre-wrap">{message.content}</div>
                <div className={message.sender === 'user' ? 'chat-timestamp-user' : 'chat-timestamp-assistant'}>
                  {message.timestamp.toLocaleTimeString('en-US', { 
                    hour: '2-digit', 
                    minute: '2-digit',
                    hour12: false 
                  })}
                </div>
              </div>
            </div>
          ))}
          
          {isLoading && (
            <div className="flex justify-start">
              <div className="typing-indicator">
                <div className="w-2 h-2 bg-blue-500 rounded-full animate-bounce"></div>
                <div className="w-2 h-2 bg-blue-500 rounded-full animate-bounce" style={{ animationDelay: '0.1s' }}></div>
                <div className="w-2 h-2 bg-blue-500 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }}></div>
              </div>
            </div>
          )}
        </div>
        <div ref={messagesEndRef} />
      </div>

      {/* Input Section */}
      <div className="p-6 bg-white border-t border-gray-200 flex-shrink-0">
        <form onSubmit={handleSendMessage} className="flex gap-3">
          <input
            type="text"
            placeholder={isConnected ? "Ask your coding buddy for help, code reviews, refactoring suggestions, or any development questions..." : "Enter your GitHub Personal Access Token to start chatting with your coding buddy"}
            value={inputMessage}
            onChange={(e) => setInputMessage(e.target.value)}
            disabled={!isConfigured || !isConnected || isLoading}
            className="chat-input"
          />
          <button
            type="submit"
            disabled={!isConfigured || !isConnected || !inputMessage.trim() || isLoading}
            className="bg-gradient-to-r from-blue-500 to-purple-600 text-white rounded-full w-12 h-12 flex items-center justify-center hover:scale-105 transition-transform disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:scale-100"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <line x1="22" y1="2" x2="11" y2="13"></line>
              <polygon points="22,2 15,22 11,13 2,9"></polygon>
            </svg>
          </button>
        </form>
      </div>
    </div>
  );
} 