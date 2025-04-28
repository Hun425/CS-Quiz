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
    <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
      {/* ✅ 모달 박스 */}
      <div className="bg-background pt-3 pb-6 px-6 rounded-2xl shadow-lg w-[380px] max-h-[100vh] overflow-y-auto flex flex-col gap-5">
        {/* pt-3 로 padding-top 줄여줌! */}

        {/* 타이틀+설명 묶기 */}
        <div className="flex flex-col items-center gap-2">
          <h2 className="text-xl font-bold mt-3 text-primary text-center">
            배틀룸 생성
          </h2>
          <p className=" text-center text-sm text-muted leading-relaxed">
            한 번에 하나의 방에만 참여할 수 있어요.
            <br />
            생성하면 바로 배틀룸으로 이동합니다.
          </p>
        </div>

        {/* ✅ 최대 참가자 수 */}
        <div className="flex flex-col gap-1">
          <label className="text-sm font-medium">최대 참가자 수 (2~10)</label>
          <input
            type="number"
            value={maxParticipants}
            onChange={(e) => setMaxParticipants(Number(e.target.value))}
            min={2}
            max={10}
            className="w-full p-2 text-sm border border-border rounded-md bg-background"
          />
        </div>

        {/* ✅ 퀴즈 검색창 */}
        <div className="flex flex-col gap-1">
          <label className="text-sm font-medium">퀴즈 검색</label>
          <input
            type="search"
            value={searchQuery}
            placeholder="퀴즈 주제를 검색하세요"
            onChange={(e) =>
              startTransition(() => setSearchQuery(e.target.value))
            }
            className="w-full p-2 text-sm border border-border rounded-md bg-background"
          />
        </div>

        {/* ✅ 퀴즈 목록 */}
        <div className="h-[200px] overflow-y-auto border border-border bg-sub-background p-2 rounded-md space-y-2">
          {isSearchLoading ? (
            <p className="text-center text-sm text-muted">🔍 검색 중...</p>
          ) : searchedQuizzes?.content.length ? (
            searchedQuizzes.content.map((quiz) => (
              <div
                key={quiz.id}
                className={`p-2 rounded-md text-sm cursor-pointer transition ${
                  quizId === quiz.id
                    ? "bg-primary text-white"
                    : "hover:bg-primary/10"
                }`}
                onClick={() => setQuizId(quiz.id)}
              >
                {quiz.title}
              </div>
            ))
          ) : (
            <p className="text-center text-sm text-muted">
              검색 결과가 없습니다.
            </p>
          )}
        </div>

        {/* ✅ 에러 메시지 */}
        {errorMessage && (
          <p className="text-center text-sm text-danger">{errorMessage}</p>
        )}

        {/* ✅ 버튼 영역 */}
        <div className="flex justify-end gap-3 mt-2">
          <Button variant="outline" size="small" onClick={onClose}>
            취소
          </Button>
          <Button
            variant="primary"
            size="small"
            onClick={handleCreateBattleRoom}
            disabled={isPending}
          >
            {isPending ? "생성 중..." : "생성하기"}
          </Button>
        </div>
      </div>
    </div>
  );
};

export default CreateBattleRoomModal;
