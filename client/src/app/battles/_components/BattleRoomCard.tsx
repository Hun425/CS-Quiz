import { BattleRoomResponse, BattleStatus } from "@/lib/types/battle";
import Button from "@/app/_components/Button";
import { useRouter } from "next/navigation";

const BattleRoomCard = ({ room }: { room: BattleRoomResponse }) => {
  const router = useRouter();

  const isJoinable =
    room.status === BattleStatus.WAITING || room.status === BattleStatus.READY;

  const isFull = room.currentParticipants >= room.maxParticipants;

  const handleJoinClick = () => {
    if (!isJoinable || isFull) return;
    router.push(`/battles/${room.id}`);
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

  const isButtonDisabled = !isJoinable || isFull;

  return (
    <div
      className={`
      w-full bg-background border border-border p-2 sm:p-3 rounded-lg
      flex flex-col sm:flex-row sm:justify-between sm:items-center
      gap-2 sm:gap-4
      transition-transform duration-200
      hover:-translate-y-[2px] hover:shadow-md
      ${
        !isJoinable || isFull
          ? "opacity-60 cursor-not-allowed pointer-events-none"
          : ""
      }
    `}
    >
      {/* 정보 */}
      <div className="flex flex-col sm:flex-row sm:items-center gap-1 sm:gap-3 flex-1">
        {/* 룸코드 + 퀴즈 제목 */}
        <div className="flex items-center gap-2 overflow-hidden">
          {/* 룸코드 박스 */}
          <span className="text-xs sm:text-sm font-semibold bg-sub-background px-2 py-1 rounded text-primary flex-shrink-0">
            {room.roomCode}
          </span>

          {/* 퀴즈 제목 */}
          <h3 className="text-sm sm:text-base font-semibold text-foreground truncate">
            {room.quizTitle}
          </h3>
        </div>

        {/* 문제 수 */}
        <span className="text-xs text-neutral sm:inline hidden">
          🧩 {room.questionCount}문제
        </span>

        {/* 제한시간 */}
        <span className="text-xs text-neutral sm:inline hidden">
          ⏱ {room.timeLimit}초
        </span>

        {/* 대결 상태 */}
        <span className={`text-xs font-medium ${statusColor[room.status]}`}>
          ⚡ {getStatusText()}
        </span>

        {/* 참가자 수 */}
        <span className="text-xs text-neutral">
          👥 {room.currentParticipants}/{room.maxParticipants}
        </span>
      </div>

      {/* 버튼 */}
      <div className="self-stretch sm:self-auto">
        <Button
          variant={buttonVariant[room.status]}
          size="medium"
          disabled={isButtonDisabled}
          onClick={handleJoinClick}
          className="w-full sm:w-auto"
        >
          {buttonText[room.status]}
        </Button>
      </div>
    </div>
  );
};

export default BattleRoomCard;
