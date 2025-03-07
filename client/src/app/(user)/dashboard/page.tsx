"use client";
import React from "react";
import { Calendar, BarChart, Award, BookOpen } from "lucide-react";

const DashboardPage: React.FC = () => {
  return (
    <div className="max-w-5xl mx-auto p-6 space-y-6">
      {/* 학습 진행 상황 */}
      <div className="bg-card p-6 rounded-lg shadow-md">
        <h2 className="text-xl font-bold border-b-2 border-primary pb-2">
          학습 진행 상황
        </h2>
        <div className="mt-4 flex justify-around text-center">
          <div>
            <p className="text-2xl font-bold text-primary">242</p>
            <p className="text-neutral">완료한 문제</p>
          </div>
          <div>
            <p className="text-2xl font-bold text-success">1일 16시간</p>
            <p className="text-neutral">총 학습 시간</p>
          </div>
          <div>
            <p className="text-2xl font-bold text-warning">45%</p>
            <p className="text-neutral">정답률</p>
          </div>
        </div>
      </div>

      {/* 주간 학습 목표 */}
      <div className="bg-card p-6 rounded-lg shadow-md flex items-center gap-4">
        <Calendar size={40} className="text-primary" />
        <div>
          <h3 className="text-lg font-bold">주간 학습 목표</h3>
          <p className="text-neutral">이번 주 목표: 10시간 학습</p>
          <p className="text-success">현재 진행: 4시간 30분</p>
        </div>
      </div>

      {/* 성장 로그 & 스킬 태그 */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="bg-card p-6 rounded-lg shadow-md">
          <h3 className="text-lg font-bold">성장 로그</h3>
          <ul className="mt-4 space-y-2 text-neutral">
            <li>✅ 클린 코드 강의 수강 완료</li>
            <li>✅ React 상태 관리 정리 완료</li>
            <li>📌 데이터베이스 최적화 공부 중</li>
          </ul>
        </div>
        <div className="bg-card p-6 rounded-lg shadow-md">
          <h3 className="text-lg font-bold">스킬 태그</h3>
          <div className="mt-4 flex gap-2 flex-wrap">
            <span className="bg-primary text-white px-3 py-1 rounded-full text-sm">
              React
            </span>
            <span className="bg-success text-white px-3 py-1 rounded-full text-sm">
              JavaScript
            </span>
            <span className="bg-warning text-white px-3 py-1 rounded-full text-sm">
              CSS
            </span>
          </div>
        </div>
      </div>

      {/* 로드맵 & 추천 채용 */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="bg-card p-6 rounded-lg shadow-md flex items-center gap-4">
          <BookOpen size={40} className="text-primary" />
          <div>
            <h3 className="text-lg font-bold">로드맵</h3>
            <p className="text-neutral">Java 개발자를 위한 실전 코스 진행 중</p>
          </div>
        </div>
        <div className="bg-card p-6 rounded-lg shadow-md flex items-center gap-4">
          <Award size={40} className="text-warning" />
          <div>
            <h3 className="text-lg font-bold">추천 채용</h3>
            <p className="text-neutral">데이터 엔지니어 (Python, Node.js)</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
