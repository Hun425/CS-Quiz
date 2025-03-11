"use client";

import { useState } from "react";
import { QuizSummaryResponse } from "@/types/api";
import {
  Search,
  ChevronDown,
  BookOpenCheck,
  Briefcase,
  ListOrdered,
} from "lucide-react";

const QuizListPage: React.FC = () => {
  // 상태 관리
  const [searchTitle, setSearchTitle] = useState<string>("");
  const [selectedDifficulty, setSelectedDifficulty] = useState<string>("");
  const [selectedLanguage, setSelectedLanguage] = useState<string>("");
  const [selectedCategory, setSelectedCategory] = useState<string>("");

  // ✅ 더미 퀴즈 데이터
  const dummyQuizzes: QuizSummaryResponse[] = [
    {
      id: 1,
      title: "문자열과 알파벳과 쿼리",
      difficultyLevel: "ADVANCED",
      quizType: "TOPIC_BASED",
      questionCount: 10,
      attemptCount: 5,
      avgScore: 80,
      tags: [],
      createdAt: new Date().toISOString(),
    },
    {
      id: 2,
      title: "눈사람 만들기",
      difficultyLevel: "ADVANCED",
      quizType: "TAG_BASED",
      questionCount: 12,
      attemptCount: 3,
      avgScore: 75,
      tags: [],
      createdAt: new Date().toISOString(),
    },
    {
      id: 3,
      title: "격자 뒤집기 미로",
      difficultyLevel: "INTERMEDIATE",
      quizType: "DAILY",
      questionCount: 15,
      attemptCount: 8,
      avgScore: 90,
      tags: [],
      createdAt: new Date().toISOString(),
    },
  ];

  return (
    <div className="bg-sub-background min-h-100vh">
      <div className=" max-w-screen-xl mx-auto py-6 px-4 grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* ✅ 왼쪽 필터 섹션 */}
        <div className="bg-card border border-border p-6 rounded-lg shadow-md">
          <h2 className="text-lg font-semibold mb-4">🔎 문제 검색</h2>
          {/* 🔍 검색 입력 */}
          <div className="relative mb-4">
            <input
              type="text"
              value={searchTitle}
              onChange={(e) => setSearchTitle(e.target.value)}
              placeholder="풀고 싶은 문제 제목, 기출문제 검색"
              className="w-full p-2 border border-border rounded-md pl-10 bg-background text-foreground"
            />
            <Search className="absolute left-3 top-3 w-4 h-4 text-neutral" />
          </div>
          {/* 🔽 필터 드롭다운 */}
          <div className="grid grid-cols-3 gap-2 mb-4">
            <select
              value={selectedDifficulty}
              onChange={(e) => setSelectedDifficulty(e.target.value)}
              className={`w-full p-2 border border-border rounded-md bg-background text-foreground ${
                selectedDifficulty ? "ring-2 ring-primary" : ""
              }`}
            >
              <option value="">난이도 선택</option>
              <option value="BEGINNER">Lv. 1</option>
              <option value="INTERMEDIATE">Lv. 2</option>
              <option value="ADVANCED">Lv. 3+</option>
            </select>

            <select
              value={selectedLanguage}
              onChange={(e) => setSelectedLanguage(e.target.value)}
              className="w-full p-2 border border-border rounded-md bg-background text-foreground"
            >
              <option value="">언어</option>
              <option value="JS">JavaScript</option>
              <option value="TS">TypeScript</option>
              <option value="Python">Python</option>
            </select>

            <select
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
              className="w-full p-2 border border-border rounded-md bg-background text-foreground"
            >
              <option value="">기출문제 모음</option>
              <option value="DAILY">데일리</option>
              <option value="TOPIC">주제 기반</option>
            </select>
          </div>
          {/* ✅ 문제 개수 표시 */}
          <ListOrdered className="w-4 h-4 text-muted mr-1" />{" "}
          {dummyQuizzes.length} 문제
        </div>

        {/* ✅ 중앙 문제 리스트 */}
        <div className="col-span-2">
          <table className="w-full border-collapse border border-border bg-card rounded-lg overflow-hidden">
            <thead>
              <tr className="bg-gray-100 text-sm">
                <th className="border border-border p-2 text-left w-1/2">
                  제목
                </th>
                <th className="border border-border p-2">난이도</th>
                <th className="border border-border p-2">완료한 사람</th>
                <th className="border border-border p-2">정답률</th>
              </tr>
            </thead>
            <tbody>
              {dummyQuizzes.map((quiz) => (
                <tr key={quiz.id} className="hover:bg-gray-50">
                  <td className="border border-border p-2">{quiz.title}</td>
                  <td className="border border-border p-2 text-primary">
                    Lv.{" "}
                    {quiz.difficultyLevel === "BEGINNER"
                      ? "1"
                      : quiz.difficultyLevel === "INTERMEDIATE"
                      ? "2"
                      : "3+"}
                  </td>
                  <td className="border border-border p-2">
                    {quiz.attemptCount}명
                  </td>
                  <td className="border border-border p-2">{quiz.avgScore}%</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* ✅ 오른쪽 추천 영역 */}
        <div className="hidden lg:block bg-card border border-border p-6 rounded-lg shadow-md">
          <h2 className="text-lg font-semibold mb-4">
            📢 로그인하고 연습을 시작하세요!
          </h2>
          <button className="w-full py-2 bg-primary text-white rounded-md">
            로그인
          </button>

          <h3 className="text-md font-semibold mt-6 mb-3">
            <BookOpenCheck className="w-5 h-5 mr-2" /> 내 실력 향상을 위한 추천
            코스
          </h3>
          <div className="bg-gray-100 p-3 rounded-md text-sm mb-2">
            🔹 AI 백엔드 개발
          </div>
          <div className="bg-gray-100 p-3 rounded-md text-sm mb-2">
            🔹 자바 중급
          </div>
          <div className="bg-gray-100 p-3 rounded-md text-sm">
            🔹 데이터 엔지니어링
          </div>

          <h3 className="text-md font-semibold mt-6 mb-3">
            {" "}
            <Briefcase className="w-5 h-5 mr-2" /> 추천 포지션
          </h3>
          <div className="bg-gray-100 p-3 rounded-md text-sm mb-2">
            💼 미들급 백엔드 개발자
          </div>
          <div className="bg-gray-100 p-3 rounded-md text-sm">
            💼 웹 프론트엔드/백엔드 개발자
          </div>
        </div>
      </div>
    </div>
  );
};

export default QuizListPage;
