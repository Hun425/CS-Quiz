import { useQuery } from "@tanstack/react-query";
import { QuizSummaryResponse } from "@/lib/types/quiz";
import { QuizType, QuizDifficultyType } from "@/lib/types/quiz";

const dummyQuizzes: QuizSummaryResponse[] = [
  {
    id: 1,
    title: "자료구조 기초 퀴즈",
    quizType: QuizType.REGULAR,
    difficultyLevel: QuizDifficultyType.BEGINNER,
    questionCount: 10,
    attemptCount: 132,
    avgScore: 7.2,
    tags: [
      {
        id: 1,
        name: "자료구조",
        description: "자료구조의 기본 개념과 배열, 연결 리스트 등",
        quizCount: 12,
        synonyms: ["Data Structure", "Structure"],
        parentId: null,
      },
      {
        id: 2,
        name: "기초",
        description: "초급 수준의 문제",
        quizCount: 25,
        synonyms: ["초보", "입문"],
        parentId: null,
      },
    ],
    createdAt: "2024-04-01T10:00:00Z",
  },
  {
    id: 2,
    title: "네트워크 심화 문제집",
    quizType: QuizType.SPECIAL,
    difficultyLevel: QuizDifficultyType.ADVANCED,
    questionCount: 15,
    attemptCount: 88,
    avgScore: 5.4,
    tags: [
      {
        id: 3,
        name: "네트워크",
        description: "OSI 7계층, TCP/IP 등",
        quizCount: 8,
        synonyms: ["Network", "인터넷"],
        parentId: null,
      },
      {
        id: 4,
        name: "TCP/IP",
        description: "전송 계층 및 인터넷 프로토콜",
        quizCount: 4,
        synonyms: ["IP", "Transport Layer"],
        parentId: null,
      },
    ],
    createdAt: "2024-03-20T15:30:00Z",
  },
];

export const useFakeQuizzes = () => {
  return useQuery<QuizSummaryResponse[]>({
    queryKey: ["fakeQuizzes"],
    queryFn: async () => {
      await new Promise((resolve) => setTimeout(resolve, 300)); // simulate delay
      return dummyQuizzes;
    },
    staleTime: Infinity,
  });
};
