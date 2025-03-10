"use client";
import { useState } from "react";
import Image from "next/image";

type Provider = "google" | "github" | "kakao";

const providerStyles: Record<
  Provider,
  { bg: string; color: string; text: string }
> = {
  google: {
    bg: "#ffffff",
    color: "#757575",
    text: "Google로 로그인",
  },
  github: {
    bg: "#333333",
    color: "#ffffff",
    text: "GitHub로 로그인",
  },
  kakao: {
    bg: "#FEE500",
    color: "#000000",
    text: "Kakao로 로그인",
  },
};

const LoginPage: React.FC = () => {
  const [loggingIn, setLoggingIn] = useState<Provider | null>(null);

  const handleOAuth2Login = (provider: Provider) => {
    setLoggingIn(provider);
    const redirectUrl = `http://localhost:8080/api/oauth2/authorize/${provider.toLowerCase()}`;
    window.location.href = redirectUrl;
  };

  return (
    <div className="min-h-screen bg-[var(--background)] flex items-center justify-center p-6">
      <div className="bg-card border border-card-border w-full max-w-xl rounded-2xl shadow-xl p-8 text-center">
        {/* 로고 & 서비스명 */}
        <div className="mb-8 flex flex-col items-center ">
          <Image
            src="/icons/logo.svg"
            alt="Cram Logo"
            width={60}
            height={60}
            className="mb-3"
          />
          <h1 className="text-3xl font-bold text-[var(--foreground)]">CRAM</h1>
        </div>

        {/* 로그인 버튼 리스트 */}
        <div className="space-y-4">
          {(Object.keys(providerStyles) as Provider[]).map((provider) => (
            <button
              key={provider}
              onClick={() => handleOAuth2Login(provider)}
              disabled={loggingIn !== null}
              className={`w-full flex items-center justify-center gap-3
                          py-4 px-6 rounded-lg text-base font-semibold
                          transition-all duration-200
                          disabled:opacity-50 disabled:cursor-not-allowed
                          cursor-pointer
                          ${
                            loggingIn && loggingIn !== provider
                              ? "opacity-50"
                              : "hover:shadow-md"
                          }`}
              style={{
                backgroundColor: providerStyles[provider].bg,
                color: providerStyles[provider].color,
                border: provider === "google" ? "1px solid #757575" : "none",
              }}
            >
              {loggingIn === provider && (
                <span
                  className="inline-block w-5 h-5 border-2 border-[rgba(0,0,0,0.1)] 
                              border-t-[var(--primary)] rounded-full animate-spin"
                />
              )}
              {providerStyles[provider].text}
            </button>
          ))}
        </div>

        {/* 추가 설명 */}
        <div className="mt-6 text-center text-sm text-[var(--neutral)]">
          <p>소셜 계정으로 간편 로그인</p>
          <p>계정이 없으면 자동으로 가입됩니다.</p>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
