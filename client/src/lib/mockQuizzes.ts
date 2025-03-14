import {
  QuizSummaryResponse,
  QuizType,
  QuizDifficultyType,
} from "@/lib/types/quiz";
import { TagResponse } from "@/lib/types/tag";

// ✅ 더미 태그 데이터 (TagResponse 형식)
const mockTags: TagResponse[] = [
  {
    id: 101,
    name: "자바스크립트",
    description: "웹 개발에서 가장 널리 사용되는 언어",
    quizCount: 120,
    synonyms: ["JS", "JavaScript"],
  },
  {
    id: 102,
    name: "리액트",
    description: "UI 개발을 위한 인기 라이브러리",
    quizCount: 90,
    synonyms: ["React.js", "ReactJS"],
  },
  {
    id: 103,
    name: "타입스크립트",
    description: "자바스크립트에 정적 타입을 추가한 언어",
    quizCount: 80,
    synonyms: ["TS", "TypeScript"],
  },
  {
    id: 104,
    name: "웹 개발",
    description: "HTML, CSS, JS 등으로 이루어진 프론트엔드 및 백엔드 기술",
    quizCount: 150,
    synonyms: ["Web Dev", "Web Programming"],
  },
  {
    id: 105,
    name: "자료구조",
    description: "컴퓨터 과학의 핵심 개념으로 데이터를 효율적으로 저장 및 관리",
    quizCount: 200,
    synonyms: ["Data Structure", "DS"],
  },
  {
    id: 106,
    name: "알고리즘",
    description: "효율적인 문제 해결을 위한 연산 및 논리 과정",
    quizCount: 250,
    synonyms: ["Algorithm", "Algo"],
  },
  {
    id: 107,
    name: "보안",
    description: "정보 보호와 시스템 안전을 위한 기술",
    quizCount: 60,
    synonyms: ["Security", "Cyber Security"],
  },
  {
    id: 108,
    name: "네트워크",
    description: "컴퓨터 간 통신을 위한 기술 및 프로토콜",
    quizCount: 100,
    synonyms: ["Networking", "Network Systems"],
  },
  {
    id: 109,
    name: "운영체제",
    description: "컴퓨터 시스템을 관리하는 소프트웨어",
    quizCount: 140,
    synonyms: ["OS", "Operating System"],
  },
  {
    id: 110,
    name: "시스템 설계",
    description: "대규모 소프트웨어 개발을 위한 설계 원칙",
    quizCount: 70,
    synonyms: ["System Design", "Architecture"],
  },
];

// ✅ 퀴즈 데이터 (QuizSummaryResponse 형식)
export const mockQuizzes: QuizSummaryResponse[] = [
  {
    id: 1,
    title: "리액트 기초 퀴즈",
    quizType: QuizType.TOPIC_BASED,
    difficultyLevel: QuizDifficultyType.BEGINNER,
    questionCount: 10,
    attemptCount: 120,
    avgScore: 85.5,
    tags: [mockTags[0], mockTags[1]], // 자바스크립트, 리액트
    createdAt: "2024-03-10T12:00:00Z",
  },
  {
    id: 2,
    title: "타입스크립트 중급 퀴즈",
    quizType: QuizType.TAG_BASED,
    difficultyLevel: QuizDifficultyType.INTERMEDIATE,
    questionCount: 15,
    attemptCount: 95,
    avgScore: 78.2,
    tags: [mockTags[2], mockTags[3]], // 타입스크립트, 웹 개발
    createdAt: "2024-03-12T15:30:00Z",
  },
  {
    id: 3,
    title: "CS 면접 대비 퀴즈",
    quizType: QuizType.DAILY,
    difficultyLevel: QuizDifficultyType.ADVANCED,
    questionCount: 20,
    attemptCount: 150,
    avgScore: 90.1,
    tags: [mockTags[4], mockTags[5]], // 자료구조, 알고리즘
    createdAt: "2024-03-14T10:45:00Z",
  },
  {
    id: 4,
    title: "웹 보안 기본 퀴즈",
    quizType: QuizType.CUSTOM,
    difficultyLevel: QuizDifficultyType.BEGINNER,
    questionCount: 12,
    attemptCount: 80,
    avgScore: 82.4,
    tags: [mockTags[6], mockTags[7]], // 보안, 네트워크
    createdAt: "2024-03-15T09:20:00Z",
  },
  {
    id: 5,
    title: "운영체제 심화 퀴즈",
    quizType: QuizType.TAG_BASED,
    difficultyLevel: QuizDifficultyType.ADVANCED,
    questionCount: 18,
    attemptCount: 60,
    avgScore: 75.3,
    tags: [mockTags[8], mockTags[9]], // 운영체제, 시스템 설계
    createdAt: "2024-03-16T14:00:00Z",
  },
];
