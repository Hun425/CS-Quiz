"use client";
import { useRouter } from "next/navigation";
import { useState, useTransition } from "react";
import { useCreateBattleRoom } from "@/lib/api/battle/useCreateBattleRoom";
import { useSearchQuizzes } from "@/lib/api/quiz/useSearchQuizzes";
import { useDebounce } from "@/lib/hooks/useDebounce";
import Button from "@/app/_components/Button";

interface CreateBattleRoomModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

const CreateBattleRoomModal: React.FC<CreateBattleRoomModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
}) => {
  const router = useRouter();
  const { mutateAsync: createBattleRoom, isPending } = useCreateBattleRoom();
  const [quizId, setQuizId] = useState<number | null>(null);
  const [maxParticipants, setMaxParticipants] = useState<number>(4);
  const [searchQuery, setSearchQuery] = useState("");
  const debouncedQuery = useDebounce(searchQuery, 300);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [, startTransition] = useTransition();

  const { data: searchedQuizzes, isLoading: isSearchLoading } =
    useSearchQuizzes({ title: debouncedQuery });

  const handleCreateBattleRoom = async () => {
    if (!quizId) {
      setErrorMessage("❌ 퀴즈를 선택하세요!");
      return;
    }

    try {
      const response = await createBattleRoom({ quizId, maxParticipants });
      const battleRoomId = response.data.id;

      alert("✅ 배틀룸이 생성되었습니다!");
      onClose();
      onSuccess();
      router.push(`/battles/${battleRoomId}`);
    } catch (error) {
      console.error("배틀룸 생성 실패:", error);
      setErrorMessage("❌ 배틀룸 생성에 실패했습니다. 다시 시도해주세요.");
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 text-default">
      <div className="bg-white p-6 rounded-lg shadow-lg w-[400px] max-h-[90vh] overflow-y-auto">
        <h2 className="text-xl font-bold mb-4">새 배틀룸 생성</h2>

        {/* 최대 참가자 수 */}
        <label className="block text-sm font-medium mb-1">최대 참가자 수</label>
        <input
          type="number"
          className="w-full p-2 border rounded-md mb-4"
          value={maxParticipants}
          onChange={(e) => setMaxParticipants(Number(e.target.value))}
          min={2}
          max={10}
        />

        {/* 퀴즈 검색 */}
        <label className="block text-sm font-medium mb-1">퀴즈 검색</label>
        <input
          type="search"
          className="w-full p-2 border rounded-md mb-3"
          placeholder="퀴즈 제목으로 검색"
          value={searchQuery}
          onChange={(e) =>
            startTransition(() => setSearchQuery(e.target.value))
          }
        />

        {/* 퀴즈 검색 결과 */}
        <div className="h-[200px] overflow-y-auto border rounded-md mb-4 p-2 bg-gray-50">
          {isSearchLoading ? (
            <p className="text-sm text-center text-gray-500">검색 중...</p>
          ) : searchedQuizzes?.content.length ? (
            searchedQuizzes.content.map((quiz) => (
              <div
                key={quiz.id}
                className={`p-2 rounded cursor-pointer hover:bg-primary hover:text-white transition-colors ${
                  quizId === quiz.id ? "bg-primary text-white" : ""
                }`}
                onClick={() => setQuizId(quiz.id)}
              >
                {quiz.title}
              </div>
            ))
          ) : (
            <p className="text-sm text-center text-gray-400">
              검색 결과가 없습니다. 다른 키워드를 입력해보세요.
            </p>
          )}
        </div>

        {/* 에러 메시지 */}
        {errorMessage && (
          <p className="text-red-500 text-sm text-center mt-3">
            {errorMessage}
          </p>
        )}

        {/* 버튼 */}
        <div className="flex justify-end space-x-2 mt-5">
          <Button variant="outline" size="small" onClick={onClose}>
            취소
          </Button>
          <Button
            variant="primary"
            size="small"
            onClick={handleCreateBattleRoom}
            disabled={isPending}
          >
            {isPending ? "생성 중..." : "생성"}
          </Button>
        </div>
      </div>
    </div>
  );
};

export default CreateBattleRoomModal;
