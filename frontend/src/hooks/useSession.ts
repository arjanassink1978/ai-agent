import { useState, useEffect, useCallback } from 'react';

interface SessionData {
  sessionId: string;
  githubToken?: string;
  githubUsername?: string;
  githubDisplayName?: string;
  selectedRepository?: string;
  repositories?: Repository[];
  openaiApiKey?: string;
  chatModel?: string;
  imageModel?: string;
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

export const useSession = () => {
  const [sessionData, setSessionData] = useState<SessionData | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadSessionData = useCallback(async (sessionId: string) => {
    try {
      const response = await fetch(`/api/session/${sessionId}`);
      if (response.ok) {
        const data = await response.json();
        console.log('[Session] Loaded session data from backend:', data);
        setSessionData(data);
      } else {
        throw new Error('Failed to load session data');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load session data');
    }
  }, []);

  // Generate or get existing session ID
  const initializeSession = useCallback(async () => {
    try {
      // Check if we have a session ID in localStorage
      let sessionId = localStorage.getItem('session-id');
      
      if (!sessionId) {
        // Generate new session
        const response = await fetch('/api/session/generate', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
        });
        
        if (response.ok) {
          const data = await response.json();
          sessionId = data.sessionId;
          if (sessionId) {
            localStorage.setItem('session-id', sessionId);
            console.log('[Session] Generated new session id:', sessionId);
          }
        } else {
          throw new Error('Failed to generate session');
        }
      } else {
        console.log('[Session] Found existing session id:', sessionId);
      }

      // Load session data
      if (sessionId) {
        await loadSessionData(sessionId);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to initialize session');
    } finally {
      setIsLoading(false);
    }
  }, [loadSessionData]);

  const saveGithubToken = useCallback(async (token: string) => {
    if (!sessionData?.sessionId) return;
    
    try {
      const response = await fetch(`/api/session/${sessionData.sessionId}/github-token`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ token }),
      });
      
      if (response.ok) {
        setSessionData(prev => prev ? { ...prev, githubToken: token } : null);
        console.log('[Session] Saved GitHub token to backend:', token);
      } else {
        throw new Error('Failed to save GitHub token');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save GitHub token');
    }
  }, [sessionData?.sessionId]);

  const saveGithubUser = useCallback(async (username: string, displayName: string) => {
    if (!sessionData?.sessionId) return;
    
    try {
      const response = await fetch(`/api/session/${sessionData.sessionId}/github-user`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ username, displayName }),
      });
      
      if (response.ok) {
        setSessionData(prev => prev ? { 
          ...prev, 
          githubUsername: username, 
          githubDisplayName: displayName 
        } : null);
        console.log('[Session] Saved GitHub user info to backend:', username, displayName);
      } else {
        throw new Error('Failed to save GitHub user info');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save GitHub user info');
    }
  }, [sessionData?.sessionId]);

  const saveSelectedRepository = useCallback(async (repository: string) => {
    if (!sessionData?.sessionId) return;
    
    try {
      const response = await fetch(`/api/session/${sessionData.sessionId}/repository`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ repository }),
      });
      
      if (response.ok) {
        setSessionData(prev => prev ? { ...prev, selectedRepository: repository } : null);
        console.log('[Session] Saved selected repository to backend:', repository);
      } else {
        throw new Error('Failed to save selected repository');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save selected repository');
    }
  }, [sessionData?.sessionId]);

  const saveRepositories = useCallback(async (repositories: Repository[]) => {
    if (!sessionData?.sessionId) return;
    
    try {
      const response = await fetch(`/api/session/${sessionData.sessionId}/repositories`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ repositories }),
      });
      
      if (response.ok) {
        setSessionData(prev => prev ? { ...prev, repositories } : null);
        console.log('[Session] Saved repositories to backend:', repositories);
      } else {
        throw new Error('Failed to save repositories');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save repositories');
    }
  }, [sessionData?.sessionId]);

  const saveOpenAIConfig = useCallback(async (apiKey: string, chatModel: string, imageModel: string) => {
    if (!sessionData?.sessionId) return;
    
    try {
      const response = await fetch(`/api/session/${sessionData.sessionId}/openai-config`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ apiKey, chatModel, imageModel }),
      });
      
      if (response.ok) {
        setSessionData(prev => prev ? { 
          ...prev, 
          openaiApiKey: apiKey, 
          chatModel, 
          imageModel 
        } : null);
        console.log('[Session] Saved OpenAI config to backend:', apiKey, chatModel, imageModel);
      } else {
        throw new Error('Failed to save OpenAI config');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save OpenAI config');
    }
  }, [sessionData?.sessionId]);

  const clearGithubData = useCallback(async () => {
    if (!sessionData?.sessionId) return;
    
    try {
      const response = await fetch(`/api/session/${sessionData.sessionId}/github`, {
        method: 'DELETE',
      });
      
      if (response.ok) {
        setSessionData(prev => prev ? {
          ...prev,
          githubToken: undefined,
          githubUsername: undefined,
          githubDisplayName: undefined,
          selectedRepository: undefined,
          repositories: undefined,
        } : null);
      } else {
        throw new Error('Failed to clear GitHub data');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to clear GitHub data');
    }
  }, [sessionData?.sessionId]);

  const clearRepositoryData = useCallback(async () => {
    if (!sessionData?.sessionId) return;
    
    try {
      const response = await fetch(`/api/session/${sessionData.sessionId}/repository`, {
        method: 'DELETE',
      });
      
      if (response.ok) {
        setSessionData(prev => prev ? {
          ...prev,
          selectedRepository: undefined,
        } : null);
      } else {
        throw new Error('Failed to clear repository data');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to clear repository data');
    }
  }, [sessionData?.sessionId]);

  // Initialize session on mount
  useEffect(() => {
    initializeSession();
  }, [initializeSession]);

  return {
    sessionData,
    isLoading,
    error,
    saveGithubToken,
    saveGithubUser,
    saveSelectedRepository,
    saveRepositories,
    saveOpenAIConfig,
    clearGithubData,
    clearRepositoryData,
    reloadSession: () => sessionData?.sessionId && loadSessionData(sessionData.sessionId),
  };
}; 