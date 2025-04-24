"use client";

import { useOAuthLogin } from "@/lib/hooks/useOAuthLogin";
import { Provider } from "@/lib/types/auth";

const providerStyles = {
  google: { bg: "#ffffff", color: "#000000", text: "Google 로그인" },
  github: { bg: "#333333", color: "#ffffff", text: "GitHub 로그인" },
  kakao: { bg: "#FEE500", color: "#000000", text: "Kakao 로그인" },
};

const LoginButtonList = () => {
  const { loginWithProvider } = useOAuthLogin();

  return (
    <div className="space-y-3 mt-6">
      {(Object.keys(providerStyles) as Provider[]).map((provider) => {
        const isAvailable = provider === "google";
        return (
          <button
            key={provider}
            onClick={() => isAvailable && loginWithProvider(provider)}
            disabled={!isAvailable}
            className={`w-full flex items-center justify-center gap-3 py-3 px-5 rounded-lg text-base font-semibold transition-all duration-200
              ${
                isAvailable
                  ? "hover:shadow-sm"
                  : "opacity-50 cursor-not-allowed"
              }
            `}
            style={{
              backgroundColor: providerStyles[provider].bg,
              color: providerStyles[provider].color,
              border: provider === "google" ? "1px solid #ccc" : "none",
            }}
          >
            {providerStyles[provider].text}
            {!isAvailable && <span className="text-xs">(준비 중)</span>}
          </button>
        );
      })}
    </div>
  );
};

export default LoginButtonList;
