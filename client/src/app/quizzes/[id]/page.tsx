"use client";

import { useRouter, useParams } from "next/navigation";
import { useGetQuizDetail } from "@/lib/api/quiz/useGetQuizDetail";

import { useAuthStore } from "@/store/authStore";
import Image from "next/image";
import Tag from "../_components/Tag";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
} from "chart.js";
import Loading from "@/app/_components/Loading";
import DifficultyChart from "../_components/DifficultyChart";

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend
);

const QuizDetailPage: React.FC = () => {
  const router = useRouter();
  const quizId = useParams().id;

  const { isAuthenticated } = useAuthStore();
  const { isLoading, error, data: quiz } = useGetQuizDetail(Number(quizId));

  const quizStatistics = quiz?.statistics;

  const handleStartQuiz = () => {
    if (!isAuthenticated) {
      router.push(`/login?redirect=/quizzes/${quizId}`);
      return;
    }
    router.push(`/quizzes/${quizId}/play`);
  };

  if (isLoading) {
    return <Loading />;
  }

  if (error || !quiz) {
    return (
      <div className="max-w-full bg-danger-light min-h-screen mx-auto p-6 flex items-center justify-center">
        <div className="text-danger p-4 rounded-md text-center">
          <p className="text-xl font-semibold">
            ❌ 퀴즈 정보를 불러오는 데 실패했습니다.
          </p>
          <button
            onClick={() => router.push("/quizzes")}
            className="mt-3 px-6 py-3 bg-primary hover:bg-primary-hover text-white rounded-lg transition-all"
          >
            🔙 퀴즈 목록으로 돌아가기
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-screen-lg mx-auto p-4 bg-card-background min-h-screen flex flex-col gap-6 pt-8">
      {/* 📌 퀴즈 헤더 */}
      <div className="bg-background p-6 rounded-lg shadow-md border border-border flex flex-col md:flex-row justify-between items-start gap-6">
        {/* 왼쪽: 퀴즈 정보 */}
        <div className="flex-1 flex flex-col gap-3">
          {/* 🏷️ 퀴즈 제목 + 생성일 */}
          <div className="flex items-end">
            <h1 className="text-2xl sm:text-3xl font-bold text-primary">
              {quiz.title}
            </h1>
            <span className="text-sm text-gray-500 hidden sm:inline ml-2">
              {new Date(quiz.createdAt).toLocaleDateString()}
            </span>
          </div>

          {/* 📝 퀴즈 설명 */}
          <p className="text-sm sm:text-[17px] text-foreground">
            {quiz.description}
          </p>

          {/* 🔖 태그 목록 */}
          <div className="flex flex-wrap gap-2">
            <Tag difficultyLevel={quiz.difficultyLevel} />
            <Tag quizType={quiz.quizType} />
            <Tag questionCount={quiz.questionCount} />
          </div>

          {/* 추가된 태그 */}
          {quiz.tags && quiz.tags.length > 0 && (
            <div className="flex flex-wrap gap-2">
              {quiz.tags.map((tag) => (
                <Tag key={tag.id} tag={tag} />
              ))}
            </div>
          )}

          {/* 👤 제작자 정보 */}
          <div className="flex items-center gap-4 mt-2 border-t border-border pt-4">
            {quiz.creator.profileImage ? (
              <Image
                src={quiz.creator.profileImage}
                alt="프로필 이미지"
                width={50}
                height={50}
                className="rounded-full border border-border"
              />
            ) : (
              <div className="w-12 h-12 bg-muted rounded-full flex items-center justify-center text-sm font-semibold">
                {quiz.creator.username.charAt(0)}
              </div>
            )}
            <div className="text-sm">
              <p className="font-semibold text-primary">
                {quiz.creator.username}
              </p>
              <p className="text-xs text-muted">
                레벨 {quiz.creator.level} ・ 가입일:{" "}
                {new Date(quiz.creator.joinedAt).toLocaleDateString()}
              </p>
            </div>
          </div>
        </div>

        {/* 오른쪽: ⏳ 퀴즈 제한 시간 */}
        <div className="flex flex-col border border-border items-center justify-center p-3 sm:p-4 bg-background rounded-lg w-full sm:max-w-xs md:max-w-[10rem] text-center gap-1 sm:gap-2">
          <span className="text-xl sm:text-3xl font-bold text-primary">⏳</span>
          <span className="text-xs text-gray-500">제한 시간</span>
          <span className="text-lg sm:text-3xl font-bold text-primary">
            {Math.floor(quiz.timeLimit / 60)}분
          </span>
        </div>
      </div>

      {/* 🚀 퀴즈 시작 버튼 */}
      <button
        onClick={handleStartQuiz}
        className="w-full bg-primary hover:bg-primary-hover text-white py-3 rounded-md font-semibold text-lg transition-all"
      >
        🚀 퀴즈 시작하기
      </button>

      {/* 📊 퀴즈 통계 */}
      {!quizStatistics ? (
        <div className="bg-background p-6 rounded-lg border border-border shadow-md text-center">
          <p className="text-lg text-danger font-semibold">
            ❌ 통계 데이터를 불러오는 데 실패했습니다.
          </p>
        </div>
      ) : quizStatistics ? (
        <div className="bg-background p-6 rounded-lg border border-border shadow-md">
          <h2 className="text-xl sm:text-2xl font-semibold text-primary mb-4">
            📊 퀴즈 통계
          </h2>

          {/* 🔥 주요 통계 카드 */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <StatCard
              title="🔥 시도"
              value={`${quizStatistics.totalAttempts}`}
            />
            <StatCard
              title="📊 평균 점수"
              value={`${quizStatistics.averageScore?.toFixed(1) || "0"}`}
            />
            <StatCard
              title="✅ 완료율"
              value={`${quizStatistics.completionRate?.toFixed(1) || "0"}%`}
            />
            <StatCard
              title="⏳ 평균 시간"
              value={`${Math.floor(quizStatistics.averageTimeSeconds / 60)}분`}
            />
          </div>

          {/* 📈 난이도 분포 그래프 */}
          {quizStatistics?.difficultyDistribution && (
            <div className="mt-5 w-full">
              <DifficultyChart
                distribution={quiz.statistics?.difficultyDistribution ?? {}}
              />
            </div>
          )}
        </div>
      ) : null}
    </div>
  );
};

/* ✅ StatCard 컴포넌트 */
const StatCard = ({ title, value }: { title: string; value: string }) => {
  return (
    <div className="bg-card-background p-4 rounded-md shadow-sm border border-card-border text-center">
      <p className="text-sm sm:text-base text-foreground">{title}</p>
      <p className="text-base sm:text-lg font-bold text-primary">{value}</p>
    </div>
  );
};

export default QuizDetailPage;
