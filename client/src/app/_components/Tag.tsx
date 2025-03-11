"use client";

import React from "react";
import classNames from "classnames";
import { TagResponse } from "@/types/api";

interface TagProps {
  tag: TagResponse;
  onClick?: () => void;
  selected?: boolean;
}

// ✅ 태그 유형별 색상 지정
const getTagColor = (name: string) => {
  const colorMap: Record<string, string> = {
    JavaScript: "bg-yellow-400 text-black",
    Python: "bg-blue-500 text-white",
    "C++": "bg-gray-700 text-white",
    HTML: "bg-orange-500 text-white",
    CSS: "bg-blue-600 text-white",
    React: "bg-sky-400 text-white",
    MySQL: "bg-green-600 text-white",
    MongoDB: "bg-gray-500 text-white",
    PostgreSQL: "bg-blue-700 text-white",
    Linux: "bg-red-600 text-white",
    Windows: "bg-blue-600 text-white",
    알고리즘: "bg-purple-500 text-white",
    자료구조: "bg-indigo-500 text-white",
    DAILY: "bg-gray-300 text-black",
    TAG_BASED: "bg-gray-300 text-black",
    TOPIC_BASED: "bg-gray-300 text-black",
    CUSTOM: "bg-gray-300 text-black",
    BEGINNER: "bg-green-400 text-black",
    INTERMEDIATE: "bg-orange-400 text-black",
    ADVANCED: "bg-red-500 text-white",
  };

  return colorMap[name] || "bg-gray-200 text-black";
};

const Tag: React.FC<TagProps> = ({ tag, onClick, selected = false }) => {
  return (
    <button
      onClick={onClick}
      className={classNames(
        "px-3 py-1 text-sm rounded-md transition-all",
        getTagColor(tag.name),
        selected ? "ring-2 ring-offset-1" : "hover:opacity-80"
      )}
    >
      {tag.name} ({tag.quizCount})
    </button>
  );
};

export default Tag;
