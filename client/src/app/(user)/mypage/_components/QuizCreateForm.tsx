"use client";

import { useState } from "react";
import { useCreateQuiz } from "@/lib/api/quiz/useCreateQuiz";
import {
  QuizCreateRequest,
  QuizDifficultyType,
  QuizType,
} from "@/lib/types/quiz";
import Button from "@/app/_components/Button";
import { TagResponse } from "@/lib/types/tag";
import TagSelector from "./TagSelector";

const initialQuiz: QuizCreateRequest = {
  title: "",
  description: "",
  quizType: QuizType.DAILY,
  difficultyLevel: QuizDifficultyType.BEGINNER,
  timeLimit: 0,
  tagIds: [],
  questions: [],
};

const DIFFICULTY_LABEL: Record<QuizDifficultyType, string> = {
  BEGINNER: "초급",
  INTERMEDIATE: "중급",
  ADVANCED: "고급",
};
const QUIZ_TYPE_LABEL: Record<QuizType, string> = {
  REGULAR: "일반 퀴즈",
  DAILY: "데일리 퀴즈",
  WEEKLY: "위클리 퀴즈",
  SPECIAL: "스페셜 퀴즈",
  BATTLE: "배틀 퀴즈",
};
const QuizCreateForm = ({ initialTags }: { initialTags: TagResponse[] }) => {
  const [quiz, setQuiz] = useState<QuizCreateRequest>(initialQuiz);
  const { mutate: createQuiz, isPending } = useCreateQuiz();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    createQuiz(quiz);
  };

  return (
    <form
      onSubmit={handleSubmit}
      className="space-y-6 bg-sub-background border border-border rounded-xl p-8 shadow-md"
    >
      <h2 className="text-2xl font-bold text-primary mb-4">📝 퀴즈 생성하기</h2>

      <div className="space-y-2">
        <label className="block text-sm text-muted-foreground">퀴즈 제목</label>
        <input
          type="text"
          value={quiz.title}
          onChange={(e) => setQuiz({ ...quiz, title: e.target.value })}
          placeholder="예: 자료구조 기초 퀴즈"
          className="w-full border border-border bg-background text-foreground p-2 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
        />
      </div>

      <div className="space-y-2">
        <label className="block text-sm text-muted-foreground">퀴즈 설명</label>
        <textarea
          value={quiz.description}
          onChange={(e) => setQuiz({ ...quiz, description: e.target.value })}
          placeholder="이 퀴즈에 대한 간단한 설명을 작성해주세요."
          className="w-full border border-border bg-background text-foreground p-2 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
        />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="space-y-2">
          <label className="block text-sm text-muted-foreground">
            퀴즈 유형
          </label>
          <select
            value={quiz.quizType}
            onChange={(e) =>
              setQuiz({
                ...quiz,
                quizType: e.target.value as QuizType,
              })
            }
            className="w-full border border-border bg-background text-foreground p-2 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
          >
            {Object.values(QuizType).map((type) => (
              <option key={type} value={type}>
                {QUIZ_TYPE_LABEL[type]}
              </option>
            ))}
          </select>
        </div>

        <div className="space-y-2">
          <label className="block text-sm text-muted-foreground">난이도</label>
          <select
            value={quiz.difficultyLevel}
            onChange={(e) =>
              setQuiz({
                ...quiz,
                difficultyLevel: e.target.value as QuizDifficultyType,
              })
            }
            className="w-full border border-border bg-background text-foreground p-2 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
          >
            {Object.values(QuizDifficultyType).map((level) => (
              <option key={level} value={level}>
                {DIFFICULTY_LABEL[level]}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="space-y-2">
        <label className="block text-sm text-muted-foreground">
          제한 시간 (초)
        </label>
        <input
          type="number"
          value={quiz.timeLimit}
          onChange={(e) =>
            setQuiz({ ...quiz, timeLimit: Number(e.target.value) })
          }
          placeholder="예: 10"
          min={0}
          className="w-full border border-border bg-background text-foreground p-2 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
        />
      </div>

      <div className="space-y-2">
        <label className="block text-sm text-muted-foreground">태그 선택</label>
        <TagSelector
          allTags={initialTags}
          selectedTagIds={quiz.tagIds}
          onChange={(tagIds) => setQuiz({ ...quiz, tagIds })}
        />
      </div>

      <div className="flex justify-end">
        <Button type="submit" disabled={isPending}>
          {isPending ? "생성 중..." : "퀴즈 생성하기"}
        </Button>
      </div>
    </form>
  );
};

export default QuizCreateForm;
