"use client";

import React from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import Image from "next/image";
import ThemeToggle from "./ThemeToggle";
import SearchBar from "./SearchBar"; // ✅ 새로 만든 컴포넌트 import
import Button from "./Button";

const Header = () => {
  const router = useRouter();

  return (
    <header className="w-full h-[64px] fixed top-0 left-0 right-0 z-50 bg-background  border-b border-border">
      <div className="max-w-screen-2xl mx-auto flex items-center justify-between h-full px-4 md:px-6 lg:px-8">
        {/* 로고 */}
        <Link href={"/"} className="flex items-center space-x-2 mr-4">
          <Image
            src="/images/logo.png"
            width={35}
            height={35}
            alt="CRAM Logo"
            className="rounded-full"
          />
          <h1 className="font-primary text-2xl font-bold tracking-tight">
            CRAM
          </h1>
        </Link>
        <ThemeToggle />

        {/* 메뉴 & 검색 */}
        <div className="hidden md:flex items-center space-x-4 flex-1 justify-center">
          <SearchBar />
          {/* 메뉴 */}
          <nav className="space-x-4">
            <Link
              href="/quizzes"
              className="text-foreground font-semibold hover:scale-105"
            >
              퀴즈
            </Link>
            <Link
              href="/battles"
              className="text-primary font-semibold hover:scale-105"
            >
              실시간 퀴즈 대결
            </Link>
          </nav>
        </div>

        {/* 다크 모드 버튼 & 로그인 */}
        <div className="flex items-center space-x-2">
          <Button onClick={() => router.push("/login")} variant="primary">
            로그인
          </Button>
        </div>
      </div>
    </header>
  );
};

export default Header;
