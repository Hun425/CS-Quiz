// app/battles/_components/BattleParticipantsList.tsx
"use client";

import Image from "next/image";
import { Participant } from "@/lib/types/battle";
import { useProfileStore } from "@/store/profileStore";

interface Props {
  participants: Participant[];
}

const BattleParticipantsList = ({ participants }: Props) => {
  const currentUser = useProfileStore((state) => state.userProfile);

  if (!participants || participants.length === 0) return null;

  return (
    <div className="bg-sub-background rounded-xl p-4 shadow-sm">
      <h2 className="text-base font-semibold text-foreground mb-3">
        👥 참가자 목록
      </h2>

      <ul className="space-y-2 max-h-[400px] overflow-y-auto scrollbar-thin scrollbar-thumb-muted/40 pr-1">
        {participants.map((p) => {
          const isMe = currentUser?.id === p.userId;

          const containerClass = `flex items-center gap-3 bg-background rounded-lg px-3 py-2 border shadow-sm ${
            isMe ? "border-primary bg-primary/5" : "border-border"
          }`;

          const nameClass = `text-sm font-medium truncate ${
            isMe ? "text-primary" : "text-foreground"
          }`;

          return (
            <li key={p.userId} className={containerClass}>
              <Image
                src={p.profileImage || "/images/default_avatar.png"}
                alt={`${p.username} 프로필 이미지`}
                width={32}
                height={32}
                className="rounded-full border border-border"
              />
              <div className="flex flex-col">
                <span className={nameClass}>
                  {p.username} {isMe && "(나)"}
                </span>
                <span className="text-xs text-muted">Lv. {p.level}</span>
              </div>
            </li>
          );
        })}
      </ul>
    </div>
  );
};

export default BattleParticipantsList;
