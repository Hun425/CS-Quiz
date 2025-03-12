"use client";

import Link from "next/link";
import Image from "next/image";
import { useProfileStore } from "@/store/profileStore";
import { useAuthStore } from "@/store/authStore";
import Button from "./Button";

const LoginButton = () => {
  const { isAuthenticated, logout } = useAuthStore();
  const { userProfile } = useProfileStore();

  return isAuthenticated ? (
    <div className="flex items-center">
      <Link href="/mypage" className="mr-3">
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
      <Button variant="primary">로그인</Button>
    </Link>
  );
};

export default LoginButton;
