"use client";

import GitHubIcon from "@/app/_components/GitHubIcon";
import GoogleIcon from "@/app/_components/GoogleIcon";
import KakaoIcon from "@/app/_components/KakaoIcon";
import Button from "@/app/_components/Button";

interface SocialButtonProps {
  name: string;
  icon: React.ReactNode;
  bgClass: string;
  textClass: string;
}

const SocialButton = ({
  name,
  icon,
  bgClass,
  textClass,
}: SocialButtonProps) => (
  <div className="relative group w-full sm:w-auto">
    <Button
      aria-label={`${name} 계정 연동`}
      className={`flex items-center gap-2 w-full sm:w-auto 
                  ${bgClass} ${textClass} 
                  cursor-not-allowed select-none opacity-70`}
    >
      {icon}
      {name} 계정 연동
    </Button>
    <span className="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 px-2 py-1 text-xs text-white bg-black rounded opacity-0 group-hover:opacity-100 transition-opacity z-10 whitespace-nowrap">
      현재 개발 중입니다
    </span>
  </div>
);

const SocialAccountSection = () => {
  return (
    <div className="space-y-4">
      <h2 className="text-xl font-semibold text-primary">소셜 계정 연동</h2>
      <p className="text-sm text-muted-foreground">
        다양한 계정을 연동하여 로그인할 수 있도록 준비 중입니다.
      </p>

      <div className="flex flex-col sm:flex-row gap-4">
        <SocialButton
          name="Google"
          icon={<GoogleIcon size={16} />}
          bgClass="!bg-white border border-gray-300"
          textClass="!text-black"
        />
        <SocialButton
          name="GitHub"
          icon={<GitHubIcon size={16} />}
          bgClass="!bg-[#24292F]"
          textClass="text-white"
        />
        <SocialButton
          name="Kakao"
          icon={<KakaoIcon size={16} />}
          bgClass="!bg-[#FEE500]"
          textClass="text-black"
        />
      </div>
    </div>
  );
};

export default SocialAccountSection;
