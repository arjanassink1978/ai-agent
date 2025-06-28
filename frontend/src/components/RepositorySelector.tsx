'use client';

import { Repository } from '../types/repository';

interface RepositorySelectorProps {
  repositories: Repository[];
  selectedRepository: Repository | null;
  onRepositoryChange: (repository: Repository | null) => void;
  disabled?: boolean;
  placeholder?: string;
}

export default function RepositorySelector({
  repositories,
  selectedRepository,
  onRepositoryChange,
  disabled = false,
  placeholder = "Select a repository..."
}: RepositorySelectorProps) {
  // Helper function to get the full name (handle both field names)
  const getFullName = (repo: Repository) => repo.full_name || repo.fullName || repo.name;
  
  return (
    <div className="flex items-center gap-2">
      <span className="text-sm font-medium text-gray-700">Repository:</span>
      <select
        value={selectedRepository ? getFullName(selectedRepository) : ''}
        onChange={(e) => {
          const selected = repositories.find(repo => getFullName(repo) === e.target.value);
          onRepositoryChange(selected || null);
        }}
        disabled={disabled}
        className="px-3 py-1 border border-gray-300 rounded-md focus:outline-none focus:border-blue-500 bg-white text-gray-900 min-w-[300px]"
      >
        <option value="">{placeholder}</option>
        {repositories.map((repo, index) => (
          <option 
            key={repo.id ? `repo-${repo.id}` : `repo-${getFullName(repo)}-${index}`} 
            value={getFullName(repo)}
          >
            {getFullName(repo)} {repo.private ? '(Private)' : '(Public)'} - {repo.language || 'Unknown'}
          </option>
        ))}
      </select>
    </div>
  );
} 