"use client";

import { useState, useEffect } from "react";
import { Search, RefreshCw } from "lucide-react";
import { QuizDifficultyType, QuizType } from "@/lib/types/quiz";
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
  const [title, setTitle] = useState("");
  const [allTags, setTags] = useState<TagResponse[]>([]);
  const [selectedDifficulty, setSelectedDifficulty] = useState<
    QuizDifficultyType | ""
  >("");
  const [selectedCategory, setSelectedCategory] = useState<QuizType | "">("");
  const [selectedTagIds, setSelectedTagIds] = useState<number[]>([]);

  const { data: tags } = useGetAllTags();

  useEffect(() => {
    if (tags?.data) setTags(tags.data);
  }, [tags?.data]);

  const handleTagToggle = (tagId: number) => {
    setSelectedTagIds((prev) =>
      prev.includes(tagId)
        ? prev.filter((id) => id !== tagId)
        : [...prev, tagId]
    );
  };

  const handleReset = () => {
    setTitle("");
    setSelectedDifficulty("");
    setSelectedCategory("");
    setSelectedTagIds([]);
    onSearch({ title: "", difficultyLevel: "", quizType: "", tagIds: [] });
  };

  return (
    <div className="bg-background shadow-sm p-6 rounded-lg mb-6">
      {/* 제목 검색 */}
      <div className="relative mb-4">
        <input
          type="search"
          aria-label="퀴즈 제목 검색"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="제목을 검색하세요"
          className="w-full p-3 border border-border rounded-md pl-10 bg-background text-foreground focus:border-primary focus:ring-2 focus:ring-primary focus:outline-none transition-all"
        />
        <Search className="absolute left-3 top-3 w-5 h-5 text-muted" />
      </div>

      {/* 필터 선택 */}
      <div className="space-y-6">
        <div className="grid grid-cols-2 gap-4">
          <select
            aria-label="난이도 선택"
            value={selectedDifficulty}
            onChange={(e) =>
              setSelectedDifficulty(e.target.value as QuizDifficultyType)
            }
            className="w-full p-3 border border-border rounded-md bg-background text-sm text-foreground focus:border-primary"
          >
            <option value="">난이도 선택</option>
            <option value={QuizDifficultyType.BEGINNER}>Lv. 1 입문</option>
            <option value={QuizDifficultyType.INTERMEDIATE}>Lv. 2 중급</option>
            <option value={QuizDifficultyType.ADVANCED}>Lv. 3 고급</option>
          </select>

          <select
            aria-label="퀴즈 유형 선택"
            value={selectedCategory}
            onChange={(e) => setSelectedCategory(e.target.value as QuizType)}
            className="w-full p-3 border border-border rounded-md bg-background text-sm text-foreground focus:border-primary"
          >
            <option value="">퀴즈 유형 선택</option>
            {Object.entries(QUIZ_TYPE_LABEL).map(([key, label]) => (
              <option key={key} value={key}>
                {label}
              </option>
            ))}
          </select>
        </div>

        {/* 태그 선택 */}
        <div>
          <div className="flex items-center mb-2">
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
      </div>

      {/* 버튼 영역 */}
      <div className="flex justify-center gap-4 mt-6">
        <Button
          variant="primary"
          className="text-white px-6"
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
        <Button
          variant="secondary"
          size="medium"
          onClick={handleReset}
          className="flex gap-1 justify-center items-center"
        >
          <RefreshCw size={16} /> 초기화
        </Button>
      </div>
    </div>
  );
};

export default QuizSearchHeader;
