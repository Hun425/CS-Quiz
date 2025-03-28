import { BattleRoomResponse } from "@/lib/types/battle";
import { BattleStatus } from "@/lib/types/battle";
import Button from "@/app/_components/Button";
import { useRouter } from "next/navigation";

/** ✅ 배틀룸 카드 컴포넌트 */
const BattleRoomCard = ({ room }: { room: BattleRoomResponse }) => {
  const router = useRouter();

  const handleJoinClick = async () => {
    if (room.currentParticipants >= room.maxParticipants) return;

    try {
      // 🔽 여기에 API 호출 로직을 넣을 수 있어요
      // 예: await joinBattleRoom(room.id);

      router.push(`/battles/${room.id}`);
    } catch (error) {
      alert("배틀 참가 중 오류가 발생했습니다.");
      console.error(error);
    }
  };

  return (
    <div className="bg-background border border-border p-4 rounded-lg shadow-md flex flex-col space-y-2">
      <h3 className="text-lg font-semibold">{room.quizTitle}</h3>
      <p className="text-sm text-neutral">문제 수: {room.questionCount}개</p>
      <p className="text-sm text-neutral">제한 시간: {room.timeLimit}초</p>
      <p
        className={`text-sm font-semibold ${
          room.status === BattleStatus.IN_PROGRESS
            ? "text-green-500"
            : "text-gray-500"
        }`}
      >
        상태:{" "}
        {room.status === BattleStatus.WAITING
          ? "대기 중"
          : room.status === BattleStatus.IN_PROGRESS
          ? "진행 중"
          : "종료됨"}
      </p>
      <div className="flex justify-between items-center mt-2">
        <p className="text-sm text-neutral">
          참가자: {room.currentParticipants}/{room.maxParticipants}
        </p>

        <Button
          variant="primary"
          size="small"
          disabled={room.currentParticipants >= room.maxParticipants}
          onClick={handleJoinClick}
        >
          {room.status === BattleStatus.WAITING ? "참가하기" : "진행 중"}
        </Button>
      </div>
    </div>
  );
};

export default BattleRoomCard;
