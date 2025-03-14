import {
  QuizDetailResponse,
  QuizDifficultyType,
  QuizType,
} from "@/lib/types/quiz";

export const mockQuizDetail: QuizDetailResponse = {
  id: 1,
  title: "JavaScript 기초 퀴즈",
  description: "자바스크립트 기초 문법을 테스트하는 퀴즈입니다.",
  quizType: QuizType.TAG_BASED,
  difficultyLevel: QuizDifficultyType.BEGINNER,
  timeLimit: 15,
  questionCount: 10,
  tags: [
    {
      id: 1,
      name: "자바스크립트",
      description: "JS 관련 문제",
      quizCount: 120,
      synonyms: ["JS", "JavaScript"],
    },
    {
      id: 2,
      name: "프로그래밍",
      description: "프로그래밍 관련",
      quizCount: 80,
      synonyms: ["코딩", "Coding"],
    },
  ],
  creator: {
    id: 100,
    username: "dev_master",
    profileImage: null,
    level: 5,
    joinedAt: "2022-05-14T10:30:00Z",
  },
  statistics: {
    totalAttempts: 350,
    averageScore: 78.5,
    completionRate: 92.3,
    averageTimeSeconds: 600,
    difficultyDistribution: {
      EASY: 40,
      MEDIUM: 35,
      HARD: 25,
    },
    questionStatistics: [
      {
        questionId: 1,
        correctAnswers: 200,
        totalAttempts: 250,
        correctRate: 80,
        averageTimeSeconds: 20,
      },
      {
        questionId: 2,
        correctAnswers: 150,
        totalAttempts: 250,
        correctRate: 60,
        averageTimeSeconds: 30,
      },
    ],
  },
  createdAt: "2023-08-01T14:00:00Z",
};
