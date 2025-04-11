"use client";

import { useState } from "react";
import { useCreateQuiz } from "@/lib/api/quiz/useCreateQuiz";
import {
  QuizCreateRequest,
  QuizDifficultyType,
  QuizType,
} from "@/lib/types/quiz";
import Button from "@/app/_components/Button";
import QuizList from "./QuizList";
import TagSelect from "./TagSelect";
import { TagResponse } from "@/lib/types/tag";

const initialQuiz: QuizCreateRequest = {
  title: "",
  description: "",
  quizType: QuizType.DAILY,
  difficultyLevel: QuizDifficultyType.BEGINNER,
  timeLimit: 0,
  tagIds: [],
  questions: [],
};

const QuizCreateForm = ({
  initialTags,
}: {
  initialTags: { id: number; name: string }[];
}) => {
  const [quiz, setQuiz] = useState<QuizCreateRequest>(initialQuiz);
  const { mutate: createQuiz, isPending } = useCreateQuiz();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    createQuiz(quiz);
  };

  return (
    <form
      onSubmit={handleSubmit}
      className="space-y-6 border rounded p-6 mt-10"
    >
      <h2 className="text-xl font-bold text-primary">퀴즈 생성하기</h2>

      <input
        type="text"
        value={quiz.title}
        onChange={(e) => setQuiz({ ...quiz, title: e.target.value })}
        placeholder="퀴즈 제목"
        aria-label="퀴즈 제목 입력"
        className="w-full border p-2 rounded-md"
      />

      <textarea
        value={quiz.description}
        onChange={(e) => setQuiz({ ...quiz, description: e.target.value })}
        placeholder="퀴즈 설명"
        aria-label="퀴즈 설명 입력"
        className="w-full border p-2 rounded-md"
      />

      <select
        value={quiz.quizType}
        onChange={(e) =>
          setQuiz({
            ...quiz,
            quizType: e.target.value as QuizCreateRequest["quizType"],
          })
        }
        aria-label="퀴즈 유형 선택"
        className="w-full border p-2 rounded-md"
      >
        <option value="DAILY">데일리 퀴즈</option>
        <option value="TAGGED">태그 퀴즈</option>
      </select>

      <select
        value={quiz.difficultyLevel}
        onChange={(e) =>
          setQuiz({
            ...quiz,
            difficultyLevel: e.target
              .value as QuizCreateRequest["difficultyLevel"],
          })
        }
        aria-label="퀴즈 난이도 선택"
        className="w-full border p-2 rounded-md"
      >
        <option value="BEGINNER">초급</option>
        <option value="INTERMEDIATE">중급</option>
        <option value="ADVANCED">고급</option>
      </select>

      <input
        type="number"
        value={quiz.timeLimit}
        onChange={(e) =>
          setQuiz({ ...quiz, timeLimit: Number(e.target.value) })
        }
        aria-label="제한 시간 (분)"
        className="w-full border p-2 rounded-md"
        placeholder="제한 시간 (분)"
        min={0}
      />

      <TagSelect
        selectedTagIds={quiz.tagIds}
        onChange={(tagIds: TagResponse[]) => setQuiz({ ...quiz, tagIds })}
        allTags={initialTags}
      />

      <div className="p-4 border rounded-md">
        <p className="text-lg font-semibold mb-2">문제 목록 (미구현)</p>
        <p className="text-sm text-muted-foreground">곧 구현될 예정입니다.</p>
      </div>

      <Button type="submit" aria-label="퀴즈 생성하기" disabled={isPending}>
        {isPending ? "생성 중..." : "퀴즈 생성하기"}
      </Button>
    </form>
  );
};

export default QuizCreateForm;
