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
    <section className="bg-background border border-card-border shadow-sm max-w-screen-xl mx-auto text-foreground p-12 rounded-xl shadow-lg flex flex-col items-center text-center">
      {isAuthenticated ? (
        <>
          <h1 className="text-4xl font-bold mb-4 text-primary drop-shadow-md">
            🎉 반가워요! 오늘도 학습을 시작해볼까요?
          </h1>
          <p className="text-xl text-neutral max-w-3xl leading-relaxed">
            계속해서 퀴즈를 풀며 CS 지식을 쌓아보세요!
          </p>
          <Link href="/mypage">
            <Button
              variant="primary"
              size="large"
              className="mt-6 px-6 py-3 font-semibold text-white"
            >
              대시보드로 이동 🚀
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
      <section className="w-full min-h-[300px] mx-auto mt-8 flex flex-col md:flex-row gap-6">
        {/* ✅ 오늘의 퀴즈 */}
        <div className="flex-1 bg-card border-2 border-card-border p-6 rounded-xl shadow-sm hover:shadow-md transition text-center flex flex-col items-center justify-center gap-2 min-h-[180px]">
          <CalendarDays size={32} className="text-primary" />
          <h2 className="text-lg font-semibold text-foreground">오늘의 퀴즈</h2>
          {isAuthenticated ? (
            isLoadingDaily ? (
              <p className="text-sm text-neutral">퀴즈 불러오는 중...</p>
            ) : dailyError ? (
              <p className="text-sm text-neutral">퀴즈를 불러올 수 없습니다.</p>
            ) : dailyQuizzes?.data ? (
              <>
                <p className="text-base text-neutral">
                  {dailyQuizzes.data.title}
                </p>
                <Link href={`/quiz/daily/${dailyQuizzes.data.id}`}>
                  <Button variant="secondary" size="small" className="mt-3">
                    도전하기 🚀
                  </Button>
                </Link>
              </>
            ) : (
              <p className="text-sm text-neutral">오늘의 퀴즈가 없습니다.</p>
            )
          ) : (
            <p className="text-sm text-neutral">
              로그인하면 오늘의 퀴즈를 확인할 수 있어요.
            </p>
          )}
        </div>

        {/* ✅ 추천 퀴즈 */}
        <div className="flex-1 bg-card border-2 border-card-border p-6 rounded-xl shadow-sm hover:shadow-md transition text-center flex flex-col items-center justify-center gap-2 min-h-[180px]">
          <Sparkles size={32} className="text-secondary" />
          <h2 className="text-lg font-semibold text-foreground">추천 퀴즈</h2>
          {isAuthenticated ? (
            isLoadingRecommended ? (
              <p className="text-sm text-neutral">퀴즈 불러오는 중...</p>
            ) : recommendedError ? (
              <p className="text-sm text-neutral">퀴즈를 불러올 수 없습니다.</p>
            ) : recommendedQuizzes?.data?.length ? (
              <>
                <p className="text-base text-neutral">
                  {recommendedQuizzes.data[0]?.title}
                </p>
                <Link
                  href={`/quiz/recommended/${recommendedQuizzes.data[0]?.id}`}
                >
                  <Button variant="secondary" size="small" className="mt-3">
                    풀어보기 🌟
                  </Button>
                </Link>
              </>
            ) : (
              <p className="text-sm text-neutral">추천 퀴즈가 없습니다.</p>
            )
          ) : (
            <p className="text-sm text-neutral">
              로그인하면 추천 퀴즈를 확인할 수 있어요.
            </p>
          )}
        </div>
      </section>
    </section>
  );
};

export default AuthSection;
