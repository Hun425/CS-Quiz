"use client";

import { useState, useEffect } from "react";
import { Search } from "lucide-react";
import { QuizDifficultyType, QuizType } from "@/lib/types/quiz";
import { RefreshCw } from "lucide-react";
import Button from "@/app/_components/Button";
import Tag from "./Tag";

import { useGetAllTags } from "@/lib/api/tag/useGetTags";
import { TagResponse } from "@/lib/types/tag";

interface Props {
  onSearch: (params: {
    title?: string;
    difficultyLevel?: QuizDifficultyType | "";
    quizType?: QuizType | "";
    tagIds?: number[];
  }) => void;
}

const QUIZ_TYPE_LABEL: Record<QuizType, string> = {
  REGULAR: "일반 퀴즈",
  DAILY: "데일리 퀴즈",
  WEEKLY: "위클리 퀴즈",
  SPECIAL: "스페셜 퀴즈",
  BATTLE: "배틀 퀴즈",
};

const QuizSearchHeader: React.FC<Props> = ({ onSearch }) => {
  // 🔹 검색 필터 상태 관리
  const [title, setTitle] = useState("");
  const [allTags, setTags] = useState<TagResponse[]>([]);
  const [selectedDifficulty, setSelectedDifficulty] = useState<
    QuizDifficultyType | ""
  >("");
  const [selectedCategory, setSelectedCategory] = useState<QuizType | "">("");
  const [selectedTagIds, setSelectedTagIds] = useState<number[]>([]);

  // 🔹 태그 목록 가져오기
  const { data: tags } = useGetAllTags();

  useEffect(() => {
    if (tags?.data) {
      setTags(tags.data);
    }
  }, [tags?.data]);

  // 🔹 태그 선택 핸들러
  const handleTagToggle = (tagId: number) => {
    setSelectedTagIds(
      (prev) =>
        prev.includes(tagId)
          ? prev.filter((id) => id !== tagId) // 이미 선택된 태그면 제거
          : [...prev, tagId] // 선택되지 않았다면 추가
    );
  };

  // 🔹 검색 필터 초기화 후 검색 재실행
  const handleReset = () => {
    setTitle("");
    setSelectedDifficulty("");
    setSelectedCategory("");
    setSelectedTagIds([]);

    onSearch({
      title: "",
      difficultyLevel: "",
      quizType: "",
      tagIds: [],
    });
  };

  return (
    <div className="bg-background border border-border p-6 rounded-lg shadow-md mb-6">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-lg font-semibold ">🔎 문제 검색</h2>
        <div
          className="flex items-center justify-between  cursor-pointer"
          onClick={() => handleReset()}
        >
          <span>초기화</span>
          <RefreshCw size={15} className="ml-2" />
        </div>
      </div>
      {/* 🔹 검색어 입력 */}
      <div className="relative w-full mb-4">
        <input
          type="search"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="제목을 검색하세요"
          className="w-full p-3 border border-border rounded-md pl-10 bg-background text-foreground"
        />
        <Search className="absolute left-3 top-3 w-5 h-5 text-muted" />
      </div>

      {/* 🔹 필터 선택 */}
      <div className="grid grid-cols-2 gap-4 mb-4">
        <select
          value={selectedDifficulty}
          onChange={(e) =>
            setSelectedDifficulty(e.target.value as QuizDifficultyType)
          }
          className="w-full p-3 border border-border rounded-md bg-background text-foreground"
        >
          <option value="">난이도 선택</option>
          <option value={QuizDifficultyType.BEGINNER}>Lv. 1 입문</option>
          <option value={QuizDifficultyType.INTERMEDIATE}>Lv. 2 중급</option>
          <option value={QuizDifficultyType.ADVANCED}>Lv. 3 고급</option>
        </select>

        <select
          value={selectedCategory}
          onChange={(e) => setSelectedCategory(e.target.value as QuizType)}
          className="w-full p-3 border border-border rounded-md bg-background text-foreground"
        >
          <option value="">퀴즈 유형 선택</option>
          {Object.entries(QUIZ_TYPE_LABEL).map(([key, label]) => (
            <option key={key} value={key}>
              {label}
            </option>
          ))}
        </select>
      </div>

      {/* 🔹 태그 선택 */}
      <div className="mb-4">
        <div className="flex items-center align-center mb-4">
          <h3 className="text-md font-semibold">
            🏷️ 태그 선택 ({selectedTagIds.length}개 선택됨)
          </h3>
          <Button
            variant="secondary"
            size="small"
            onClick={() => setSelectedTagIds([])}
            className="ml-2"
          >
            태그 초기화
          </Button>
        </div>
        <div className="flex flex-wrap gap-2">
          {allTags.map((tag) => (
            <Tag
              key={tag.id}
              tag={tag}
              isSelected={selectedTagIds.includes(tag.id)}
              onClick={() => handleTagToggle(tag.id)}
              size="small"
            />
          ))}
        </div>
      </div>

      {/* 🔹 검색 버튼 */}
      <div className="flex justify-center mt-4">
        <Button
          variant="primary"
          className="text-white px-8"
          size="medium"
          onClick={() =>
            onSearch({
              title,
              difficultyLevel: selectedDifficulty,
              quizType: selectedCategory,
              tagIds: selectedTagIds,
            })
          }
        >
          검색하기
        </Button>
      </div>
    </div>
  );
};

export default QuizSearchHeader;
