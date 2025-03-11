import Link from "next/link";
import { QuizSummaryResponse } from "@/lib/types/api";
import classNames from "classnames";

interface QuizCardProps {
  quiz: QuizSummaryResponse;
}

const QuizCard: React.FC<QuizCardProps> = ({ quiz }) => {
  // 난이도별 Tailwind 배경색 설정
  const difficultyClasses: Record<
    QuizSummaryResponse["difficultyLevel"],
    string
  > = {
    BEGINNER: "bg-success text-white",
    INTERMEDIATE: "bg-warning text-white",
    ADVANCED: "bg-danger text-white",
  };

  // 난이도 한글 라벨
  const difficultyLabels: Record<
    QuizSummaryResponse["difficultyLevel"],
    string
  > = {
    BEGINNER: "입문",
    INTERMEDIATE: "중급",
    ADVANCED: "고급",
  };

  // 퀴즈 유형 한글 라벨
  const quizTypeLabels: Record<QuizSummaryResponse["quizType"], string> = {
    DAILY: "데일리 퀴즈",
    TAG_BASED: "태그 기반",
    TOPIC_BASED: "주제 기반",
    CUSTOM: "커스텀",
  };

  return (
    <div className="border border-border rounded-lg p-6 bg-card-background shadow-md transition-transform duration-200 hover:scale-105">
      <Link
        href={`/quizzes/${quiz.id}`}
        className="flex flex-col h-full text-foreground no-underline"
      >
        {/* 퀴즈 제목 */}
        <h3 className="text-lg font-bold text-primary mb-2">{quiz.title}</h3>

        {/* 퀴즈 메타정보 */}
        <div className="flex flex-wrap gap-2 mb-4">
          {/* 난이도 */}
          <span
            className={classNames(
              "px-2 py-1 rounded text-sm",
              difficultyClasses[quiz.difficultyLevel]
            )}
          >
            {difficultyLabels[quiz.difficultyLevel]}
          </span>

          {/* 퀴즈 유형 */}
          <span className="bg-muted text-foreground px-2 py-1 rounded text-sm">
            {quizTypeLabels[quiz.quizType]}
          </span>

          {/* 문제 개수 */}
          <span className="bg-muted text-foreground px-2 py-1 rounded text-sm">
            {quiz.questionCount}문제
          </span>
        </div>

        {/* 시도 횟수 & 평균 점수 */}
        <div className="text-sm text-muted">
          <span>
            🔥 {quiz.attemptCount}회 도전 | 📊 평균 점수:{" "}
            {quiz.avgScore.toFixed(1)}
          </span>
        </div>
      </Link>
    </div>
  );
};

export default QuizCard;
