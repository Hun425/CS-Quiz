"use client";
import { useOAuthLogin } from "@/lib/hooks/useOAuthLogin";
import { Provider } from "@/lib/types/auth";
import { useSearchParams } from "next/navigation";

const providerStyles: Record<
  Provider,
  { bg: string; color: string; text: string }
> = {
  google: { bg: "#ffffff", color: "#757575", text: "Google 로그인" },
  github: { bg: "#333333", color: "#ffffff", text: "GitHub 로그인" },
  kakao: { bg: "#FEE500", color: "#000000", text: "Kakao 로그인" },
};

const LoginPage: React.FC = () => {
  const { loginWithProvider } = useOAuthLogin();
  const searchParams = useSearchParams();
  const error = searchParams.get("error");

  return (
    <div className="min-h-screen flex items-center justify-center bg-background">
      <div className="bg-card border border-card-border w-full max-w-xl rounded-2xl shadow-xl p-8 text-center">
        <h1 className="text-3xl font-bold text-foreground mb-3">CRAM</h1>
        {error && (
          <p className="text-red-500 ">
            {error === "invalid_token"
              ? "⛔ 로그인 인증에 실패했습니다. 다시 시도해주세요."
              : "알 수 없는 오류가 발생했습니다."}
          </p>
        )}

        {/* ✅ 로그인 버튼 리스트 */}
        <div className="space-y-4 mt-6">
          {(Object.keys(providerStyles) as Provider[]).map((provider) => (
            <button
              key={provider}
              onClick={() => loginWithProvider(provider)}
              className="w-full flex items-center justify-center gap-3 py-4 px-6 rounded-lg text-base font-semibold transition-all duration-200 cursor-pointer hover:shadow-md"
              style={{
                backgroundColor: providerStyles[provider]?.bg,
                color: providerStyles[provider]?.color,
                border: provider === "google" ? "1px solid #757575" : "none",
              }}
            >
              {providerStyles[provider]?.text}
            </button>
          ))}
        </div>

        <p className="mt-6 text-sm text-muted">
          소셜 계정으로 간편 로그인 (계정이 없으면 자동 가입)
        </p>
      </div>
    </div>
  );
};

export default LoginPage;
