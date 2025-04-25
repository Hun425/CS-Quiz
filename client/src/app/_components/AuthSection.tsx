"use client";

import Link from "next/link";
import { useAuthStore } from "@/store/authStore";
import { useGetDailyQuizzes } from "@/lib/api/quiz/useGetDailyQuizzes";
import { useGetRecommendedQuizzes } from "@/lib/api/quiz/useGetRecommendedQuizzes";
import { CalendarDays, Sparkles } from "lucide-react";
import Button from "../_components/Button";

const AuthSection = () => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  const {
    data: dailyQuizzes,
    isLoading: isLoadingDaily,
    error: dailyError,
  } = useGetDailyQuizzes();

  const {
    data: recommendedQuizzes,
    isLoading: isLoadingRecommended,
    error: recommendedError,
  } = useGetRecommendedQuizzes({ limit: 3 });

  return (
    <section className="bg-background border border-card-border shadow-sm max-w-screen-xl mx-auto text-foreground p-12 rounded-xl  flex flex-col items-center text-center">
      {isAuthenticated ? (
        <>
          <h1 className="text-3xl sm:text-4xl font-bold mb-3 text-primary drop-shadow-md leading-snug">
            반가워요🎉
            <br />
            오늘도 퀴즈 한 걸음!
          </h1>
          <p className="text-base sm:text-xl text-neutral max-w-xl leading-relaxed">
            짧은 시간, 깊이 있는 CS 학습을 시작해보세요!
          </p>

          <Link href="/quizzes">
            <Button
              variant="primary"
              size="large"
              className="mt-6 px-6 py-3 font-semibold text-white"
            >
              퀴즈 시작하기 🚀
            </Button>
          </Link>
        </>
      ) : (
        <>
          <h1 className="text-5xl font-bold mb-4 text-primary drop-shadow-md">
            쉽고 재미있는 CS 퀴즈 학습
          </h1>
          <p className="text-xl text-neutral max-w-3xl leading-relaxed">
            <strong>실시간 경쟁</strong>과 <strong>퀴즈 챌린지</strong>로 CS
            지식을 쌓아보세요. <br />
            재미있게 배우고, 빠르게 성장하세요.
          </p>
          <Link href={"/login"}>
            <Button
              variant="primary"
              size="large"
              className="mt-6 px-6 py-3 font-semibold text-white"
            >
              로그인하고 시작하기 🚀
            </Button>
          </Link>
        </>
      )}

      {/* 오늘의 퀴즈 & 추천 퀴즈 */}
      <section className="w-full max-w-screen-xl mx-auto mt-8 grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* ✅ 오늘의 퀴즈 */}
        <div className="flex-1 bg-card-background border border-border px-4 py-5 rounded-lg hover:border-primary hover:bg-sub-background transition text-center flex flex-col items-center justify-center gap-1 min-h-[130px]">
          <CalendarDays size={24} className="text-primary mb-1" />
          <h2 className="text-base font-medium text-foreground">오늘의 퀴즈</h2>

          {isAuthenticated ? (
            isLoadingDaily ? (
              <p className="text-sm text-muted">퀴즈 불러오는 중...</p>
            ) : dailyError ? (
              <p className="text-sm text-muted">퀴즈를 불러올 수 없습니다.</p>
            ) : dailyQuizzes?.data &&
              Object.keys(dailyQuizzes.data).length > 0 ? (
              <Link href={`/quizzes/${dailyQuizzes.data.id}`} passHref>
                <button className="text-sm text-primary hover:underline transition">
                  {dailyQuizzes.data.title}
                </button>
              </Link>
            ) : (
              <p className="text-sm text-muted">오늘의 퀴즈가 없습니다.</p>
            )
          ) : (
            <p className="text-sm text-muted">
              로그인하면 오늘의 퀴즈를 확인할 수 있어요.
            </p>
          )}
        </div>

        {/* ✅ 추천 퀴즈 */}
        <div className="flex-1 bg-card-background border border-border rounded-lg px-4 py-5 transition hover:border-primary hover:bg-sub-background text-center flex flex-col items-center justify-center gap-1 min-h-[140px]">
          <Sparkles size={24} className="text-secondary mb-1" />
          <h2 className="text-base font-medium text-foreground">추천 퀴즈</h2>

          {isAuthenticated ? (
            isLoadingRecommended ? (
              <p className="text-sm text-muted">퀴즈 불러오는 중...</p>
            ) : recommendedError ? (
              <p className="text-sm text-muted">퀴즈를 불러올 수 없습니다.</p>
            ) : recommendedQuizzes?.data?.length &&
              Object.keys(recommendedQuizzes.data[0] || {}).length > 0 ? (
              <Link href={`/quizzes/${recommendedQuizzes.data[0].id}`} passHref>
                <button className="text-sm text-primary hover:underline transition">
                  {recommendedQuizzes.data[0].title}
                </button>
              </Link>
            ) : (
              <p className="text-sm text-muted">추천 퀴즈가 없습니다.</p>
            )
          ) : (
            <p className="text-sm text-muted">
              로그인하면 추천 퀴즈를 확인할 수 있어요.
            </p>
          )}
        </div>
      </section>
    </section>
  );
};

export default AuthSection;
