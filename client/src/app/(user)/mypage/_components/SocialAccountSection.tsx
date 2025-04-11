"use client";

import GitHubIcon from "@/app/_components/GitHubIcon";
import GoogleIcon from "@/app/_components/GoogleIcon";
import KakaoIcon from "@/app/_components/KakaoIcon";
import Button from "@/app/_components/Button";

const SocialAccountSection = () => {
  return (
    <div className="space-y-4">
      <h2 className="text-xl font-semibold text-primary">소셜 계정 연동</h2>
      <p className="text-sm text-muted-foreground">
        다양한 계정을 연동하여 로그인할 수 있도록 준비 중입니다.
      </p>

      <div className="flex flex-col sm:flex-row gap-4">
        {/* Google */}
        <div className="relative group w-full sm:w-auto">
          <Button
            disabled
            aria-label="Google 계정 연동 (현재 개발 중입니다)"
            className="flex items-center gap-2 w-full sm:w-auto 
                       bg-white text-[#202124] border border-border 
                       cursor-not-allowed select-none opacity-100"
          >
            <GoogleIcon size={16} />
            Google 계정 연동
          </Button>
          <span className="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 px-2 py-1 text-xs text-white bg-black rounded opacity-0 group-hover:opacity-100 transition-opacity z-10 whitespace-nowrap">
            현재 개발 중입니다
          </span>
        </div>

        {/* GitHub */}
        <div className="relative group w-full sm:w-auto">
          <Button
            disabled
            aria-label="GitHub 계정 연동 (현재 개발 중입니다)"
            className="flex items-center gap-2 w-full sm:w-auto 
                       bg-[#24292F] text-white 
                       cursor-not-allowed select-none opacity-100"
          >
            <GitHubIcon size={16} />
            GitHub 계정 연동
          </Button>
          <span className="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 px-2 py-1 text-xs text-white bg-black rounded opacity-0 group-hover:opacity-100 transition-opacity z-10 whitespace-nowrap">
            현재 개발 중입니다
          </span>
        </div>

        {/* Kakao */}
        <div className="relative group w-full sm:w-auto">
          <Button
            disabled
            aria-label="Kakao 계정 연동 (현재 개발 중입니다)"
            className="flex items-center gap-2 w-full sm:w-auto 
                       bg-[#FEE500] text-[#3C1E1E] 
                       cursor-not-allowed select-none opacity-100"
          >
            <KakaoIcon size={16} />
            Kakao 계정 연동
          </Button>
          <span className="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 px-2 py-1 text-xs text-white bg-black rounded opacity-0 group-hover:opacity-100 transition-opacity z-10 whitespace-nowrap">
            현재 개발 중입니다
          </span>
        </div>
      </div>
    </div>
  );
};

export default SocialAccountSection;
