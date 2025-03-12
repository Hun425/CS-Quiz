// src/types/auth.ts

export type Provider = "google" | "github" | "kakao";

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  email?: string;
  username?: string;
  tokenType: string;
  expiresIn: number;
}
