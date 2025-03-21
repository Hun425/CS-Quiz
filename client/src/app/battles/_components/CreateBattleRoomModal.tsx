"use client";
import { useState } from "react";
import { useCreateBattleRoom } from "@/lib/api/battle/useCreateBattleRoom";
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
  const { mutateAsync: createBattleRoom, isPending } = useCreateBattleRoom();
  const [quizId, setQuizId] = useState<number | null>(null);
  const [maxParticipants, setMaxParticipants] = useState<number>(4);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleCreateBattleRoom = async () => {
    if (!quizId) {
      setErrorMessage("❌ 퀴즈를 선택하세요!");
      return;
    }

    try {
      await createBattleRoom({ quizId, maxParticipants });

      alert("✅ 배틀룸이 생성되었습니다!");
      onClose();
      onSuccess();
    } catch (error) {
      console.error("배틀룸 생성 실패:", error);
      setErrorMessage("❌ 배틀룸 생성에 실패했습니다. 다시 시도해주세요.");
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50">
      <div className="bg-white p-6 rounded-lg shadow-lg w-80">
        <h2 className="text-xl font-bold mb-4">새 배틀룸 생성</h2>

        {/* 🔹 퀴즈 선택 드롭다운 */}
        <label className="block text-sm font-medium text-gray-700">
          퀴즈 선택
        </label>
        <select
          className="w-full p-2 border rounded-md mb-2"
          value={quizId || ""}
          onChange={(e) => setQuizId(Number(e.target.value))}
        >
          <option value="" disabled>
            퀴즈를 선택하세요
          </option>
          <option value={1}>퀴즈 1</option>
          <option value={2}>퀴즈 2</option>
          <option value={3}>퀴즈 3</option>
        </select>

        {/* 🔹 최대 참가자 수 */}
        <label className="block text-sm font-medium text-gray-700">
          최대 참가자 수
        </label>
        <input
          type="number"
          placeholder="최대 참가자 수"
          className="w-full p-2 border rounded-md mb-4"
          value={maxParticipants}
          onChange={(e) => setMaxParticipants(Number(e.target.value))}
          min={2}
          max={10}
        />

        {/* 🔹 에러 메시지 출력 */}
        {errorMessage && (
          <p className="text-red-500 text-sm text-center mb-2">
            {errorMessage}
          </p>
        )}

        {/* 🔹 버튼 */}
        <div className="flex justify-end space-x-2">
          <Button variant="outline" size="small" onClick={onClose}>
            취소
          </Button>
          <Button
            variant="primary"
            size="small"
            onClick={handleCreateBattleRoom}
            disabled={isPending} // 🔹 로딩 중이면 버튼 비활성화
          >
            {isPending ? "생성 중..." : "생성"}
          </Button>
        </div>
      </div>
    </div>
  );
};

export default CreateBattleRoomModal;
