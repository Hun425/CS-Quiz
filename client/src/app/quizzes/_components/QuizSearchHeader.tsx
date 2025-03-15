"use client";

import { useState } from "react";
import { Search } from "lucide-react";
import { QuizDifficultyType, QuizType } from "@/lib/types/quiz";
import Button from "@/app/_components/Button";
import TagSelector from "./TagSelector";

interface Props {
  onSearch: (params: {
    title?: string;
    difficultyLevel?: QuizDifficultyType | "";
    quizType?: QuizType | "";
    tagIds?: number[];
  }) => void;
}

const QuizSearchHeader: React.FC<Props> = ({ onSearch }) => {
  // 🔹 검색 필터 상태 관리
  const [title, setTitle] = useState("");
  const [selectedDifficulty, setSelectedDifficulty] = useState<
    QuizDifficultyType | ""
  >("");
  const [selectedCategory, setSelectedCategory] = useState<QuizType | "">("");
  const [selectedTagIds, setSelectedTagIds] = useState<number[]>([]);

  return (
    <div className="bg-card border border-border p-6 rounded-lg shadow-md mb-6">
      <h2 className="text-lg font-semibold mb-4">🔎 문제 검색</h2>

      {/* 🔹 검색어 입력 */}
      <div className="relative w-full mb-4">
        <input
          type="text"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="문제 제목 검색"
          className="w-full p-3 border border-border rounded-md pl-10 bg-background text-foreground"
        />
        <Search className="absolute left-3 top-3 w-5 h-5 text-muted" />
      </div>

      {/* 🔹 필터 선택 */}
      <div className="grid grid-cols-3 gap-4 mb-4">
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
          <option value="">기출문제 모음</option>
          <option value={QuizType.DAILY}>데일리</option>
          <option value={QuizType.TOPIC_BASED}>주제 기반</option>
          <option value={QuizType.TAG_BASED}>태그 기반</option>
          <option value={QuizType.CUSTOM}>사용자 지정</option>
        </select>
      </div>

      <TagSelector
        selectedTagIds={selectedTagIds}
        onChange={setSelectedTagIds}
      />

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
