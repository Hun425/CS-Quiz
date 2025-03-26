"use client";

import Link from "next/link";
import Image from "next/image";
import { useProfileStore } from "@/store/profileStore";
import { usePathname } from "next/navigation";
import { useAuthStore } from "@/store/authStore";
import Button from "./Button";
import TokenTimer from "./TokenTimer";

const LoginButton = () => {
  const pathname = usePathname();
  const { isAuthenticated, logout } = useAuthStore();
  const { userProfile } = useProfileStore();

  // ✅ 로그인 페이지에서는 버튼을 숨김
  if (pathname === "/login") return null;

  return isAuthenticated ? (
    <div className="flex items-center">
      <TokenTimer />
      <Link href="/mypage" className="mx-3">
        <Image
          src={userProfile?.profileImage || "/images/default-avatar.png"}
          width={32}
          height={32}
          alt="Profile"
          className="rounded-full"
        />
      </Link>

      <Button onClick={logout} variant="outline">
        로그아웃
      </Button>
    </div>
  ) : (
    <Link href="/login">
      <Button variant="primary" className="text-white">
        로그인
      </Button>
    </Link>
  );
};

export default LoginButton;
