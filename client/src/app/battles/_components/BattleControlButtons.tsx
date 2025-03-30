"use client";

import Button from "@/app/_components/Button";

interface Props {
  isReady: boolean;
  onLeave: () => void;
  onToggleReady: () => void;
}

const BattleControlButtons = ({ isReady, onLeave, onToggleReady }: Props) => {
  return (
    <div className="flex justify-center gap-4">
      <Button variant="danger" size="large" onClick={onLeave}>
        나가기
      </Button>
      <Button
        variant={isReady ? "outline" : "success"}
        size="large"
        onClick={onToggleReady}
      >
        {isReady ? "❌ 준비 취소" : "✅ 준비 완료"}
      </Button>
    </div>
  );
};

export default BattleControlButtons;
