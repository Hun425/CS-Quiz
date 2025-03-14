"use client";

import React from "react";
import classNames from "classnames";
import { TagResponse } from "@/lib/types/tag";
import { QuizType, QuizDifficultyType } from "@/lib/types/quiz";

// ✅ 태그 유형별 색상 조정 (부드러운 컬러로 변경)
const getTagColor = (name: string) => {
  const colorMap: Record<string, string> = {
    자바스크립트: "bg-amber-300 text-black",
    파이썬: "bg-blue-400 text-white",
    데이터베이스: "bg-emerald-400 text-white",
    알고리즘: "bg-violet-400 text-white",
    자료구조: "bg-indigo-400 text-white",
    시스템설계: "bg-gray-600 text-white",
    네트워크: "bg-sky-500 text-white",
    운영체제: "bg-rose-500 text-white",
    웹개발: "bg-cyan-500 text-white",
    데브옵스: "bg-teal-500 text-white",
    머신러닝: "bg-orange-400 text-white",
    보안: "bg-red-500 text-white",
  };

  return colorMap[name] || "bg-gray-300 text-black"; // 기본 색상
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

// ✅ 난이도 색상 조정 (기존 대비 부드럽게)
const getDifficultyColor = (difficulty: QuizDifficultyType) => {
  const difficultyMap: Record<QuizDifficultyType, string> = {
    [QuizDifficultyType.BEGINNER]: "bg-lime-400 text-black",
    [QuizDifficultyType.INTERMEDIATE]: "bg-amber-500 text-black",
    [QuizDifficultyType.ADVANCED]: "bg-rose-500 text-white",
  };
  return difficultyMap[difficulty] || "bg-gray-300 text-black"; // 기본 색상
};

// ✅ 퀴즈 유형 색상 조정
const getQuizTypeColor = (quizType: QuizType) => {
  const quizTypeMap: Record<QuizType, string> = {
    [QuizType.DAILY]: "bg-sky-400 text-black",
    [QuizType.TAG_BASED]: "bg-violet-400 text-white",
    [QuizType.TOPIC_BASED]: "bg-indigo-400 text-white",
    [QuizType.CUSTOM]: "bg-gray-500 text-white",
  };
  return quizTypeMap[quizType] || "bg-gray-300 text-black"; // 기본 색상
};

// ✅ 문제 개수 스타일 조정
export const getQuestionCountStyle = (count: number) => {
  if (count >= 20) return "bg-rose-400 text-white font-semibold";
  if (count >= 10) return "bg-amber-300 text-black";
  return "bg-gray-200 text-black";
};

const Tag: React.FC<{
  tag?: TagResponse;
  quizType?: QuizType;
  difficultyLevel?: QuizDifficultyType;
  questionCount?: number;
  onClick?: () => void;
  selected?: boolean;
  className?: string;
}> = ({
  tag,
  quizType,
  difficultyLevel,
  questionCount,
  onClick,
  selected = false,
  className,
}) => {
  return (
    <div className="relative group">
      <button
        onClick={onClick}
        className={classNames(
          "px-2 py-1 text-xs font-semibold rounded-md  shadow-md",
          className,
          selected ? "ring-2 ring-offset-1 ring-black" : "",
          tag
            ? getTagColor(tag.name)
            : quizType
            ? getQuizTypeColor(quizType)
            : difficultyLevel
            ? getDifficultyColor(difficultyLevel)
            : questionCount !== undefined
            ? getQuestionCountStyle(questionCount)
            : "bg-gray-300 text-black"
        )}
      >
        {tag?.name ||
          (quizType && quizTypeLabels[quizType]) ||
          (difficultyLevel && difficultyLabels[difficultyLevel]) ||
          (questionCount && `${questionCount}문제`)}

        {tag?.quizCount !== undefined && (
          <span
            className={classNames("px-1 py-1 text-xs text-muted  rounded-md")}
          >
            ({tag.quizCount}문제)
          </span>
        )}
      </button>
      {/* ✅ 태그 설명 툴팁 (호버 시 표시) */}
      {tag && (
        <div className="absolute left-0 bottom-full mt-1 w-48 bg-gray-700 text-white text-xs rounded p-2 opacity-0 group-hover:opacity-100 transition-opacity duration-200 z-20">
          {tag.description} ({tag.quizCount}개 퀴즈)
          {/* ✅ 동의어 표시 (존재할 경우) */}
          {(tag?.synonyms ?? []).length > 0 && ( // ⬅️ undefined 방지
            <div className="text-xs text-white mt-1">
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
