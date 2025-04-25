"use client";

import { BattleParticipantsResponse } from "@/lib/types/battle";
import { CheckCircle, Users } from "lucide-react";

interface Props {
  participantsPayload: BattleParticipantsResponse | null;
}

const ReadyStatusIndicator = ({ participantsPayload }: Props) => {
  const participants = participantsPayload?.participants ?? [];
  const current = participantsPayload?.currentParticipants ?? 0;
  const max = participantsPayload?.maxParticipants ?? 0;
  const ready = participants.filter((p) => p.ready).length;

  return (
    <div className="flex flex-wrap justify-between items-center gap-x-4 gap-y-2 px-4 py-3 border border-border rounded-lg bg-sub-background text-sm shadow-sm">
      <div className="flex items-center gap-2 text-muted-foreground">
        <Users className="w-4 h-4 text-primary" />
        <span>
          <span className="font-medium text-foreground">{current}</span> / {max}{" "}
          명 참여 중
        </span>
      </div>
      <div className="flex items-center gap-2 text-muted-foreground">
        <CheckCircle className="w-4 h-4 text-green-600" />
        <span>
          <span className="font-medium text-green-700">{ready}</span> /{" "}
          {current} 준비 완료
        </span>
      </div>
    </div>
  );
};

export default ReadyStatusIndicator;
