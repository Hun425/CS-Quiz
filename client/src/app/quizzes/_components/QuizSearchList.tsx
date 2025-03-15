import QuizCard from "./QuizCard";
import { QuizSummaryResponse } from "@/lib/types/quiz";
import { PageResponse } from "@/lib/types/common";
import { mockQuizzes } from "@/lib/mockQuizzes";
// import Pagination from "@/app/_components/Pagination";

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
  // 검색 결과 또는 더미 데이터 사용
  const quizzes =
    data?.content && data.content.length > 0 ? data.content : mockQuizzes;
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
              퀴즈가 없습니다.
            </p>
          )}
        </div>
      )}

      {/* 페이지네이션 */}
      {data && data.totalPages > 1 && (
        <div className="flex justify-center mt-4 gap-2">
          <button
            className="px-4 py-2 border rounded-md bg-gray-100 disabled:opacity-50"
            onClick={() => setCurrentPage(currentPage - 1)}
            disabled={currentPage === 0}
          >
            이전
          </button>

          {[...Array(data.totalPages)].map((_, index) => (
            <button
              key={index}
              className={`px-4 py-2 border rounded-md ${
                currentPage === index ? "bg-blue-500 text-white" : "bg-gray-100"
              }`}
              onClick={() => setCurrentPage(index)}
            >
              {index + 1}
            </button>
          ))}

          <button
            className="px-4 py-2 border rounded-md bg-gray-100 disabled:opacity-50"
            onClick={() => setCurrentPage(currentPage + 1)}
            disabled={currentPage === data.totalPages - 1}
          >
            다음
          </button>
        </div>
      )}

      {/* <Pagination /> */}
    </div>
  );
};

export default QuizSearchList;
