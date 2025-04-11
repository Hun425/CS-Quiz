// app/(admin)/quiz/page.tsx
import { getAllQuizzes } from "@/lib/api/quiz/server";
import { getAllTags } from "@/lib/api/tag/server";
import QuizList from "./QuizList";
import QuizCreateForm from "./QuizCreateForm";
import { isAdmin } from "@/lib/auth"; // 임시 관리자 확인용 함수

export default async function QuizPage() {
  const quizzes = await getAllQuizzes();
  const tags = await getAllTags();
  const admin = await isAdmin(); // 임시: 사용자 정보 기반 체크

  return (
    <div className="max-w-5xl mx-auto py-12 px-6 space-y-10">
      <h1 className="text-3xl font-bold text-primary">퀴즈 관리</h1>

      {!admin && (
        <div className="bg-yellow-100 border border-yellow-300 p-4 rounded text-yellow-800">
          현재 관리자 권한이 없습니다. 권한 요청을 원하시면 아래 버튼을
          눌러주세요.
          <button className="mt-2 block bg-yellow-500 text-white px-4 py-2 rounded">
            관리자 권한 요청하기
          </button>
        </div>
      )}

      <QuizList quizzes={quizzes} />

      {admin && (
        <>
          <QuizCreateForm initialTags={tags} />
        </>
      )}
    </div>
  );
}
