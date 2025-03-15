"use client";

import { useRouter, useParams } from "next/navigation";
import { useGetQuizDetail } from "@/lib/api/quiz/useGetQuizDetail";
import { useAuthStore } from "@/store/authStore";
import Tag from "../_components/Tag";

const QuizDetailPage: React.FC = () => {
  const router = useRouter();
  const quizId = useParams().id;
  const { isAuthenticated } = useAuthStore();
  const { isLoading, error, data: quiz } = useGetQuizDetail(Number(quizId));

  const handleStartQuiz = () => {
    if (!isAuthenticated) {
      router.push(`/login?redirect=/quizzes/${quizId}`);
      return;
    }
    router.push(`/quizzes/${quizId}/play`);
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-screen text-lg">
        🔄 퀴즈 정보를 불러오는 중...
      </div>
    );
  }

  if (error || !quiz) {
    return (
      <div className="max-w-4xl bg-danger-light min-h-screen mx-auto p-6 flex items-center justify-center ">
        <div className="text-danger p-4 rounded-md text-center">
          <p className="text-xl font-semibold">
            ❌ 퀴즈 정보를 불러오는 데 실패했습니다.
          </p>
          <button
            onClick={() => router.push("/quizzes")}
            className="mt-4 px-6 py-3 bg-primary hover:bg-primary-hover text-white rounded-lg transition-all"
          >
            🔙 퀴즈 목록으로 돌아가기
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-screen-lg mx-auto p-6 bg-card-background min-h-screen flex flex-col gap-6 justify-center">
      {/* 퀴즈 헤더 */}
      <div className="bg-background p-6 rounded-lg mb-6 shadow-md border border-border">
        <h1 className="text-3xl font-bold text-primary">{quiz.title}</h1>
        <p className="text-foreground mt-2">{quiz.description}</p>

        <div className="mt-4 flex flex-wrap gap-3">
          <Tag difficultyLevel={quiz.difficultyLevel} />
          <Tag quizType={quiz.quizType} />
          <Tag questionCount={quiz.questionCount} />
        </div>

        <div className="mt-4 flex flex-wrap gap-2">
          {quiz.tags.map((tag) => (
            <Tag key={tag.id} tag={tag} />
          ))}
        </div>

        <button
          onClick={handleStartQuiz}
          className="mt-6 w-full bg-primary hover:bg-primary-hover text-white py-3 rounded-lg font-semibold text-lg transition-all"
        >
          🚀 퀴즈 시작하기
        </button>
      </div>

      {/* 퀴즈 통계 */}
      {quiz.statistics && (
        <div className="bg-background p-6 rounded-lg border border-border mb-6 shadow-md">
          <h2 className="text-xl font-semibold text-primary">📊 퀴즈 통계</h2>
          <div className="grid grid-cols-2 gap-6 mt-4">
            <StatCard
              title="🔥 시도 횟수"
              value={`${quiz.statistics.totalAttempts}회`}
            />
            <StatCard
              title="📊 평균 점수"
              value={`${quiz.statistics.averageScore?.toFixed(1) || "0"}점`}
            />
            <StatCard
              title="✅ 완료율"
              value={`${quiz.statistics.completionRate?.toFixed(1) || "0"}%`}
            />
            <StatCard
              title="⏳ 평균 소요 시간"
              value={`${Math.floor(
                quiz.statistics.averageTimeSeconds / 60
              )}분 ${quiz.statistics.averageTimeSeconds % 60}초`}
            />
          </div>
        </div>
      )}
    </div>
  );
};

// ✅ 통계 카드 컴포넌트
const StatCard = ({ title, value }: { title: string; value: string }) => {
  return (
    <div className="bg-card-background p-4 rounded-lg shadow-sm border border-card-border text-center">
      <p className="text-sm text-foreground">{title}</p>
      <p className="text-lg font-bold text-foreground">{value}</p>
    </div>
  );
};

export default QuizDetailPage;
