"use client";

import React from "react";
import classNames from "classnames";
import { TagResponse } from "@/lib/types/tag";
import { QuizType, QuizDifficultyType } from "@/lib/types/quiz";

// ✅ 태그 유형별 색상 조정 (차분한 컬러 팔레트)
const getTagColor = (name: string) => {
  const colorMap: Record<string, string> = {
    // Java 계열 (차분한 블루-그레이 톤)
    Java: "bg-blue-700 text-gray-200", // 더 어두운 블루
    "Core Java": "bg-blue-600 text-gray-200",
    "Java 8+": "bg-blue-500 text-gray-200",
    "Java Collections": "bg-blue-400 text-gray-800",
    "Java Concurrency": "bg-blue-300 text-gray-800",
    "Java OOP": "bg-blue-200 text-gray-800",
    JVM: "bg-blue-800 text-gray-200",

    // Spring 계열 (뮤트된 세이지 그린 톤)
    Spring: "bg-emerald-700 text-gray-200", // 더 어두운 그린
    "Spring Core": "bg-emerald-600 text-gray-200",
    "Spring Boot": "bg-emerald-500 text-gray-200",
    "Spring MVC": "bg-emerald-400 text-gray-800",
    "Spring Data": "bg-emerald-300 text-gray-800",
    "Spring Security": "bg-emerald-800 text-gray-200",

    // 기존 태그 (뮤트 톤으로 변경)
    자바스크립트: "bg-amber-600 text-gray-200", // 더 어두운 앰버
    파이썬: "bg-sky-600 text-gray-200", // 더 어두운 스카이 블루
    데이터베이스: "bg-teal-600 text-gray-200", // 더 어두운 틸
    알고리즘: "bg-purple-600 text-gray-200", // 더 어두운 퍼플
    자료구조: "bg-indigo-600 text-gray-200", // 더 어두운 인디고
    시스템설계: "bg-gray-600 text-gray-200",
    네트워크: "bg-cyan-600 text-gray-200", // 더 어두운 시안
    운영체제: "bg-rose-600 text-gray-200", // 더 어두운 로즈
    웹개발: "bg-blue-600 text-gray-200", // 더 어두운 블루
    데브옵스: "bg-teal-600 text-gray-200", // 더 어두운 틸
    머신러닝: "bg-orange-600 text-gray-200", // 더 어두운 오렌지
    보안: "bg-red-600 text-gray-200", // 더 어두운 레드
  };

  return colorMap[name] || "bg-gray-400 text-gray-800"; // 기본 색상도 차분하게
};

// ✅ 퀴즈 유형 한글 라벨 매핑
const quizTypeLabels: Record<QuizType, string> = {
  [QuizType.DAILY]: "데일리 퀴즈",
  [QuizType.TAG_BASED]: "태그 기반",
  [QuizType.TOPIC_BASED]: "주제 기반",
  [QuizType.CUSTOM]: "커스텀",
};

// ✅ 난이도 한글 라벨 매핑
const difficultyLabels: Record<QuizDifficultyType, string> = {
  [QuizDifficultyType.BEGINNER]: "입문",
  [QuizDifficultyType.INTERMEDIATE]: "중급",
  [QuizDifficultyType.ADVANCED]: "고급",
};

// ✅ 난이도 색상 조정 (차분한 톤으로 변경)
const getDifficultyColor = (difficulty: QuizDifficultyType) => {
  const difficultyMap: Record<QuizDifficultyType, string> = {
    [QuizDifficultyType.BEGINNER]: "bg-emerald-400 text-gray-800",
    [QuizDifficultyType.INTERMEDIATE]: "bg-amber-400 text-gray-800",
    [QuizDifficultyType.ADVANCED]: "bg-rose-400 text-gray-200",
  };
  return difficultyMap[difficulty] || "bg-gray-400 text-gray-800";
};

// ✅ 퀴즈 유형 색상 조정 (차분한 톤으로 변경)
const getQuizTypeColor = (quizType: QuizType) => {
  const quizTypeMap: Record<QuizType, string> = {
    [QuizType.DAILY]: "bg-sky-500 text-gray-200",
    [QuizType.TAG_BASED]: "bg-purple-500 text-gray-200",
    [QuizType.TOPIC_BASED]: "bg-indigo-500 text-gray-200",
    [QuizType.CUSTOM]: "bg-gray-500 text-gray-200",
  };
  return quizTypeMap[quizType] || "bg-gray-400 text-gray-800";
};

// ✅ 문제 개수 스타일 조정 (차분한 톤으로 변경)
export const getQuestionCountStyle = (count: number) => {
  if (count >= 10) return "bg-orange-500 text-gray-200 font-semibold";
  if (count >= 5) return "bg-amber-400 text-gray-800";
  return "bg-gray-300 text-gray-800";
};

const Tag: React.FC<{
  tag?: TagResponse;
  quizType?: QuizType;
  difficultyLevel?: QuizDifficultyType;
  questionCount?: number;
  onClick?: () => void;
  size?: "small" | "medium" | "large";
  isSelected?: boolean;
  className?: string;
}> = ({
  tag,
  quizType,
  difficultyLevel,
  questionCount,
  onClick,
  size = "medium",
  isSelected,
  className,
}) => {
  const sizeClasses = {
    small: "px-1.5 py-0.5 text-xs",
    medium: "px-2 py-1 text-sm",
    large: "px-3 py-1.5 text-base",
  };

  return (
    <div className="relative group">
      <button
        onClick={onClick}
        className={classNames(
          "px-2 py-0.5 text-xs font-medium rounded-md shadow-sm transition-all duration-200",
          sizeClasses[size],
          className,
          {
            // ✅ isSelected 테두리 색상 차분하게 변경
            "ring-2 ring-gray-500 ring-offset-2": isSelected,
            "hover:shadow-sm": !isSelected, // 호버 효과도 가볍게 조정
            "cursor-pointer": isSelected || !isSelected,
            "cursor-default": isSelected === undefined,
          },
          tag
            ? getTagColor(tag.name)
            : quizType
            ? getQuizTypeColor(quizType)
            : difficultyLevel
            ? getDifficultyColor(difficultyLevel)
            : questionCount !== undefined
            ? getQuestionCountStyle(questionCount)
            : "bg-gray-400 text-gray-800"
        )}
      >
        <span className="flex items-center gap-1">
          {tag?.name ||
            (quizType && quizTypeLabels[quizType]) ||
            (difficultyLevel && difficultyLabels[difficultyLevel]) ||
            (questionCount && `${questionCount}문제`)}
          {tag?.quizCount !== undefined && (
            <span
              className={classNames(
                "ml-1 px-1.5 py-0.5 text-xs rounded-full bg-white/10" // 배경 투명도 낮춰 차분하게
              )}
            >
              {tag.quizCount}
            </span>
          )}
        </span>
      </button>
      {/* ✅ 태그 설명 툴팁 (차분한 스타일로 변경) */}
      {tag && (
        <div className="absolute left-1/2 -translate-x-1/2 bottom-full mb-2 w-48 bg-gray-700 text-gray-300 text-xs rounded-lg p-2 opacity-0 group-hover:opacity-100 transition-opacity duration-200 shadow-sm z-50 pointer-events-none">
          {tag.description} ({tag.quizCount}개 퀴즈)
          {(tag?.synonyms ?? []).length > 0 && (
            <div className="text-xs text-gray-500 mt-1">
              <span className="font-semibold">유사 태그:</span>{" "}
              {tag?.synonyms?.join(", ")}
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default Tag;
