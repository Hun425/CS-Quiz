"use client";

import { useState } from "react";
import Link from "next/link";
import { QuizSummaryResponse } from "@/lib/types/quiz";

const PAGE_SIZE = 10;

const QuizList = ({ quizzes }: { quizzes: QuizSummaryResponse[] }) => {
  const [page, setPage] = useState(1);

  const totalPages = Math.ceil(quizzes.length / PAGE_SIZE);
  const startIndex = (page - 1) * PAGE_SIZE;
  const currentPageQuizzes = quizzes.slice(startIndex, startIndex + PAGE_SIZE);

  const handleDelete = (quizId: number) => {
    console.log("삭제 요청:", quizId);
    // TODO: 삭제 API 연결 예정
  };

  if (quizzes.length === 0) {
    return (
      <div className="p-4 text-sm text-muted-foreground border rounded">
        아직 생성된 퀴즈가 없습니다.
      </div>
    );
  }

  return (
    <section className="space-y-4">
      <h2 className="text-lg font-semibold text-primary">내가 생성한 퀴즈</h2>

      <ul className="divide-y border rounded text-sm">
        {currentPageQuizzes.map((quiz) => (
          <li key={quiz.id} className="p-3">
            <div className="flex justify-between items-start gap-4">
              <div className="flex-1">
                <h3 className="font-medium text-foreground">{quiz.title}</h3>
                <p className="text-xs text-muted-foreground line-clamp-1 mt-1">
                  총 {quiz.questionCount}문제 · 평균 점수 {quiz.avgScore}점 ·
                  응시 {quiz.attemptCount}회
                </p>
              </div>

              <div className="flex items-center gap-2">
                <Link
                  href={`/quiz/${quiz.id}/edit`}
                  className="text-xs text-blue-600 hover:underline whitespace-nowrap"
                  aria-label={`${quiz.title} 퀴즈 수정하기`}
                >
                  ✏️ 수정
                </Link>

                <button
                  onClick={() => handleDelete(quiz.id)}
                  className="text-xs text-red-500 hover:underline"
                  aria-label={`${quiz.title} 퀴즈 삭제하기`}
                >
                  🗑 삭제
                </button>
              </div>
            </div>
          </li>
        ))}
      </ul>

      {/* 페이지네이션 */}
      {totalPages > 1 && (
        <div className="flex justify-center items-center gap-4 mt-4 text-sm">
          <button
            onClick={() => setPage((prev) => Math.max(1, prev - 1))}
            disabled={page === 1}
            className="px-3 py-1 border rounded disabled:opacity-30"
          >
            ← 이전
          </button>
          <span className="text-muted-foreground">
            {page} / {totalPages}
          </span>
          <button
            onClick={() => setPage((prev) => Math.min(totalPages, prev + 1))}
            disabled={page === totalPages}
            className="px-3 py-1 border rounded disabled:opacity-30"
          >
            다음 →
          </button>
        </div>
      )}
    </section>
  );
};

export default QuizList;
