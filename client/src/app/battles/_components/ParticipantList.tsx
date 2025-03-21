import { BattleRoomResponse } from "@/lib/types/battle";

/** ✅ 참가자 목록 컴포넌트 */
const ParticipantList = ({
  participants,
}: {
  participants: BattleRoomResponse["participants"];
}) => {
  return (
    <div className="flex flex-wrap gap-2 mt-2">
      {participants.map((participant) => (
        <div
          key={participant.userId}
          className="flex items-center space-x-2 p-2 border rounded-md"
        >
          <img
            src={participant.profileImage || "/default-avatar.png"}
            alt={participant.username}
            className="w-8 h-8 rounded-full"
          />
          <div>
            <p className="text-sm font-medium">{participant.username}</p>
            <p className="text-xs text-gray-500">
              LV.{participant.level}{" "}
              {participant.isReady ? "✅ 준비 완료" : "⏳ 대기 중"}
            </p>
          </div>
        </div>
      ))}
    </div>
  );
};

export default ParticipantList;
