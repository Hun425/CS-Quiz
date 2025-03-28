import QuizCard from "./QuizCard";
import { QuizSummaryResponse } from "@/lib/types/quiz";
import { PageResponse } from "@/lib/types/common";

interface Props {
  data?: PageResponse<QuizSummaryResponse>;
  isLoading: boolean;
  error?: string | null;
  currentPage: number;
  setCurrentPage: (page: number) => void;
}

const QuizSearchList: React.FC<Props> = ({
  data,
  isLoading,
  error,
  currentPage,
  setCurrentPage,
}) => {
  const quizzes = data?.content ?? [];
  const hasQuizzes = quizzes.length > 0;

  return (
    <div className="flex flex-col gap-4">
      {/* 로딩 상태 */}
      {isLoading && (
        <p className="text-center text-muted">퀴즈를 불러오는 중...</p>
      )}

      {/* 에러 상태 */}
      {error && (
        <p className="text-center text-red-500">
          오류 발생: {error?.toString() || "알 수 없는 오류"} <br />
          다시 시도해 주세요.
        </p>
      )}

      {/* 퀴즈 목록 */}
      {!isLoading && !error && (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
          {hasQuizzes ? (
            quizzes.map((quiz) => <QuizCard key={quiz.id} quiz={quiz} />)
          ) : (
            <p className="text-center col-span-3 text-muted">
              검색된 퀴즈가 없습니다.
            </p>
          )}
        </div>
      )}

      {/* 페이지네이션 */}
      {data && data.totalPages > 0 && (
        <div className="flex justify-center mt-6 space-x-2">
          {/* 이전 버튼 */}
          <button
            className={`px-5 py-2 rounded-lg border bg-gray-200 text-gray-600 hover:bg-gray-300 transition ${
              currentPage === 0 ? "opacity-50" : ""
            }`}
            onClick={() => setCurrentPage(currentPage - 1)}
            disabled={currentPage === 0}
          >
            ⬅ 이전
          </button>

          {/* 페이지 숫자 버튼 */}
          <div className="flex space-x-2">
            {[...Array(data.totalPages)].map((_, index) => (
              <button
                key={index}
                className={`px-4 py-2 rounded-lg border transition ${
                  currentPage === index
                    ? "bg-primary text-white font-bold"
                    : "bg-gray-100 text-gray-700 hover:bg-gray-200"
                }`}
                onClick={() => setCurrentPage(index)}
              >
                {index + 1}
              </button>
            ))}
          </div>

          {/* 다음 버튼 */}
          <button
            className={`px-5 py-2 rounded-lg border bg-gray-200 text-gray-600 hover:bg-gray-300 transition ${
              currentPage === data.totalPages - 1 ? "opacity-50" : ""
            }`}
            onClick={() => setCurrentPage(currentPage + 1)}
            disabled={currentPage === data.totalPages - 1}
          >
            다음 ➡
          </button>
        </div>
      )}
    </div>
  );
};

export default QuizSearchList;
