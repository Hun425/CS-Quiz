import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  serverActions: true,
  reactStrictMode: false,
  env: {},
  images: {
    domains: [
      "lh3.googleusercontent.com",
      "avatars.githubusercontent.com",
      "k.kakaocdn.net",
    ],
  },
};

export default nextConfig;
