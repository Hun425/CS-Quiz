"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import TagSelector from "./_components/TagSelector";
import QuizCard from "./_components/QuizCard";

const QuizListPage: React.FC = () => {
  // 상태 관리
  const [searchTitle, setSearchTitle] = useState<string>("");
  const [selectedDifficulty, setSelectedDifficulty] = useState<string>("");
  const [selectedQuizType, setSelectedQuizType] = useState<string>("");
  const [selectedTags, setSelectedTags] = useState<number[]>([]);
  const [page, setPage] = useState<number>(0);
  const [size, _setSize] = useState<number>(9);

  // 더미 태그 데이터
  const dummyTags = [
    { id: 1, name: "JavaScript", quizCount: 12 },
    { id: 2, name: "React", quizCount: 8 },
    { id: 3, name: "TypeScript", quizCount: 5 },
  ];

  // 더미 퀴즈 데이터
  const dummyQuizzes = {
    content: [
      { id: 1, title: "React 기본 개념", difficulty: "BEGINNER" },
      { id: 2, title: "TypeScript 기초", difficulty: "INTERMEDIATE" },
      { id: 3, title: "JavaScript 심화", difficulty: "ADVANCED" },
    ],
    totalPages: 1,
  };

  // 태그 데이터 가져오기 (API 연결 X → 더미 데이터 사용)
  const { data: tags = dummyTags } = useQuery({
    queryKey: ["tags"],
    queryFn: async () => {
      throw new Error("API 연결 실패");
    },
    enabled: false, // API 요청을 막음
  });

  // 인기 태그 가져오기 (API 연결 X → 더미 데이터 사용)
  const { data: popularTags = dummyTags } = useQuery({
    queryKey: ["popularTags"],
    queryFn: async () => {
      throw new Error("API 연결 실패");
    },
    enabled: false,
  });

  // 퀴즈 목록 가져오기 (API 연결 X → 더미 데이터 사용)
  const {
    data: quizData = dummyQuizzes,
    isLoading,
    isError,
    refetch,
  } = useQuery({
    queryKey: [
      "quizzes",
      page,
      selectedDifficulty,
      selectedQuizType,
      selectedTags,
    ],
    queryFn: async () => {
      throw new Error("API 연결 실패");
    },
    enabled: false,
  });

  return (
    <div className="max-w-screen-xl mx-auto py-8 px-4">
      <h1 className="text-3xl font-bold text-center mb-6 text-[var(--foreground)]">
        퀴즈 목록
      </h1>

      {/* 필터 섹션 */}
      <div className="bg-card border border-card-border p-6 rounded-lg shadow-md mb-6">
        <h2 className="text-lg font-semibold mb-4">필터 및 검색</h2>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
          {/* 제목 검색 */}
          <input
            type="text"
            value={searchTitle}
            onChange={(e) => setSearchTitle(e.target.value)}
            placeholder="퀴즈 제목 검색..."
            className="w-full p-2 border border-border rounded-md"
          />

          {/* 난이도 필터 */}
          <select
            value={selectedDifficulty}
            onChange={(e) => setSelectedDifficulty(e.target.value)}
            className="w-full p-2 border border-border rounded-md"
          >
            <option value="">모든 난이도</option>
            <option value="BEGINNER">입문</option>
            <option value="INTERMEDIATE">중급</option>
            <option value="ADVANCED">고급</option>
          </select>

          {/* 퀴즈 유형 필터 */}
          <select
            value={selectedQuizType}
            onChange={(e) => setSelectedQuizType(e.target.value)}
            className="w-full p-2 border border-border rounded-md"
          >
            <option value="">모든 유형</option>
            <option value="DAILY">데일리 퀴즈</option>
            <option value="TAG_BASED">태그 기반</option>
            <option value="TOPIC_BASED">주제 기반</option>
            <option value="CUSTOM">커스텀</option>
          </select>
        </div>

        {/* 태그 필터 */}
        <TagSelector selectedTagIds={selectedTags} onChange={setSelectedTags} />

        {/* 인기 태그 */}
        <div className="flex flex-wrap gap-2 mt-4">
          {popularTags?.map((tag) => (
            <button
              key={tag.id}
              onClick={() =>
                setSelectedTags((prev) =>
                  prev.includes(tag.id)
                    ? prev.filter((id) => id !== tag.id)
                    : [...prev, tag.id]
                )
              }
              className={`px-3 py-1 rounded-md border text-sm ${
                selectedTags.includes(tag.id)
                  ? "bg-primary text-white"
                  : "border-border text-neutral"
              }`}
            >
              {tag.name} ({tag.quizCount})
            </button>
          ))}
        </div>

        {/* 검색 버튼 */}
        <button
          onClick={() => refetch()}
          className="mt-4 w-full py-2 bg-primary text-white rounded-md"
        >
          검색
        </button>
      </div>

      {/* 로딩 및 에러 처리 */}
      {isLoading ? (
        <div className="text-center py-8">퀴즈를 불러오는 중...</div>
      ) : isError ? (
        <div className="text-center py-8 text-danger">
          ⚠️ 퀴즈 데이터를 불러올 수 없습니다.
          <button
            onClick={() => refetch()}
            className="block mx-auto mt-4 bg-warning text-white px-4 py-2 rounded-md"
          >
            다시 시도
          </button>
        </div>
      ) : (
        <>
          {/* 퀴즈 목록 */}
          {quizData?.content.length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              {quizData.content.map((quiz) => (
                <QuizCard key={quiz.id} quiz={quiz} />
              ))}
            </div>
          ) : (
            <div className="text-center py-8 bg-background/10 p-6 rounded-md">
              검색 조건에 맞는 퀴즈가 없습니다.
            </div>
          )}

          {/* 페이지네이션 */}
          {quizData?.totalPages > 1 && (
            <div className="flex justify-center mt-6">
              <button
                onClick={() => setPage(page - 1)}
                disabled={page === 0}
                className="px-4 py-2 border border-border rounded-md mr-2 disabled:opacity-50"
              >
                이전
              </button>
              {[...Array(quizData.totalPages)].map((_, index) => (
                <button
                  key={index}
                  onClick={() => setPage(index)}
                  className={`px-4 py-2 border border-border rounded-md mx-1 ${
                    page === index ? "bg-primary text-white" : "text-neutral"
                  }`}
                >
                  {index + 1}
                </button>
              ))}
              <button
                onClick={() => setPage(page + 1)}
                disabled={page === quizData.totalPages - 1}
                className="px-4 py-2 border border-border rounded-md ml-2 disabled:opacity-50"
              >
                다음
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default QuizListPage;
