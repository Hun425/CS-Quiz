import type { Meta, StoryObj } from "@storybook/react";
import QuizCard from "@/app/quizzes/_components/QuizCard";
import "@/styles/globals.css";
import {
  QuizSummaryResponse,
  QuizDifficultyType,
  QuizType,
} from "@/lib/types/quiz";
import { TagResponse } from "@/lib/types/tag";

const StoryQuizCard = ({
  id = 1,
  title = "기본 퀴즈 카드",
  difficultyLevel = QuizDifficultyType.BEGINNER,
  quizType = QuizType.DAILY,
  questionCount = 10,
  attemptCount = 0,
  avgScore = 0.0,
  tags = [],
  createdAt = new Date().toISOString(),
}: {
  id?: number;
  title?: string;
  difficultyLevel?: QuizDifficultyType;
  quizType?: QuizType;
  questionCount?: number;
  attemptCount?: number;
  avgScore?: number;
  tags?: TagResponse[];
  createdAt?: string;
}) => {
  const quiz: QuizSummaryResponse = {
    id,
    title,
    difficultyLevel,
    quizType,
    questionCount,
    attemptCount,
    avgScore,
    tags,
    createdAt,
  };

  return <QuizCard quiz={quiz} />;
};

const meta: Meta<typeof StoryQuizCard> = {
  title: "Components/QuizCard",
  component: StoryQuizCard,
  tags: ["autodocs"],
  argTypes: {
    difficultyLevel: {
      control: "select",
      options: ["BEGINNER", "INTERMEDIATE", "ADVANCED"],
      description: "퀴즈 난이도를 선택하세요.",
    },
    quizType: {
      control: "select",
      options: ["REGULAR", "DAILY", "WEEKLY", "SPECIAL", "BATTLE"],
      description: "퀴즈 유형을 선택하세요.",
    },
    questionCount: {
      control: "number",
      description: "퀴즈 문제 개수를 설정하세요.",
    },
    title: {
      control: "text",
      description: "퀴즈 제목을 입력하세요.",
    },
    attemptCount: {
      control: "number",
      description: "시도 횟수를 설정하세요.",
    },
    avgScore: {
      control: "number",
      description: "평균 점수를 설정하세요.",
    },
  },
};

export default meta;

type Story = StoryObj<typeof StoryQuizCard>;

/** ✅ 기본 QuizCard */
export const Default: Story = {
  args: {
    title: "기본 퀴즈 카드",
    difficultyLevel: QuizDifficultyType.BEGINNER,
    quizType: QuizType.DAILY,
    questionCount: 10,
    attemptCount: 0,
    avgScore: 0.0,
  },
};

/** ✅ 중급 난이도 QuizCard */
export const Intermediate: Story = {
  args: {
    title: "중급 퀴즈",
    difficultyLevel: QuizDifficultyType.INTERMEDIATE,
    quizType: QuizType.REGULAR,
    questionCount: 15,
    attemptCount: 12,
    avgScore: 75.5,
  },
};

/** ✅ 고급 난이도 QuizCard */
export const Advanced: Story = {
  args: {
    title: "고급 퀴즈",
    difficultyLevel: QuizDifficultyType.ADVANCED,
    quizType: QuizType.SPECIAL,
    questionCount: 20,
    attemptCount: 5,
    avgScore: 62.8,
  },
};

/** ✅ 커스텀 퀴즈 QuizCard */
export const CustomQuiz: Story = {
  args: {
    title: "커스텀 퀴즈",
    difficultyLevel: QuizDifficultyType.BEGINNER,
    quizType: QuizType.WEEKLY,
    questionCount: 5,
    attemptCount: 3,
    avgScore: 90.0,
  },
};
