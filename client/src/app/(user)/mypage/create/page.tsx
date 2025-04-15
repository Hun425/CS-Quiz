"use client";

import { useGetAllTags } from "@/lib/api/tag/useGetTags";
import QuizList from "../_components/QuizList";
import QuizCreateForm from "../_components/QuizCreateForm";
// ❗ 실제로는 아직 API 없지만, 훅 있다고 가정
import { useFakeQuizzes } from "@/lib/api/user/useFakeQuizzes";

export default function QuizPage() {
  const { data: tagData, isLoading: tagsLoading } = useGetAllTags();
  //퀴즈목록 현재 더미임
  const { data: fakeQuizzes, isLoading: quizzesLoading } = useFakeQuizzes();

  // ❗ 현재는 관리자 아님. 나중에 useCheckIsAdmin()으로 대체 예정
  const isAdmin = false;

  if (tagsLoading || quizzesLoading) {
    return (
      <div className="text-center py-12">🔄 퀴즈 관리 페이지 로딩 중...</div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto py-12 px-6 space-y-6">
      <h1 className="text-3xl font-bold text-primary">퀴즈 관리</h1>

      {!isAdmin && (
        <div className="bg-yellow-50 border border-yellow-300 rounded-xl p-6 shadow-sm space-y-4">
          <div className="flex items-center gap-3">
            <span className="text-yellow-600 text-2xl">⚠️</span>
            <div>
              <p className="text-lg font-semibold text-yellow-900">
                관리자 권한이 필요합니다
              </p>
              <p className="text-sm text-yellow-800">
                현재 계정은 관리자 권한이 없어 퀴즈 생성 기능을 사용할 수
                없습니다.
              </p>
            </div>
          </div>

          <ul className="pl-7 list-disc text-sm text-yellow-800 space-y-1">
            <li>관리자는 퀴즈 생성 및 승인 권한을 가집니다.</li>
            <li>최근 활동(플레이 기록 등)을 기반으로 심사 후 승인됩니다.</li>
            <li>
              승인 여부는{" "}
              <strong className="text-yellow-900">최대 일주일 이내</strong>에
              결정됩니다.
            </li>
          </ul>

          <div className="text-right">
            <button className="bg-yellow-500 hover:bg-yellow-600 text-white text-sm font-medium px-4 py-2 rounded-lg transition">
              🔑 관리자 권한 요청하기
            </button>
          </div>
        </div>
      )}

      {isAdmin && (
        <>
          <QuizCreateForm initialTags={tagData?.data || []} />
          <QuizList quizzes={fakeQuizzes || []} />
        </>
      )}

      {!isAdmin && (
        <div className="text-center text-gray-500 text-sm mt-12">
          ⚒ 현재 퀴즈 <strong>조회 및 생성 기능은 개발 중</strong>입니다.
        </div>
      )}
    </div>
  );
}
