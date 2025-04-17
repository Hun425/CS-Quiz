import Image from "next/image";
import LoginButtonList from "./LoginButtonList";
import { METADATA } from "@/lib/constants/seo";

export const metadata = METADATA.login;

const LoginPage = () => {
  return (
    <div className="min-h-screen flex items-center justify-center px-4 bg-background">
      <div className="bg-card border border-card-border w-full max-w-md rounded-xl shadow-sm p-6 sm:p-8 text-center">
        <div className="flex flex-col items-center justify-center gap-2 mb-4">
          <Image src="/images/logo.png" width={48} height={48} alt="logo" />
          <h1 className="text-2xl sm:text-3xl font-bold text-foreground">
            CRAM
          </h1>
        </div>

        <LoginButtonList />

        <p className="mt-6 text-sm text-muted">
          소셜 계정으로 간편 로그인
          <br className="sm:hidden" />
          (계정이 없으면 자동 가입됩니다)
        </p>
      </div>
    </div>
  );
};

export default LoginPage;
