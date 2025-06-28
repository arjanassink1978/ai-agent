export interface Repository {
  id?: number;
  name: string;
  full_name: string;
  fullName?: string;
  description: string;
  private: boolean;
  html_url?: string;
  url?: string;
  clone_url: string;
  language: string;
  updated_at: string;
} 