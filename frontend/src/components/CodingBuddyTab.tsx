'use client';

import { useState, useRef, useEffect, useCallback } from 'react';
import { ChatMessage } from '../types/chat';
import { useSession } from '../hooks/useSession';

interface CodingBuddyTabProps {
  isConfigured: boolean;
}

interface Repository {
  name: string;
  full_name: string;
  description: string;
  private: boolean;
  html_url: string;
  clone_url: string;
  language: string;
  updated_at: string;
}

export default function CodingBuddyTab({ isConfigured }: CodingBuddyTabProps) {
  const { sessionData, isLoading: sessionLoading, saveGithubToken, saveGithubUser, saveSelectedRepository, saveRepositories } = useSession();
  
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [inputMessage, setInputMessage] = useState('');
  const [personalAccessToken, setPersonalAccessToken] = useState('');
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isConnecting, setIsConnecting] = useState(false);
  const [repositories, setRepositories] = useState<Repository[]>([]);
  const [selectedRepository, setSelectedRepository] = useState<Repository | null>(null);
  const [isConnected, setIsConnected] = useState(false);
  const [connectionStatus, setConnectionStatus] = useState<'idle' | 'connecting' | 'connected' | 'error'>('idle');
  const [userInfo, setUserInfo] = useState<{ username: string; name: string } | null>(null);
  const [authError, setAuthError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Load persisted state from session data when it becomes available
  useEffect(() => {
    if (sessionData) {
      try {
        // Load GitHub token
        if (sessionData.githubToken) {
          setPersonalAccessToken(sessionData.githubToken);
        }

        // Load user info and set authentication state
        if (sessionData.githubUsername && sessionData.githubDisplayName) {
          setUserInfo({
            username: sessionData.githubUsername,
            name: sessionData.githubDisplayName
          });
          setIsAuthenticated(true);
        }

        // Load repositories first
        if (sessionData.repositories) {
          setRepositories(sessionData.repositories as Repository[]);
        }

        // Initialize with welcome message if no messages exist
        if (messages.length === 0) {
          setMessages([
            {
              id: '1',
              content: "Hello! I'm your AI Coding Buddy with full GitHub integration. Enter your GitHub Personal Access Token to get started.\n\nI can help you with:\n• Creating issues and pull requests\n• Searching and analyzing code\n• Managing branches and commits\n• Code reviews and refactoring\n• And much more!\n\nJust tell me what you want to do in natural language, like:\n• \"Create an issue about the slow performance\"\n• \"Make a pull request for the new feature\"\n• \"Search for all API endpoints\"\n• \"Create a new branch for bug fixes\"",
              sender: 'assistant',
              timestamp: new Date(),
              type: 'text'
            }
          ]);
        }
      } catch (error) {
        console.error('Error loading session data:', error);
        // Fallback to welcome message if loading fails
        setMessages([
          {
            id: '1',
            content: "Hello! I'm your AI Coding Buddy with full GitHub integration. Enter your GitHub Personal Access Token to get started.\n\nI can help you with:\n• Creating issues and pull requests\n• Searching and analyzing code\n• Managing branches and commits\n• Code reviews and refactoring\n• And much more!\n\nJust tell me what you want to do in natural language, like:\n• \"Create an issue about the slow performance\"\n• \"Make a pull request for the new feature\"\n• \"Search for all API endpoints\"\n• \"Create a new branch for bug fixes\"",
            sender: 'assistant',
            timestamp: new Date(),
            type: 'text'
          }
        ]);
      }
    }
  }, [sessionData, messages.length]);

  // Ensure authentication state is properly set when we have token and user info
  useEffect(() => {
    if (personalAccessToken && userInfo && !isAuthenticated) {
      setIsAuthenticated(true);
    }
  }, [personalAccessToken, userInfo, isAuthenticated]);

  // Load selected repository after repositories are loaded and set connection state
  useEffect(() => {
    if (sessionData?.selectedRepository && repositories.length > 0) {
      const repo = repositories.find(r => r.full_name === sessionData.selectedRepository);
      if (repo) {
        setSelectedRepository(repo);
        setIsConnected(true);
        setConnectionStatus('connected');
        
        // Add a message indicating successful reconnection if we have user info
        if (userInfo && messages.length === 1) {
          setMessages(prev => [
            ...prev,
            {
              id: '2',
              content: `Welcome back! You're connected to ${repo.full_name} as ${userInfo.name} (@${userInfo.username}).\n\nI'm ready to help you with your coding tasks. Just tell me what you want to do!`,
              sender: 'assistant',
              timestamp: new Date(),
              type: 'text'
            }
          ]);
        }
      }
    }
  }, [sessionData?.selectedRepository, repositories, userInfo, messages.length]);

  // Save GitHub token to session when it changes
  useEffect(() => {
    if (personalAccessToken && sessionData?.sessionId) {
      saveGithubToken(personalAccessToken);
    }
  }, [personalAccessToken, sessionData?.sessionId, saveGithubToken]);

  // Save user info to session when it changes
  useEffect(() => {
    if (userInfo && sessionData?.sessionId) {
      saveGithubUser(userInfo.username, userInfo.name);
    }
  }, [userInfo, sessionData?.sessionId, saveGithubUser]);

  // Save selected repository to session when it changes
  useEffect(() => {
    if (selectedRepository && sessionData?.sessionId) {
      saveSelectedRepository(selectedRepository.full_name);
    }
  }, [selectedRepository, sessionData?.sessionId, saveSelectedRepository]);

  // Save repositories to session when they change
  useEffect(() => {
    if (repositories.length > 0 && sessionData?.sessionId) {
      saveRepositories(repositories);
    }
  }, [repositories, sessionData?.sessionId, saveRepositories]);

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
      const response = await fetch('/api/agent/coding-buddy', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          message: userMessage,
          username: userInfo?.username || '',
          repository: selectedRepository.full_name,
          personalAccessToken: personalAccessToken
        })
      });

      if (response.ok) {
        const data = await response.json();
        if (data.success) {
          addMessage(data.message, 'assistant');
          
          // Add links if available
          if (data.links && data.links.length > 0) {
            const linksMessage = data.links.map((link: string) => `🔗 ${link}`).join('\n');
            addMessage(linksMessage, 'assistant');
          }
        } else {
          addMessage(data.message || 'Sorry, I encountered an error. Please try again.', 'assistant');
        }
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
        console.log('Authentication successful, fetching repositories...');
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

  const fetchUserRepositories = useCallback(async () => {
    console.log('Fetching user repositories...');
    try {
      const response = await fetch('/api/github/repositories', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ personalAccessToken }),
      });

      const data = await response.json();
      console.log('Repository fetch response:', data);

      if (data.success) {
        setRepositories(data.repositories || []);
        console.log('Repositories set:', data.repositories?.length || 0);
        addMessage(`Found ${data.repositories?.length || 0} repositories. Select one to connect.`, 'assistant');
      } else {
        addMessage('Failed to fetch repositories: ' + (data.error || 'Unknown error'), 'assistant');
      }
    } catch (error) {
      console.error('Error fetching repositories:', error);
      addMessage('Failed to fetch repositories. Please try again.', 'assistant');
    }
  }, [personalAccessToken, addMessage]);

  // Fallback: If we have authentication but no repositories, fetch them
  useEffect(() => {
    if (isAuthenticated && userInfo && personalAccessToken && repositories.length === 0 && !sessionLoading) {
      console.log('Fetching repositories as fallback...');
      fetchUserRepositories();
    }
  }, [isAuthenticated, userInfo, personalAccessToken, repositories.length, sessionLoading, fetchUserRepositories]);

  const connectToRepository = async (repo: Repository) => {
    setConnectionStatus('connecting');
    setSelectedRepository(repo);

    try {
      const response = await fetch('/api/connect-repository', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ 
          personalAccessToken, 
          repositoryUrl: repo.html_url 
        }),
      });

      const data = await response.json();

      if (data.success) {
        setIsConnected(true);
        setConnectionStatus('connected');
        addMessage(
          `Connected to repository: ${repo.full_name}. I'm your AI coding buddy with full GitHub integration!\n\nI can now help you with:\n• Creating issues and pull requests\n• Searching and analyzing code\n• Managing branches and commits\n• Code reviews and refactoring\n• And much more!\n\nJust tell me what you want to do in natural language. For example:\n• \"Create an issue about the slow performance\"\n• \"Make a pull request for the new feature\"\n• \"Search for all API endpoints\"\n• \"Create a new branch for bug fixes\"`,
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
    // Clear repository-related session data
    if (sessionData?.sessionId) {
      saveSelectedRepository('');
    }
    addMessage('Disconnected from repository. Select a new repository to continue.', 'assistant');
  };

  const logout = () => {
    setIsAuthenticated(false);
    setIsConnected(false);
    setConnectionStatus('idle');
    setRepositories([]);
    setSelectedRepository(null);
    setUserInfo(null);
    setPersonalAccessToken('');
    // Clear all session data
    if (sessionData?.sessionId) {
      saveGithubToken('');
      saveGithubUser('', '');
      saveSelectedRepository('');
      saveRepositories([]);
    }
    addMessage('Logged out. Authenticate again to continue.', 'assistant');
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

  // Show loading state while session is being loaded
  if (sessionLoading) {
    return (
      <div className="flex flex-col h-full">
        <div className="flex-1 p-6 overflow-y-auto bg-gray-50">
          <div className="flex items-center justify-center h-full">
            <div className="text-center">
              <div className="w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
              <p className="text-gray-600">Loading your session...</p>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full">
      {/* GitHub Authentication Section */}
      {!isAuthenticated ? (
        <div className="bg-white border-b border-gray-200 p-6 flex-shrink-0">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">GitHub Authentication</h3>
          <div className="space-y-4">
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Personal Access Token
              </label>
              <input
                type="password"
                value={personalAccessToken}
                onChange={(e) => setPersonalAccessToken(e.target.value)}
                placeholder="ghp_xxxxxxxxxxxxxxxxxxxx"
                className="config-input"
                disabled={isConnecting}
                autoComplete="off"
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
            <div className="bg-white border-b border-gray-200 p-6 flex-shrink-0">
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
                  <div className="text-sm text-gray-600 mb-2">
                    Found {repositories.length} repositories
                  </div>
                  <div>
                    <label htmlFor="repository-select" className="block text-sm font-medium text-gray-700 mb-2">
                      Choose Repository
                    </label>
                    <select
                      id="repository-select"
                      value={selectedRepository?.full_name || ''}
                      onChange={(e) => {
                        const selectedRepo = repositories.find(repo => repo.full_name === e.target.value);
                        if (selectedRepo) {
                          connectToRepository(selectedRepo);
                        }
                      }}
                      disabled={connectionStatus === 'connecting'}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      <option value="">Select a repository...</option>
                      {repositories.map((repo) => (
                        <option key={repo.full_name} value={repo.full_name}>
                          {repo.name} {repo.private ? '(Private)' : '(Public)'} - {repo.language || 'Unknown'}
                        </option>
                      ))}
                    </select>
                    <p className="mt-1 text-xs text-gray-500">
                      Select a repository to automatically connect and start coding
                    </p>
                  </div>
                  
                  {selectedRepository && (
                    <div className="p-3 bg-gray-50 border border-gray-200 rounded-md">
                      <h4 className="font-medium text-gray-900">{selectedRepository.name}</h4>
                      <p className="text-sm text-gray-600">{selectedRepository.description || 'No description'}</p>
                      <div className="flex items-center space-x-4 mt-1">
                        <span className="text-xs text-gray-500">{selectedRepository.language || 'Unknown'}</span>
                        <span className="text-xs text-gray-500">
                          {selectedRepository.private ? 'Private' : 'Public'}
                        </span>
                        <span className="text-xs text-gray-500">
                          Updated {new Date(selectedRepository.updated_at).toLocaleDateString()}
                        </span>
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
            <div className="bg-white border-b border-gray-200 p-6 flex-shrink-0">
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
        </>
      )}

      {/* Chat Messages */}
      <div className="flex-1 p-6 overflow-y-auto bg-gray-50">
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
            placeholder={isConnected ? "Tell me what you want to do! Examples: 'Create an issue about slow performance', 'Make a PR for the new feature', 'Search for API endpoints', 'Create a branch for bug fixes'..." : "Enter your GitHub Personal Access Token to start chatting with your coding buddy"}
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