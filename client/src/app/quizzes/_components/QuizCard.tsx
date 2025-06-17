import Link from "next/link";
import Tag from "@/app/quizzes/_components/Tag";
import { QuizSummaryResponse } from "@/lib/types/quiz";

interface QuizCardProps {
  quiz: QuizSummaryResponse;
}

const QuizCard: React.FC<QuizCardProps> = ({ quiz }) => {
  return (
    <div className="border border-border rounded-lg p-4 bg-card-background text-foreground  transition-transform duration-200 hover:scale-105">
      <Link
        href={`/quizzes/${quiz.id}`}
        className="flex flex-col h-full no-underline"
      >
        {/* 퀴즈 제목 */}
        <h3 className="text-lg text-foreground font-bold mb-2">{quiz.title}</h3>

        {/* 퀴즈 메타정보 */}
        <div className="flex flex-wrap gap-1 mb-2">
          {/* 난이도 */}
          <Tag difficultyLevel={quiz.difficultyLevel} className="p-0" />

          {/* 퀴즈 유형 */}
          <Tag quizType={quiz.quizType} />

          {/* 문제 개수 */}
          <Tag questionCount={quiz.questionCount} />
        </div>

        {/* 태그 목록 */}
        <div className="flex flex-wrap gap-2 mb-2">
          {(quiz.tags || []).map((tag) => (
            <Tag key={tag.id} tag={tag} size="small" />
          ))}
        </div>

        {/* 시도 횟수 & 평균 점수 */}
        <div className="text-sm text-muted">
          📊 {quiz.attemptCount}회 도전 | 평균 점수: {quiz.avgScore.toFixed(1)}
        </div>
      </Link>
    </div>
  );
};

export default QuizCard;
