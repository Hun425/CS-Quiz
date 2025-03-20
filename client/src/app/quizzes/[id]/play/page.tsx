"use client";

import { useRouter, useParams } from "next/navigation";
import { useState, useEffect } from "react";
import { useAuthStore } from "@/store/authStore";
import { useGetPlayableQuiz } from "@/lib/api/quiz/useGetPlayableQuiz";
import { format } from "date-fns";
import { QuizResponse, QuizType, QuizDifficultyType } from "@/lib/types/quiz";
import { QuestionType } from "@/lib/types/question";
import Button from "@/app/_components/Button";

// ✅ 더미 데이터 (실제 API가 실패하면 이걸 사용)
const dummyQuiz: QuizResponse = {
  id: 1,
  title: "더미 퀴즈",
  description: "이것은 테스트용 더미 퀴즈입니다.",
  quizType: QuizType.DAILY, // ✅ ENUM 적용
  difficultyLevel: QuizDifficultyType.INTERMEDIATE, // ✅ ENUM 적용
  timeLimit: 10,
  questionCount: 3,
  tags: [
    {
      id: 1,
      name: "Java",
      description: "Java 프로그래밍 언어 관련 태그",
      quizCount: 15,
      synonyms: ["자바", "Java SE"],
    },
    {
      id: 2,
      name: "Spring Boot",
      description: "Spring Boot 관련 태그",
      quizCount: 10,
      synonyms: ["스프링 부트", "Spring"],
    },
    {
      id: 3,
      name: "Database",
      description: "데이터베이스 관련 문제",
      quizCount: 20,
      synonyms: ["DB", "SQL", "RDBMS"],
    },
  ], // ✅ `TagResponse[]` 형식에 맞게 추가 완료
  creator: {
    id: 1,
    username: "테스트 유저",
    profileImage: null,
    level: 5,
    joinedAt: "2024-01-01T00:00:00Z",
  },
  statistics: {
    totalAttempts: 100,
    averageScore: 75,
    completionRate: 80,
    averageTimeSeconds: 120,
    difficultyDistribution: {
      [QuizDifficultyType.BEGINNER]: 10,
      [QuizDifficultyType.INTERMEDIATE]: 5,
      [QuizDifficultyType.ADVANCED]: 3,
    },
  },
  createdAt: "2024-03-15T12:00:00Z",
  questions: [
    {
      id: 101,
      questionType: QuestionType.MULTIPLE_CHOICE,
      questionText: "다음 Java 코드의 출력 결과는 무엇인가?",
      codeSnippet: `
        public class Main {
          public static void main(String[] args) {
            System.out.println(2 + "3");
          }
        }
      `,
      options: ["5", "23", "컴파일 오류", "예외 발생"],
      explanation: `"23"이 출력됩니다. 숫자 2가 문자열 "3"과 결합되어 문자열 "23"이 됩니다.`,
      points: 10,
      difficultyLevel: QuizDifficultyType.INTERMEDIATE,
      timeLimitSeconds: 30,
    },
    {
      id: 102,
      questionType: QuestionType.MULTIPLE_CHOICE,
      questionText: "SQL에서 데이터를 조회하는 기본 명령어는?",
      options: ["INSERT", "UPDATE", "DELETE", "SELECT"],
      explanation: `"SELECT" 명령어는 데이터베이스에서 데이터를 조회할 때 사용됩니다.`,
      points: 10,
      difficultyLevel: QuizDifficultyType.BEGINNER,
      timeLimitSeconds: 30,
    },
    {
      id: 103,
      questionType: QuestionType.MULTIPLE_CHOICE,
      questionText: "다음 다이어그램은 어떤 디자인 패턴을 나타내는가?",
      diagramData: "https://example.com/design-pattern-diagram.png",
      options: ["싱글톤 패턴", "팩토리 패턴", "옵저버 패턴", "데코레이터 패턴"],
      explanation: `"팩토리 패턴"은 객체 생성을 캡슐화하여 클라이언트 코드가 직접 객체 생성을 하지 않도록 합니다.`,
      points: 10,
      difficultyLevel: QuizDifficultyType.ADVANCED,
      timeLimitSeconds: 30,
    },
  ],
};

const QuizPlayPage: React.FC = () => {
  const router = useRouter();
  const quizId = useParams().id;
  const { isAuthenticated } = useAuthStore();
  const {
    isLoading,
    error,
    data: quizData,
  } = useGetPlayableQuiz(Number(quizId));

  // ✅ API 데이터가 없으면 더미 데이터 사용
  const quiz = quizData || dummyQuiz;

  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [answers, setAnswers] = useState<Record<number, string>>({});
  const [timeLeft, setTimeLeft] = useState(quiz?.timeLimit * 60 || 0);
  const [quizStarted, setQuizStarted] = useState(false);
  const [quizCompleted, setQuizCompleted] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [sidebarOpen, setSidebarOpen] = useState(false);

  useEffect(() => {
    if (!isAuthenticated) {
      router.push(`/login?redirect=/quizzes/${quizId}/play`);
    }
  }, [isAuthenticated, router, quizId]);

  useEffect(() => {
    if (!quizStarted || quizCompleted || timeLeft <= 0) return;

    const timer = setInterval(() => {
      setTimeLeft((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          handleSubmitQuiz();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [quizStarted, quizCompleted, timeLeft]);

  const handleStartQuiz = () => {
    setQuizStarted(true);
  };

  const handleAnswerSelect = (questionId: number, answer: string) => {
    setAnswers((prev) => ({
      ...prev,
      [questionId]: answer,
    }));
  };

  const handleSubmitQuiz = () => {
    if (submitting) return;
    setSubmitting(true);
    setQuizCompleted(true);
    router.push(`/quizzes/${quizId}/results`);
  };

  const formatTimeLeft = () => {
    const minutes = Math.floor(timeLeft / 60);
    const seconds = timeLeft % 60;
    return `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(
      2,
      "0"
    )}`;
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center py-8 text-lg min-h-screen">
        🔄 퀴즈 정보를 불러오는 중...
      </div>
    );
  }

  return (
    <div className="flex items-center justify-center min-h-screen bg-background">
      <div className="max-w-3xl w-full mx-auto p-8 border-border border shadow-xl rounded-lg bg-card">
        {!quizStarted ? (
          // ✅ 퀴즈 시작 전 화면
          <div className="flex flex-col items-center text-center space-y-6">
            <h1 className="text-4xl font-bold text-primary">{quiz.title}</h1>
            <p className="text-lg text-gray-700">{quiz.description}</p>

            {/* ✅ 태그 목록 */}
            <div className="flex flex-wrap justify-center gap-2">
              {quiz.tags.map((tag) => (
                <span
                  key={tag.id}
                  className="px-3 py-1 bg-gray-200 text-gray-700 text-sm rounded-full"
                >
                  {tag.name}
                </span>
              ))}
            </div>

            {/* ✅ 퀴즈 정보 (난이도 + 문제 개수) */}
            <p className="text-gray-600 text-sm">
              ⏳ {timeLeft}초 동안 {quiz.questionCount}문제를 풀어보세요.
            </p>
            <p className="text-gray-500 text-sm font-medium">
              {quiz.difficultyLevel} 난이도 | {quiz.questions.length} 문제
            </p>

            {/* ✅ 퀴즈 시작 버튼 */}
            <button
              onClick={handleStartQuiz}
              className="mt-4 w-full max-w-xs bg-primary hover:bg-primary-hover text-white py-3 rounded-lg font-semibold text-lg transition-all"
            >
              🚀 퀴즈 시작하기
            </button>
          </div>
        ) : (
          <div className="flex flex-col lg:flex-row gap-6">
            {/* ✅ 문제 카드 (퀴즈 진행 화면) */}
            <div className="flex-1 bg-card p-6 rounded-lg shadow-lg space-y-6 w-full max-w-lg mx-auto">
              {/* ✅ 퀴즈 상단 (타이머 + 제목) */}
              <div className="flex justify-between items-center bg-background p-4 rounded-lg shadow">
                <h2 className="text-lg lg:text-xl font-semibold text-primary">
                  {quiz.title}
                </h2>
                <span className="px-3 py-2 text-sm lg:text-lg bg-red-500 text-white rounded-md font-bold">
                  ⏳ {formatTimeLeft()}
                </span>
              </div>

              {/* ✅ 문제 출력 */}
              <div className="space-y-4">
                <h3 className="text-md lg:text-lg font-bold text-foreground">
                  문제 {currentQuestionIndex + 1} / {quiz.questions.length}
                </h3>
                <p className="text-foreground text-sm lg:text-base">
                  {quiz.questions[currentQuestionIndex].questionText}
                </p>

                {/* ✅ 선택지 목록 */}
                <div className="space-y-2">
                  {quiz.questions[currentQuestionIndex].options.map(
                    (option, index) => (
                      <button
                        key={index}
                        className={`block w-full text-left px-3 py-2 text-sm lg:text-base rounded-lg border transition-all ${
                          answers[quiz.questions[currentQuestionIndex].id] ===
                          option
                            ? "bg-blue-500 text-white border-blue-500"
                            : "bg-background border-gray-300 hover:bg-gray-200"
                        }`}
                        onClick={() =>
                          handleAnswerSelect(
                            quiz.questions[currentQuestionIndex].id,
                            option
                          )
                        }
                      >
                        {option}
                      </button>
                    )
                  )}
                </div>
              </div>

              {/* ✅ 네비게이션 버튼 */}
              <div className="flex justify-between">
                <Button
                  disabled={currentQuestionIndex === 0}
                  variant="secondary"
                  onClick={() => setCurrentQuestionIndex((prev) => prev - 1)}
                >
                  ⬅ 이전 문제
                </Button>
                {currentQuestionIndex < quiz.questions.length - 1 ? (
                  <Button
                    className="px-3 py-2 text-sm lg:text-base bg-primary text-white rounded-lg hover:bg-primary-hover transition-all"
                    onClick={() => setCurrentQuestionIndex((prev) => prev + 1)}
                  >
                    다음 문제 ➡
                  </Button>
                ) : (
                  <Button variant="primary" onClick={handleSubmitQuiz}>
                    ✅ 퀴즈 제출하기
                  </Button>
                )}
              </div>
            </div>

            {/* ✅ 문제 진행 상황 사이드바 */}
            <div className="hidden lg:block w-60 bg-card p-4 rounded-lg shadow-lg">
              <h3 className="text-lg font-semibold text-primary mb-3">
                📌 진행 상황
              </h3>
              <div className="space-y-2">
                {quiz.questions.map((_, index) => (
                  <button
                    key={index}
                    className={`w-full px-4 py-2 text-sm rounded-lg text-left transition-all ${
                      index === currentQuestionIndex
                        ? "bg-primary text-white"
                        : "bg-background hover:bg-gray-200"
                    }`}
                    onClick={() => setCurrentQuestionIndex(index)}
                  >
                    문제 {index + 1}
                  </button>
                ))}
              </div>
            </div>

            {/* ✅ 모바일용 진행 상황 토글 버튼 */}
            <button
              onClick={() => setSidebarOpen(true)}
              className="lg:hidden fixed bottom-4 right-4 bg-primary text-white px-4 py-2 rounded-full shadow-lg"
            >
              📌 진행 상황
            </button>

            {/* ✅ 모바일용 사이드바 (오버레이) */}
            {sidebarOpen && (
              <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-end">
                <div className="w-64 bg-card p-4 shadow-lg h-full">
                  <div className="flex justify-between items-center mb-4">
                    <h3 className="text-lg font-semibold text-primary">
                      📌 진행 상황
                    </h3>
                    <button
                      onClick={() => setSidebarOpen(false)}
                      className="text-gray-600 text-xl"
                    >
                      ✖
                    </button>
                  </div>
                  <div className="space-y-2">
                    {quiz.questions.map((_, index) => (
                      <button
                        key={index}
                        className={`w-full px-4 py-2 text-sm rounded-lg text-left transition-all ${
                          index === currentQuestionIndex
                            ? "bg-primary text-white"
                            : "bg-background hover:bg-gray-200"
                        }`}
                        onClick={() => {
                          setCurrentQuestionIndex(index);
                          setSidebarOpen(false);
                        }}
                      >
                        문제 {index + 1}
                      </button>
                    ))}
                  </div>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default QuizPlayPage;
