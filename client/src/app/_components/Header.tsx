"use client";

import React, { useState } from "react";
import Link from "next/link";
import Image from "next/image";
import { Search } from "lucide-react";
import ThemeToggle from "./ThemeToggle";

const Header = () => {
  const [searchTerm, setSearchTerm] = useState("");

  return (
    <header
      style={{
        backgroundColor: "var(--background)",
        boxShadow: "0 4px 6px rgba(0,0,0,0.1)",
      }}
      className="w-full h-[60px] fixed top-0 left-0 right-0 z-50"
    >
      <div className="max-w-screen-lg mx-auto flex items-center justify-between h-full px-4 md:px-6 lg:px-8">
        {/* 로고 */}
        <div className="flex items-center space-x-2">
          <Image
            src="/images/logo.png"
            width={40}
            height={40}
            alt="CRAM Logo"
            className="rounded-full"
          />
          <h1
            style={{ color: "var(--primary)" }}
            className="text-2xl font-bold"
          >
            CRAM
          </h1>
        </div>

        {/* 메뉴 & 검색 */}
        <div className="hidden md:flex items-center space-x-4 flex-1 justify-center">
          <div className="relative w-full max-w-md">
            <input
              type="text"
              placeholder="어떤 CS 주제를 벼락치기 할까요?"
              style={{ backgroundColor: "white", color: "var(--foreground)" }}
              className="w-full p-2 pl-8 rounded-full shadow-md focus:outline-none focus:ring-2"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
            <Search
              className="absolute left-2 top-1/2 transform -translate-y-1/2"
              size={16}
              style={{ color: "var(--neutral)" }}
            />
          </div>

          {/* 메뉴 */}
          <nav className="space-x-4">
            <Link
              href="/quiz"
              className="transition-colors"
              style={{ color: "var(--foreground)" }}
            >
              퀴즈
            </Link>
            <Link
              href="/live-battle"
              className="font-semibold transition-colors"
              style={{ color: "var(--danger)" }}
            >
              실시간 퀴즈 대결
            </Link>
          </nav>
        </div>

        {/* 다크 모드 버튼 & 로그인 */}
        <div className="flex items-center space-x-2">
          <ThemeToggle />
          <Link
            href="/login"
            className="px-3 py-1 rounded-lg shadow-md hover:scale-105 transition-all"
            style={{ backgroundColor: "var(--primary)", color: "white" }}
          >
            로그인
          </Link>
          <Link
            href="/signup"
            className="px-3 py-1 rounded-lg shadow-md hover:scale-105 transition-all"
            style={{ backgroundColor: "var(--primary)", color: "white" }}
          >
            회원가입
          </Link>
        </div>
      </div>
    </header>
  );
};

export default Header;
