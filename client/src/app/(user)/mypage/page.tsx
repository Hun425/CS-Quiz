"use client";
import React from "react";
import Button from "@/app/_components/Button";
import Image from "next/image";
import DailyQuizHeatmap from "../_components/DailyQizHeatmap";
import Dashboard from "../_components/Dashboard";
import { Edit } from "lucide-react";

const MyPage: React.FC = () => {
  return (
    <div className="max-w-5xl mx-auto p-6 space-y-6">
      {/* 프로필 섹션 */}
      <div className="bg-card p-6 rounded-lg shadow-md flex flex-col sm:flex-row items-center sm:items-start space-y-4 sm:space-y-0 sm:space-x-6">
        <div className="relative w-24 h-24">
          <Image
            src="/profile-placeholder.png"
            alt="Profile Picture"
            className="rounded-full border border-border"
            width={96}
            height={96}
          />
          <button className="absolute bottom-0 right-0 bg-primary text-white p-1 rounded-full">
            <Edit size={16} />
          </button>
        </div>
        <div className="flex-1">
          <h1 className="text-2xl font-bold">사용자 닉네임</h1>
          <p className="text-neutral">가입일: 2024년 3월 1일</p>
          <div className="mt-2 flex space-x-2">
            <Button variant="primary" size="small">
              프로필 수정
            </Button>
            <Button variant="outline" size="small">
              설정
            </Button>
          </div>
        </div>
      </div>

      {/* 대시보드 접근 */}
      <div className="bg-card p-6 rounded-lg shadow-md">
        <DailyQuizHeatmap />
        <Dashboard />
      </div>
    </div>
  );
};

export default MyPage;
