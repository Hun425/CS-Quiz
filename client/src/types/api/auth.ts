// src/types/auth.ts

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  email?: string;
  username?: string;
  tokenType: string;
  expiresIn: number;
}

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  profileImage?: string;
  level: number;
  joinedAt: string;
}

export interface User {
  id: number;
  username: string;
  email: string;
  profileImage?: string;
  level: number;
  role: string;
}

export interface AuthState {
  isAuthenticated: boolean;
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  expiresAt: number | null;

  login: (
    accessToken: string,
    refreshToken: string,
    user: User,
    expiresIn?: number
  ) => void;
  logout: () => void;
  updateUser: (user: Partial<User>) => void;
  updateTokens: (
    accessToken: string,
    refreshToken: string,
    expiresIn?: number
  ) => void;
  isTokenExpired: () => boolean;
}
