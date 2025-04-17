"use client";

import Button from "@/app/_components/Button";
import { useBattleSocketStore } from "@/store/battleStore";

interface Props {
  isReady: boolean;
  onLeave: () => void;
  onToggleReady: () => void;
}

const BattleControlButtons = ({ isReady, onLeave, onToggleReady }: Props) => {
  const status = useBattleSocketStore((s) => s.status);
  const isGameStarted = status !== null;

  return (
    <div className="flex justify-center gap-3 mt-4">
      <Button
        variant="danger"
        size="medium"
        className="px-5 py-2 text-sm"
        onClick={onLeave}
      >
        나가기
      </Button>
      <Button
        variant={isReady ? "outline" : "success"}
        size="medium"
        className="px-5 py-2 text-sm"
        onClick={onToggleReady}
        disabled={isReady && isGameStarted}
        title={
          isReady && isGameStarted
            ? "게임이 시작된 후에는 취소할 수 없습니다."
            : ""
        }
      >
        {isReady ? "❌ 준비 취소" : "✅ 준비 완료"}
      </Button>
    </div>
  );
};

export default BattleControlButtons;
