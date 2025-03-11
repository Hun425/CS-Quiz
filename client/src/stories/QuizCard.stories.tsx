import type { Meta, StoryObj } from "@storybook/react";
import QuizCard from "@/app/quizzes/_components/QuizCard";
import "@/styles/globals.css";
import { QuizSummaryResponse } from "@/lib/types/api";
import { TagResponse } from "@/lib/types/api";

type DifficultyLevel = "BEGINNER" | "INTERMEDIATE" | "ADVANCED";
type QuizType = "DAILY" | "TAG_BASED" | "TOPIC_BASED" | "CUSTOM";

const StoryQuizCard = ({
  id = 1,
  title = "기본 퀴즈 카드",
  difficultyLevel = "BEGINNER" as DifficultyLevel,
  quizType = "DAILY" as QuizType,
  questionCount = 10,
  attemptCount = 0,
  avgScore = 0.0,
  tags = [],
  createdAt = new Date().toISOString(),
}: {
  id?: number;
  title?: string;
  difficultyLevel?: DifficultyLevel;
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
      options: ["DAILY", "TAG_BASED", "TOPIC_BASED", "CUSTOM"],
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
    difficultyLevel: "BEGINNER",
    quizType: "DAILY",
    questionCount: 10,
    attemptCount: 0,
    avgScore: 0.0,
  },
};

/** ✅ 중급 난이도 QuizCard */
export const Intermediate: Story = {
  args: {
    title: "중급 퀴즈",
    difficultyLevel: "INTERMEDIATE",
    quizType: "TAG_BASED",
    questionCount: 15,
    attemptCount: 12,
    avgScore: 75.5,
  },
};

/** ✅ 고급 난이도 QuizCard */
export const Advanced: Story = {
  args: {
    title: "고급 퀴즈",
    difficultyLevel: "ADVANCED",
    quizType: "TOPIC_BASED",
    questionCount: 20,
    attemptCount: 5,
    avgScore: 62.8,
  },
};

/** ✅ 커스텀 퀴즈 QuizCard */
export const CustomQuiz: Story = {
  args: {
    title: "커스텀 퀴즈",
    difficultyLevel: "BEGINNER",
    quizType: "CUSTOM",
    questionCount: 5,
    attemptCount: 3,
    avgScore: 90.0,
  },
};
