"use client";
import { useOAuthLogin } from "@/lib/hooks/useOAuthLogin";
import { Provider } from "@/lib/types/auth";
import { useSearchParams } from "next/navigation";
import Image from "next/image";

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
        {/* ✅ 로고와 타이틀을 수직 정렬 */}
        <div className="flex flex-col items-center justify-center gap-2 mb-4">
          <Image src={"/images/logo.png"} width={50} height={50} alt="logo" />
          <h1 className="text-3xl font-bold text-foreground">CRAM</h1>
        </div>

        {/* ✅ 오류 메시지 */}
        {error && (
          <p className="text-red-500 mb-4">
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
