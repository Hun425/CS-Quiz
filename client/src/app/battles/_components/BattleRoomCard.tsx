import { BattleRoomResponse, BattleStatus } from "@/lib/types/battle";
import Button from "@/app/_components/Button";
import { useRouter } from "next/navigation";
const BattleRoomCard = ({ room }: { room: BattleRoomResponse }) => {
  const router = useRouter();

  const handleJoinClick = async () => {
    if (room.currentParticipants >= room.maxParticipants) return;

    try {
      router.push(`/battles/${room.id}`);
    } catch (error) {
      alert("배틀 참가 중 오류가 발생했습니다.");
      console.error(error);
    }
  };

  const getStatusText = () => {
    switch (room.status) {
      case BattleStatus.WAITING:
        return "게임 대기 중";
      case BattleStatus.READY:
        return "준비 완료";
      case BattleStatus.IN_PROGRESS:
        return "게임 진행 중";
      case BattleStatus.FINISHED:
        return "종료됨";
      default:
        return "알 수 없음";
    }
  };

  const statusColor: Record<BattleStatus, string> = {
    [BattleStatus.WAITING]: "text-blue-500",
    [BattleStatus.READY]: "text-indigo-500",
    [BattleStatus.IN_PROGRESS]: "text-green-600",
    [BattleStatus.FINISHED]: "text-gray-500",
  };

  const buttonVariant: Record<
    BattleStatus,
    "primary" | "secondary" | "outline"
  > = {
    [BattleStatus.WAITING]: "primary",
    [BattleStatus.READY]: "primary",
    [BattleStatus.IN_PROGRESS]: "secondary",
    [BattleStatus.FINISHED]: "outline",
  };

  const buttonText: Record<BattleStatus, string> = {
    [BattleStatus.WAITING]: "참가하기",
    [BattleStatus.READY]: "참가하기",
    [BattleStatus.IN_PROGRESS]: "진행 중",
    [BattleStatus.FINISHED]: "종료됨",
  };

  const isButtonDisabled =
    room.status === BattleStatus.FINISHED ||
    room.currentParticipants >= room.maxParticipants;

  return (
    <div className="w-full bg-background border border-border p-4 rounded-xl shadow-md flex flex-col sm:flex-row sm:justify-between sm:items-center gap-4 sm:gap-6">
      {/* 왼쪽 정보 */}
      <div className="flex flex-col sm:flex-row sm:items-center gap-2 sm:gap-6 flex-1">
        <h3 className="text-base sm:text-lg font-semibold text-primary">
          {room.quizTitle}
        </h3>

        <span className="text-sm sm:text-base text-neutral">
          🧩 {room.questionCount}문제
        </span>

        <span className="text-sm sm:text-base text-neutral">
          ⏱ {room.timeLimit}초
        </span>

        <span
          className={`text-sm sm:text-base font-medium ${
            statusColor[room.status]
          }`}
        >
          ⚡ {getStatusText()}
        </span>

        <span className="text-sm sm:text-base text-neutral">
          👥 {room.currentParticipants}/{room.maxParticipants}
        </span>
      </div>

      {/* 오른쪽 버튼 */}
      <div className="self-end sm:self-auto">
        <Button
          variant={buttonVariant[room.status]}
          size="large"
          disabled={isButtonDisabled}
          onClick={handleJoinClick}
        >
          {buttonText[room.status]}
        </Button>
      </div>
    </div>
  );
};

export default BattleRoomCard;
