"use client";

import React from "react";
import classNames from "classnames";
import { TagResponse } from "@/lib/types/api";

interface TagProps {
  tag: TagResponse;
  onClick?: () => void;
  selected?: boolean;
}

// ✅ 태그 유형별 색상 지정
const getTagColor = (name: string) => {
  const colorMap: Record<string, string> = {
    자바스크립트: "bg-yellow-400 text-black", // JavaScript
    파이썬: "bg-blue-500 text-white", // Python
    데이터베이스: "bg-green-500 text-white", // Database
    알고리즘: "bg-purple-500 text-white", // Algorithm
    자료구조: "bg-indigo-500 text-white", // Data Structure
    시스템설계: "bg-gray-700 text-white", // System Design
    네트워크: "bg-blue-700 text-white", // Network
    운영체제: "bg-red-600 text-white", // Operating System
    웹개발: "bg-sky-500 text-white", // Web Development
    데브옵스: "bg-teal-600 text-white", // DevOps
    머신러닝: "bg-orange-500 text-white", // Machine Learning
    보안: "bg-rose-600 text-white", // Security
  };

  return colorMap[name] || "bg-gray-200 text-black"; // 기본 색상
};

const Tag: React.FC<TagProps> = ({ tag, onClick, selected = false }) => {
  return (
    <button
      onClick={onClick}
      className={classNames(
        "px-4 py-2 text-sm font-semibold rounded-full transition-all duration-200 shadow-md",
        getTagColor(tag.name),
        selected ? "ring-2 ring-offset-1 ring-black" : "hover:scale-105"
      )}
    >
      {tag.name} <span className="text-xs opacity-70">({tag.quizCount})</span>
    </button>
  );
};

export default Tag;
