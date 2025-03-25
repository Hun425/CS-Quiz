"use client";

import { useSearchParams } from "next/navigation";
import useGetQuizResult from "@/lib/api/quiz/useGetQuizResult";
import Button from "@/app/_components/Button";

const QuizResultsPage: React.FC = () => {
  const params = useSearchParams();
  const attemptId = params.get("attemptId");
  const quizId = params.get("quizId");

  // ✅ 퀴즈 결과 데이터 가져오기
  const { isLoading, data: resultData } = useGetQuizResult(
    Number(attemptId),
    Number(quizId)
  );

  // ✅ attemptId 또는 quizId가 없으면 접근 제한
  if (!attemptId || !quizId) {
    return (
      <div className="flex justify-center items-center py-12 text-xl min-h-screen text-danger">
        ❌ 잘못된 접근입니다.
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="flex justify-center items-center py-12 text-xl min-h-screen">
        🔄 퀴즈 결과를 불러오는 중...
      </div>
    );
  }

  if (!resultData) {
    return (
      <div className="flex justify-center items-center py-12 text-xl min-h-screen text-danger">
        ❌ 퀴즈 결과 데이터를 불러오는 데 실패했습니다.
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center min-h-screen bg-sub-background py-10">
      {/* ✅ 퀴즈 결과 요약 */}
      <div className="bg-background shadow-lg rounded-xl p-6 w-full max-w-2xl">
        <h1 className="text-2xl font-bold text-primary text-center">
          🎉 퀴즈 결과 🎉
        </h1>
        <p className="text-lg text-center text-foreground mt-2">
          {resultData.title}
        </p>

        {/* ✅ 결과 통계 */}
        <div className="grid grid-cols-2 gap-4 mt-6">
          <div className="p-4 bg-gray-100 rounded-lg text-center">
            <p className="text-xl font-semibold text-primary">
              {resultData.correctAnswers} / {resultData.totalQuestions}
            </p>
            <p className="text-sm text-gray-600">정답 개수</p>
          </div>
          <div className="p-4 bg-gray-100 rounded-lg text-center">
            <p className="text-xl font-semibold text-primary">
              {resultData.score} / {resultData.totalPossibleScore}
            </p>
            <p className="text-sm text-gray-600">획득 점수</p>
          </div>
          <div className="p-4 bg-gray-100 rounded-lg text-center">
            <p className="text-xl font-semibold text-primary">
              {resultData.timeTaken}초
            </p>
            <p className="text-sm text-gray-600">소요 시간</p>
          </div>
          <div className="p-4 bg-gray-100 rounded-lg text-center">
            <p className="text-xl font-semibold text-primary">
              +{resultData.experienceGained} XP
            </p>
            <p className="text-sm text-gray-600">획득 경험치</p>
          </div>
        </div>

        {/* ✅ 다시 도전하기 버튼 */}
        <div className="mt-6 flex justify-center">
          <Button
            variant="primary"
            onClick={() => (window.location.href = `/quizzes/${quizId}/play`)}
          >
            🔄 다시 도전하기
          </Button>
        </div>
      </div>

      {/* ✅ 문제별 정답 분석 */}
      <div className="w-full max-w-2xl mt-10">
        <h2 className="text-xl font-bold text-primary mb-4">📋 문제별 분석</h2>
        <div className="space-y-4">
          {resultData.questions.map((question) => (
            <div
              key={question.id}
              className={`p-4 rounded-lg shadow-md transition-all ${
                question.isCorrect
                  ? "bg-green-100 border-l-4 border-green-500"
                  : "bg-red-100 border-l-4 border-red-500"
              }`}
            >
              <p className="text-lg font-medium">{question.questionText}</p>
              <p className="text-sm mt-1">
                <span className="font-semibold text-gray-600">당신의 답:</span>{" "}
                <span
                  className={`font-semibold ${
                    question.isCorrect ? "text-green-600" : "text-red-600"
                  }`}
                >
                  {question.yourAnswer || "❌ 미응답"}
                </span>
              </p>
              <p className="text-sm">
                <span className="font-semibold text-gray-600">정답:</span>{" "}
                <span className="text-primary font-semibold">
                  {question.correctAnswer}
                </span>
              </p>

              {/* ✅ 설명 추가 */}
              {question.explanation && (
                <p className="text-sm text-gray-700 mt-2">
                  📖 {question.explanation}
                </p>
              )}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default QuizResultsPage;
