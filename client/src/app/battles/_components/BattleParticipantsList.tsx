import { BattleParticipantsPayload } from "@/lib/types/battle";
import Image from "next/image";

interface Props {
  participants: BattleParticipantsPayload | null;
}

const BattleParticipantsList: React.FC<Props> = ({ participants }) => {
  const hasParticipants = (participants?.participants?.length ?? 0) > 0;

  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 gap-6">
      {hasParticipants ? (
        participants!.participants.map((p) => (
          <div
            key={p.userId}
            className={`p-4 rounded-2xl flex flex-col items-center border ${
              p.ready
                ? "border-green-500 bg-background"
                : "border-border bg-background"
            }`}
          >
            <Image
              src={p.profileImage || "/default-avatar.png"}
              alt={p.username}
              className="rounded-full"
              width={80}
              height={80}
              priority
            />
            <p className="mt-3 text-lg font-semibold text-foreground">
              {p.username}
            </p>
            <p
              className={`mt-2 text-sm font-bold px-3 py-1 rounded-full tracking-wide ${
                p.ready
                  ? "bg-green-100 text-green-700 border border-green-400"
                  : "bg-gray-100 text-gray-500 border border-gray-300"
              }`}
            >
              {p.ready ? "✅ 준비 완료" : "⏳ 대기 중"}
            </p>
          </div>
        ))
      ) : (
        <div className="col-span-full text-center text-gray-400 py-12 flex flex-col items-center min-h-full">
          <p className="text-md">아직 참가자가 없어요. 🕊️</p>
          <p className="text-sm text-gray-300 mt-1">
            누군가 들어올 때까지 기다려주세요!
          </p>
        </div>
      )}
    </div>
  );
};

export default BattleParticipantsList;
