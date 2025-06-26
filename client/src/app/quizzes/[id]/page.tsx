"use client";

import { useRouter, useParams } from "next/navigation";
import { useGetQuizDetail } from "@/lib/api/quiz/useGetQuizDetail";
import { useGetQuizStatistics } from "@/lib/api/quiz/useGetQuizStatistics";

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
import StatCard from "./../../(user)/_components/StatsCard";
import QuestionStatisticsChart from "../_components/QuestionStaticsChart";

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
  const { data: quizStatistics } = useGetQuizStatistics(Number(quizId));
  const quizStatics = quizStatistics?.data;

  console.log(quizStatics);
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

          {quiz.creator}
          {/* 👤 제작자 정보 */}
          <div className="flex items-center gap-4 mt-2 border-t border-border pt-4">
            {quiz.creatorProfileImage ? (
              <>
                <p>출제자</p>
                <Image
                  src={quiz.creatorProfileImage}
                  alt="프로필 이미지"
                  width={30}
                  height={30}
                  className="rounded-full border border-border"
                />
              </>
            ) : (
              <div className="w-12 h-12 bg-muted rounded-full flex items-center justify-center text-sm font-semibold">
                {quiz.creatorUsername.charAt(0)}
              </div>
            )}
            <div className="text-sm">
              <p className="font-semibold text-primary">
                {quiz.creatorUsername}
              </p>
            </div>
          </div>
        </div>

        {/* 오른쪽: ⏳ 퀴즈 제한 시간 */}
        <div className="flex flex-col border border-border items-center justify-center p-3 sm:p-4 bg-background rounded-lg w-full sm:max-w-xs md:max-w-[10rem] text-center gap-1 sm:gap-2">
          <span className="text-xl sm:text-3xl font-bold text-primary">⏳</span>
          <span className="text-xs text-gray-500">제한 시간</span>
          <span className="text-lg sm:text-2xl font-bold text-primary">
            {`${Math.floor((quiz.timeLimit * quiz.questionCount) / 60)}분 ${
              (quiz.timeLimit * quiz.questionCount) % 60
            }초`}
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
      <div className="bg-background p-6 rounded-lg border border-border shadow-md">
        <h2 className="text-lg sm:text-xl font-semibold text-primary mb-4">
          📊 통계
        </h2>

        <div className="flex flex-col md:flex-row gap-4">
          {/* 왼쪽: 통계 카드 (2/3) */}
          <div className="md:flex-2 w-full gap-4">
            <div className="grid grid-cols-2 md:grid-cols-1 lg:grid-cols-2 gap-2">
              <StatCard
                title="시도 횟수"
                value={`${quizStatics?.totalAttempts ?? 0}`}
                isDummy={!quizStatics}
              />
              <StatCard
                title="평균 점수"
                value={`${quizStatics?.averageScore?.toFixed(1) ?? "0.0"}`}
                isDummy={!quizStatics}
              />
              <StatCard
                title="평균 시간"
                value={`${quizStatics?.averageTimeSeconds ?? 0}`}
                isDummy={!quizStatics}
              />
              <StatCard
                title="완료율"
                value={`${quizStatics?.completionRate?.toFixed(1) ?? "0.0"}%`}
                isDummy={!quizStatics}
              />
            </div>
          </div>

          {/* 오른쪽: 난이도 차트 (1/3) */}
          <div className="flex-1 w-full md:w-1/3 h-full flex items-center justify-center">
            <DifficultyChart
              distribution={quizStatics?.difficultyDistribution ?? {}}
              totalCount={quiz.questionCount}
            />
          </div>
        </div>
        {/* 📈 통계 그래프 */}
        <div className="max-w-full mt-6 min-h-64">
          <QuestionStatisticsChart
            statistics={quizStatics?.questionStatistics ?? []}
          />
        </div>
      </div>
    </div>
  );
};

export default QuizDetailPage;
