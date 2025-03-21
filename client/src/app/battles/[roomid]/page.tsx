"use client";
import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { useGetBattleRoom } from "@/lib/api/battle/useGetBattleRoom";
import battleWebSocketService from "@/lib/services/battleWebSocketService";
import { Participant, BattleStatus } from "@/lib/types/battle";
import { useProfileStore } from "@/store/profileStore";
import Button from "@/app/_components/Button";

/** ✅ 배틀룸 페이지 */
const BattleRoomPage: React.FC = () => {
  const router = useRouter();
  const { roomId } = useParams();
  const { data, isLoading } = useGetBattleRoom(Number(roomId));
  const { userProfile } = useProfileStore.getState();
  const userId = userProfile?.id;
  const [participants, setParticipants] = useState<Participant[]>([]);

  useEffect(() => {
    if (!roomId || !userId) return;

    // ✅ WebSocket 연결 먼저 실행
    battleWebSocketService.connect(Number(roomId), userId);

    // ✅ 참가자 목록 업데이트 이벤트 리스너 등록
    const handleParticipantsUpdate = (updatedParticipants: Participant[]) => {
      setParticipants(updatedParticipants);
    };

    battleWebSocketService.on("PARTICIPANTS", handleParticipantsUpdate);

    console.log("배틀룸 페이지 마운트");

    return () => {
      // ✅ 이벤트 리스너 제거 및 WebSocket 연결 해제
      battleWebSocketService.off("PARTICIPANTS");
      battleWebSocketService.disconnect();
    };
  }, [roomId, userId]);

  if (isLoading) {
    return <p className="text-center py-4 text-gray-500">로딩 중...</p>;
  }

  if (!data?.data) {
    return (
      <p className="text-center py-4 text-gray-500">
        배틀룸을 찾을 수 없습니다.
      </p>
    );
  }

  const battleRoom = data.data;

  /** ✅ 배틀룸 나가기 */
  const handleLeave = () => {
    battleWebSocketService.disconnect();
    router.push("/battles");
  };

  /** ✅ 준비 완료 토글 */
  const handleReady = () => {
    battleWebSocketService.toggleReady();
  };

  return (
    <div className="max-w-4xl mx-auto p-6 space-y-6">
      {/* 배틀룸 정보 */}
      <div className="bg-primary text-white p-6 rounded-lg shadow-md">
        <h1 className="text-2xl font-bold">{battleRoom.quizTitle} - 대기실</h1>
        <p className="opacity-80">
          {battleRoom.status === BattleStatus.WAITING
            ? "대결이 시작되기를 기다리는 중입니다."
            : "배틀이 진행 중입니다."}
        </p>
      </div>

      {/* 참가자 정보 */}
      <div className="bg-card p-6 rounded-lg shadow-md">
        <h2 className="text-xl font-bold border-b-2 border-primary pb-2 mb-4">
          참가자 목록
        </h2>
        <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
          {participants.map((participant) => (
            <div
              key={participant.userId}
              className="bg-white p-4 rounded-lg shadow-md flex flex-col items-center border border-card-border"
            >
              <img
                src={participant.profileImage || "/default-avatar.png"}
                alt={participant.username}
                className="w-20 h-20 rounded-full"
              />
              <p className="mt-2 text-lg font-semibold">
                {participant.username}
              </p>
              <p
                className={`text-sm px-2 py-1 rounded ${
                  participant.isReady ? "bg-green-300" : "bg-gray-200"
                }`}
              >
                {participant.isReady ? "✅ 준비 완료" : "⏳ 대기 중"}
              </p>
            </div>
          ))}
        </div>
      </div>

      {/* 버튼 */}
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
