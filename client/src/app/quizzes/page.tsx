"use client";

import { useState } from "react";
import dynamic from "next/dynamic";
import QuizSearchHeader from "./_components/QuizSearchHeader";
import Sidebar from "./_components/SideBar";
import { useSearchQuizzes } from "@/lib/api/quiz/useSearchQuizzes";
import { QuizDifficultyType, QuizType } from "@/lib/types/quiz";
import QuizSearchListSkeleton from "./_components/QuizSearchListSkeleton";

const QuizSearchList = dynamic(() => import("./_components/QuizSearchList"), {
  ssr: false,
  loading: () => <QuizSearchListSkeleton />,
});

const QuizListPage: React.FC = () => {
  const [searchParams, setSearchParams] = useState({
    title: "",
    difficultyLevel: "",
    quizType: "",
    tagIds: [] as number[],
  });

  const [currentPage, setCurrentPage] = useState(0);

  const { data, isLoading, error } = useSearchQuizzes(
    searchParams,
    currentPage,
    12
  );

  const handleSearch = (newParams: {
    title?: string;
    difficultyLevel?: QuizDifficultyType | "";
    quizType?: QuizType | "";
    tagIds?: number[];
  }) => {
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
        {/* 🔍 검색 필터 */}
        <QuizSearchHeader onSearch={handleSearch} />

        {/* 📄 지연 로딩 */}
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
