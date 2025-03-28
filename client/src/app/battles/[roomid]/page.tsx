"use client";

import { useParams, useRouter } from "next/navigation";
import { useGetBattleRoom } from "@/lib/api/battle/useGetBattleRoom";
import { useBattleSocket } from "@/lib/services/websocket/useBattleSocket";
import { useBattleSocketStore } from "@/store/battleStore";
import { useProfileStore } from "@/store/profileStore";
import BattleHeader from "../_components/BattleHeader";
import BattleParticipantsList from "../_components/BattleParticipantsList";
import Button from "@/app/_components/Button";
import battleSocketClient from "@/lib/services/websocket/battleWebSocketService";

const BattleRoomPage: React.FC = () => {
  const router = useRouter();
  const { roomid } = useParams();
  const roomId = Number(roomid);

  const { userProfile } = useProfileStore.getState();
  const userId = userProfile?.id;

  // 배틀룸 기본 정보 가져오기
  const { data, isLoading } = useGetBattleRoom(roomId);
  const battleRoom = data?.data;

  // ✅ 소켓 연결 훅
  useBattleSocket(roomId);
  // Zustand에서 소켓 상태 가져오기
  const participants = useBattleSocketStore(
    (state) => state.participantsPayload
  );

  // ✅ 나가기 버튼
  const handleLeave = () => {
    router.push("/battles");
  };

  // ✅ 준비 완료
  const handleReady = () => {
    if (!roomId || !userId) return;

    // WebSocket을 통해 준비 완료 메시지 전송
    battleSocketClient.toggleReady();
  };

  // ✅ 로딩 상태
  if (!userId || isLoading) {
    return (
      <p className="text-center py-4 text-gray-500 min-h-screen flex items-center justify-center">
        로딩 중...
      </p>
    );
  }

  if (!battleRoom) {
    return (
      <p className="text-center py-4 text-gray-500 min-h-screen flex items-center justify-center">
        배틀룸을 찾을 수 없습니다.
      </p>
    );
  }

  return (
    <div className="max-w-xl mx-auto p-6 pt-16 space-y-6 min-h-screen">
      <BattleHeader
        quizTitle={battleRoom.quizTitle}
        roomCode={battleRoom.roomCode}
      />

      <BattleParticipantsList participants={participants} />

      <div className="flex justify-center gap-4">
        <Button variant="danger" size="large" onClick={handleLeave}>
          나가기
        </Button>
        <Button variant="success" size="large" onClick={handleReady}>
          준비 완료
        </Button>
      </div>
    </div>
  );
};

export default BattleRoomPage;
