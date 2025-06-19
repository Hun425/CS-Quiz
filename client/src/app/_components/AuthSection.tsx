"use client";

import Link from "next/link";
import { useAuthStore } from "@/store/authStore";
import { useGetDailyQuizzes } from "@/lib/api/quiz/useGetDailyQuizzes";
import { useGetRecommendedQuizzes } from "@/lib/api/quiz/useGetRecommendedQuizzes";
import { CalendarDays, Sparkles } from "lucide-react";
import Button from "../_components/Button";
import Image from "next/image";
import { motion } from "framer-motion"; // ✅ 애니메이션용 추가!

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
    <section className="max-w-screen-xl text-foreground px-2 my-16 rounded-xl flex flex-col items-center text-center overflow-hidden">
      {/* ✅ 메인 영역 */}
      <div className="flex flex-col lg:flex-row items-center justify-between w-full gap-10 relative">
        {/* ✨ 왼쪽 텍스트/버튼/카드 */}
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          className="flex flex-col items-start text-left max-w-xl w-full gap-6 z-10"
        >
          <div className="flex flex-col gap-4">
            {isAuthenticated ? (
              <>
                <h1 className="text-3xl sm:text-4xl font-bold text-primary drop-shadow-md leading-tight">
                  반가워요! 🎉
                  <br />
                  오늘도 한 걸음 성장해볼까요?
                </h1>
                <p className="text-base sm:text-lg text-muted leading-relaxed">
                  짧은 시간 안에 깊이 있는 CS 지식을 쌓아보세요.
                </p>
                <Link href="/quizzes">
                  <Button
                    variant="primary"
                    size="large"
                    className="mt-2 px-5 py-2.5 text-base font-semibold"
                  >
                    퀴즈 시작하기 🚀
                  </Button>
                </Link>
              </>
            ) : (
              <>
                <h1 className="text-4xl sm:text-5xl font-bold text-primary drop-shadow-md leading-tight">
                  쉽고 빠른 CS 퀴즈 학습
                </h1>
                <p className="text-lg sm:text-xl text-muted leading-relaxed">
                  실시간 대결과 다양한 퀴즈로 재미있게 배우세요!
                </p>
                <Link href="/login">
                  <Button
                    variant="primary"
                    size="large"
                    className="mt-2 px-5 py-2.5 text-base font-semibold"
                  >
                    로그인하고 시작하기 🚀
                  </Button>
                </Link>
              </>
            )}
          </div>

          {/* ✨ 퀴즈 카드 2개 */}
          <div className="grid grid-cols-2 gap-3 mt-6 w-full">
            {/* 오늘의 퀴즈 */}
            <motion.div
              whileHover={{ scale: 1.05 }}
              className="bg-card-background border border-border px-3 py-4 rounded-lg hover:border-primary hover:bg-sub-background transition flex flex-col items-center justify-center gap-1 min-h-[100px]"
            >
              <CalendarDays size={18} className="text-primary mb-1" />
              <h2 className="text-sm font-semibold text-foreground">
                오늘의 퀴즈
              </h2>
              {isAuthenticated ? (
                isLoadingDaily ? (
                  <p className="text-xs text-muted">불러오는 중...</p>
                ) : dailyError ? (
                  <p className="text-xs text-muted">불러올 수 없습니다.</p>
                ) : dailyQuizzes?.data &&
                  Object.keys(dailyQuizzes.data).length > 0 ? (
                  <Link href={`/quizzes/${dailyQuizzes.data.id}`}>
                    <button className="text-xs text-primary hover:underline transition">
                      {dailyQuizzes.data.title}
                    </button>
                  </Link>
                ) : (
                  <p className="text-xs text-muted">없습니다.</p>
                )
              ) : (
                <p className="text-xs text-muted">로그인이 필요합니다.</p>
              )}
            </motion.div>

            {/* 추천 퀴즈 */}
            <motion.div
              whileHover={{ scale: 1.05 }}
              className="bg-card-background border border-border px-3 py-4 rounded-lg hover:border-primary hover:bg-sub-background transition flex flex-col items-center justify-center gap-1 min-h-[100px]"
            >
              <Sparkles size={18} className="text-secondary mb-1" />
              <h2 className="text-sm font-semibold text-foreground">
                추천 퀴즈
              </h2>
              {isAuthenticated ? (
                isLoadingRecommended ? (
                  <p className="text-xs text-muted">불러오는 중...</p>
                ) : recommendedError ? (
                  <p className="text-xs text-muted">불러올 수 없습니다.</p>
                ) : recommendedQuizzes?.data?.length &&
                  Object.keys(recommendedQuizzes.data[0] || {}).length > 0 ? (
                  <Link href={`/quizzes/${recommendedQuizzes.data[0].id}`}>
                    <button className="text-xs text-primary hover:underline transition">
                      {recommendedQuizzes.data[0].title}
                    </button>
                  </Link>
                ) : (
                  <p className="text-xs text-muted">없습니다.</p>
                )
              ) : (
                <p className="text-xs text-muted">로그인이 필요합니다.</p>
              )}
            </motion.div>
          </div>
        </motion.div>

        {/* ✨ 오른쪽 이미지 */}
        <motion.div
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.7 }}
          className="hidden sm:block relative w-[400px] h-[400px] sm:w-[500px] sm:h-[500px] flex-shrink-0"
        >
          <Image
            src="/images/quiz-main2.png"
            alt="퀴즈 메인 이미지"
            fill
            className="object-contain"
            priority
          />
        </motion.div>

        {/* ✨ sm 이하에서는 이미지가 텍스트 위로 살짝 겹치게 */}
        <div className="absolute top-0 left-1/2 transform -translate-x-1/2 sm:hidden w-3/4 opacity-10 z-0">
          <Image
            src="/images/quiz-main2.png"
            alt="퀴즈 배경 이미지"
            width={300}
            height={300}
            className="object-contain"
          />
        </div>
      </div>
    </section>
  );
};

export default AuthSection;
