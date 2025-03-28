import { BattleParticipantsPayload } from "@/lib/types/battle";
import Image from "next/image";

interface Props {
  participants: BattleParticipantsPayload | null;
}

const BattleParticipantsList: React.FC<Props> = ({ participants }) => {
  const hasParticipants = (participants?.currentParticipants ?? 0) > 0;

  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
      {hasParticipants ? (
        participants!.participants.map((p) => (
          <div
            key={p.userId}
            className="bg-white p-4 rounded-lg shadow-md flex flex-col items-center border border-card-border"
          >
            <Image
              src={p.profileImage || "/default-avatar.png"}
              alt={p.username}
              className="w-20 h-20 rounded-full"
              width={80}
              height={80}
            />
            <p className="mt-2 text-lg font-semibold">{p.username}</p>
            <p
              className={`text-sm px-2 py-1 rounded ${
                p.ready ? "bg-green-300" : "bg-gray-200"
              }`}
            >
              {p.ready ? "✅ 준비 완료" : "⏳ 대기 중"}
            </p>
          </div>
        ))
      ) : (
        <div className="col-span-full text-center text-gray-400 py-8">
          아직 참가자가 없습니다.
        </div>
      )}
    </div>
  );
};

export default BattleParticipantsList;
