"use client";

import { useEffect, useState } from "react";
import QuizSearchHeader from "./_components/QuizSearchHeader";
import QuizList from "./_components/QuizList";
import Sidebar from "./_components/SideBar";
import { QuizDifficultyType, QuizType } from "@/lib/types/quiz";
import { QuizSummaryResponse } from "@/lib/types/quiz";
import { mockQuizzes } from "@/lib/mockQuizzes";

const QuizListPage: React.FC = () => {
  const [searchTitle, setSearchTitle] = useState<string>("");
  const [searchTag, setSearchTag] = useState<string>("");
  const [selectedDifficulty, setSelectedDifficulty] = useState<
    QuizDifficultyType | ""
  >("");
  const [selectedCategory, setSelectedCategory] = useState<QuizType | "">("");
  const [selectedTagIds, setSelectedTagIds] = useState<number[]>([]);

  // ✅ 더미 데이터 및 로딩 상태
  const [quizzes, setQuizzes] = useState<QuizSummaryResponse[] | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<boolean>(false);

  useEffect(() => {
    // 더미 데이터 가져오기 (2초 후 로딩 해제)
    setTimeout(() => {
      try {
        setQuizzes(mockQuizzes);
        setIsLoading(false);
      } catch (err) {
        setError(true);
        setIsLoading(false);
      }
    }, 2000);
  }, []);

  return (
    <div className="bg-sub-background max-w-screen-xl min-h-screen py-8 mx-auto px-4 flex flex-col lg:flex-row gap-6">
      <div className="w-full lg:w-3/4 flex flex-col">
        <QuizSearchHeader
          searchTitle={searchTitle}
          setSearchTitle={setSearchTitle}
          searchTag={searchTag}
          setSearchTag={setSearchTag}
          selectedDifficulty={selectedDifficulty}
          setSelectedDifficulty={setSelectedDifficulty}
          selectedCategory={selectedCategory}
          setSelectedCategory={setSelectedCategory}
          selectedTagIds={selectedTagIds}
          setSelectedTagIds={setSelectedTagIds}
        />

        {/* ✅ 퀴즈 목록 */}
        {isLoading ? (
          <p>로딩 중...</p>
        ) : error ? (
          <p>퀴즈를 불러오는 중 오류 발생</p>
        ) : (
          <QuizList quizzes={quizzes ?? []} />
        )}
      </div>
      <Sidebar />
    </div>
  );
};

export default QuizListPage;
