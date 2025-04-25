"use client";

import { useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import { useGetBattleRoom } from "@/lib/api/battle/useGetBattleRoom";
import { useBattleSocket } from "@/lib/services/websocket/useBattleSocket";
import { useBattleSocketStore } from "@/store/battleStore";
import { useProfileStore } from "@/store/profileStore";
import BattleHeader from "../_components/BattleHeader";
import BattleParticipantsList from "../_components/BattleParticipantsList";
import ReadyStatusIndicator from "../_components/ReadyStatusIndicator";
import BattleControlButtons from "../_components/BattleControlButtons";
import battleSocketClient from "@/lib/services/websocket/battleWebSocketService";
import Loading from "@/app/_components/Loading";

const BattleRoomClientPage = () => {
  const router = useRouter();
  const { roomid } = useParams();
  const roomId = Number(roomid);
  const userId = useProfileStore.getState().userProfile?.id;

  const { data, isLoading } = useGetBattleRoom(roomId);
  const battleRoom = data?.data;

  useBattleSocket(roomId);
  // useBattleHealthCheck(roomId);
  const participantsPayload = useBattleSocketStore(
    (s) => s.participantsPayload
  );

  //뒤로가기 했을때도 방 나가게 추가 설정하기
  // useEffect(() => {
  //   return () => {
  //     battleSocketClient.leaveBattle();
  //   };
  // }, []);

  useEffect(() => {
    if (!participantsPayload) {
      const timeout = setTimeout(() => {
        battleSocketClient.leaveBattle();
        router.replace("/battles");
      }, 3000);
      return () => clearTimeout(timeout);
    }
  }, [participantsPayload, router]);

  // 방 떠나는건 버튼 누르는것만으로 가능
  const handleLeave = () => {
    battleSocketClient.leaveBattle();
    router.push("/battles");
  };

  const handleToggleReady = () => {
    battleSocketClient.toggleReady();
  };

  if (!userId || isLoading) return <Loading />;

  if (!participantsPayload) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen text-center space-y-2 text-danger">
        <p className="text-xl font-semibold">연결에 문제가 발생했어요 😢</p>
        <p className="text-sm text-muted">잠시 후 배틀 목록으로 이동합니다.</p>
      </div>
    );
  }

  if (!battleRoom)
    return (
      <div className="flex justify-center items-center min-h-screen text-danger">
        배틀룸을 찾을 수 없습니다.
      </div>
    );

  const participants = participantsPayload?.participants ?? [];
  const myStatus = participants.find((p) => p.userId === userId);
  const isReady = myStatus?.ready ?? false;

  return (
    <div className="max-w-full mx-auto p-6 py-8 space-y-6 min-h-screen">
      <div className="max-w-screen-lg mx-auto space-y-6 min-h-screen">
        <BattleHeader battleRoom={battleRoom} />
        <ReadyStatusIndicator participantsPayload={participantsPayload} />
        <BattleControlButtons
          isReady={isReady}
          onLeave={handleLeave}
          onToggleReady={handleToggleReady}
        />
        <BattleParticipantsList
          participants={participantsPayload.participants}
        />
      </div>
    </div>
  );
};

export default BattleRoomClientPage;
