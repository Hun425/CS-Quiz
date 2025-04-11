"use client";

import Link from "next/link";
import { QuizSummaryResponse } from "@/lib/types/quiz";

const QuizList = ({ quizzes }: { quizzes: QuizSummaryResponse[] }) => {
  if (quizzes.length === 0) {
    return (
      <div className="p-4 text-muted-foreground border rounded">
        아직 생성된 퀴즈가 없습니다.
      </div>
    );
  }

  return (
    <section className="space-y-4">
      <h2 className="text-xl font-semibold text-primary">내가 생성한 퀴즈</h2>
      <ul className="divide-y border rounded">
        {quizzes.map((quiz) => (
          <li key={quiz.id} className="p-4">
            <div className="flex justify-between items-center">
              <div>
                <h3 className="text-lg font-medium text-foreground">
                  {quiz.title}
                </h3>
                <p className="text-sm text-muted-foreground line-clamp-2"></p>
              </div>
              <Link
                href={`/quiz/${quiz.id}/edit`}
                className="text-sm text-blue-600 hover:underline"
                aria-label={`${quiz.title} 퀴즈 수정하기`}
              >
                수정
              </Link>
            </div>
          </li>
        ))}
      </ul>
    </section>
  );
};

export default QuizList;
