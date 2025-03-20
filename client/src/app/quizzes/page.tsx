"use client";

import { useState } from "react";
import QuizSearchHeader from "./_components/QuizSearchHeader";
import QuizSearchList from "./_components/QuizSearchList";
import Sidebar from "./_components/SideBar";
import { useSearchQuizzes } from "@/lib/api/quiz/useSearchQuizzes";
import { QuizDifficultyType, QuizType } from "@/lib/types/quiz";

const QuizListPage: React.FC = () => {
  // 🔹 검색 필터 상태
  const [searchParams, setSearchParams] = useState({
    title: "",
    difficultyLevel: "",
    quizType: "",
    tagIds: [] as number[],
  });

  // 🔹 페이지네이션 상태
  const [currentPage, setCurrentPage] = useState(0);

  // ✅ 검색 API 호출
  const { data, isLoading, error } = useSearchQuizzes(
    searchParams,
    currentPage,
    10
  );

  // 🔹 검색 실행 함수 (검색 시 페이지를 0으로 리셋)
  const handleSearch = (newParams: {
    title?: string;
    difficultyLevel?: QuizDifficultyType | "";
    quizType?: QuizType | "";
    tagIds?: number[];
  }) => {
    console.log("검색 실행 함수", newParams);
    setSearchParams((prev) => ({
      ...prev,
      title: newParams.title ?? "",
      difficultyLevel: newParams.difficultyLevel ?? "",
      quizType: newParams.quizType ?? "",
      tagIds: newParams.tagIds ?? [],
    }));
    setCurrentPage(0);
  };

  return (
    <div className="bg-sub-background max-w-screen-xl min-h-screen py-8 mx-auto px-4 flex flex-col lg:flex-row gap-6">
      <div className="w-full lg:w-3/4 flex flex-col">
        {/* 🔹 검색 UI */}
        <QuizSearchHeader onSearch={handleSearch} />

        {/* 🔹 검색 결과 */}
        <QuizSearchList
          data={data}
          isLoading={isLoading}
          error={error?.message}
          currentPage={currentPage}
          setCurrentPage={setCurrentPage}
        />
      </div>
      <Sidebar />
    </div>
  );
};

export default QuizListPage;
