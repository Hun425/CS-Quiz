"use client";

import Link from "next/link";
import { useAuthStore } from "@/store/authStore";
import { useGetDailyQuizzes } from "@/lib/api/quiz/useGetDailyQuizzes";
import { useGetRecommendedQuizzes } from "@/lib/api/quiz/useGetRecommendedQuizzes";
import { CalendarDays, Sparkles } from "lucide-react";
import Button from "../_components/Button";

// ✅ 로그인하지 않았을 때 기본 제공할 추천 퀴즈 목록
const defaultRecommendedQuizzes = [
  { id: "1", title: "자료구조 기초 퀴즈" },
  { id: "2", title: "알고리즘 난이도별 문제" },
  { id: "3", title: "네트워크 개념 테스트" },
  { id: "4", title: "데이터베이스 기본 문법" },
];

const AuthSection = () => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  // ✅ 오늘의 퀴즈 API 호출
  const {
    data: dailyQuizzes,
    isLoading: isLoadingDaily,
    error: dailyError,
  } = useGetDailyQuizzes();

  // ✅ 추천 퀴즈 API 호출
  const {
    data: recommendedQuizzes,
    isLoading: isLoadingRecommended,
    error: recommendedError,
  } = useGetRecommendedQuizzes({ limit: 3 });

  // ✅ 비로그인 상태에서 랜덤 추천 퀴즈 선택
  const randomQuiz =
    defaultRecommendedQuizzes[
      Math.floor(Math.random() * defaultRecommendedQuizzes.length)
    ];

  return (
    <section className="bg-background border border-card-border shadow-sm max-w-screen-2xl mx-auto text-foreground p-12 rounded-xl shadow-lg flex flex-col items-center text-center">
      {isAuthenticated ? (
        <>
          <h1 className="text-4xl font-bold mb-4 text-primary drop-shadow-md">
            🎉 반가워요! 오늘도 학습을 시작해볼까요?
          </h1>
          <p className="text-xl text-neutral max-w-3xl leading-relaxed">
            계속해서 퀴즈를 풀며 CS 지식을 쌓아보세요!
          </p>
          <Link href="/dashboard">
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

      {/* 오늘의 퀴즈 & 추천 퀴즈 (항상 표시됨) */}
      <section className="max-w-screen-xl min-h-[250px] mx-auto mt-8 flex flex-col md:flex-row gap-4">
        {/* ✅ 오늘의 퀴즈 */}
        <div className="flex-1 bg-card border-2 border-card-border p-5 rounded-xl shadow-md hover:shadow-lg transition text-center flex flex-col justify-center">
          <CalendarDays size={28} className="text-primary mx-auto mb-3" />
          <h2 className="text-lg font-semibold text-foreground mb-3">
            오늘의 퀴즈
          </h2>
          {isAuthenticated ? (
            isLoadingDaily ? (
              <p className="text-sm text-neutral">불러오는 중...</p>
            ) : dailyError ? (
              <p className="text-sm text-danger">퀴즈 로딩 실패</p>
            ) : dailyQuizzes?.data?.length ? (
              <>
                <p className="text-sm md:text-base text-neutral">
                  {dailyQuizzes.data[0]?.title}
                </p>
                <Link href={`/quiz/daily/${dailyQuizzes.data[0]?.id}`}>
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
        <div className="flex-1 bg-card border-2 border-card-border p-5 rounded-xl shadow-md hover:shadow-lg transition text-center flex flex-col justify-center">
          <Sparkles size={28} className="text-secondary mx-auto mb-3" />
          <h2 className="text-lg font-semibold text-foreground mb-3">
            추천 퀴즈
          </h2>
          {isAuthenticated ? (
            isLoadingRecommended ? (
              <p className="text-sm text-neutral">불러오는 중...</p>
            ) : recommendedError ? (
              <p className="text-sm text-danger">퀴즈 로딩 실패</p>
            ) : recommendedQuizzes?.data?.length ? (
              <>
                <p className="text-sm md:text-base text-neutral">
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
            <>
              <p className="text-sm md:text-base text-neutral">
                {randomQuiz.title}
              </p>
              <Link href={`/quiz/recommended/${randomQuiz.id}`}>
                <Button variant="secondary" size="small" className="mt-3">
                  풀어보기 🌟
                </Button>
              </Link>
            </>
          )}
        </div>
      </section>
    </section>
  );
};

export default AuthSection;
