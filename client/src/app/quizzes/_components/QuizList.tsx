import QuizCard from "./QuizCard";
import { QuizSummaryResponse } from "@/lib/types/quiz";

interface Props {
  quizzes: QuizSummaryResponse[];
}

const QuizList: React.FC<Props> = ({ quizzes }) => {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
      {quizzes.length > 0 ? (
        quizzes.map((quiz) => <QuizCard key={quiz.id} quiz={quiz} />)
      ) : (
        <p className="text-center col-span-3 text-muted">퀴즈가 없습니다.</p>
      )}
    </div>
  );
};

export default QuizList;
