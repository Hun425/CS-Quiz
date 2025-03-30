"use client";

import { BattleParticipantsPayload } from "@/lib/types/battle";

interface Props {
  participants: BattleParticipantsPayload | null;
}

const ReadyStatusIndicator = ({ participants }: Props) => {
  const total = participants?.participants.length ?? 0;
  const ready = participants?.participants.filter((p) => p.ready).length ?? 0;
  const allReady = total > 1 && total === ready;

  return (
    <div className="bg-sub-background p-4 rounded-xl text-center border border-border">
      <p className="text-base text-muted-foreground">
        ✅ 준비된 인원: <span className="text-primary font-bold">{ready}</span>{" "}
        / {total}
      </p>
      {allReady && (
        <p className="mt-2 text-green-600 font-semibold text-sm animate-pulse">
          🚀 모든 참가자가 준비 완료되었습니다. 잠시 후 대결이 시작됩니다!
        </p>
      )}
    </div>
  );
};

export default ReadyStatusIndicator;
