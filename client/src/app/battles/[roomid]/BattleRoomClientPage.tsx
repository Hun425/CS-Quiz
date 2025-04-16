"use client";

import { useEffect, useMemo } from "react";
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

  const participantsPayload = useBattleSocketStore(
    (s) => s.participantsPayload
  );

  const myParticipant = useMemo(() => {
    return participantsPayload?.participants?.find((p) => p.userId === userId);
  }, [participantsPayload, userId]);

  const isReady = myParticipant?.ready ?? false;

  const handleLeave = () => {
    battleSocketClient.leaveBattle();
    router.push("/battles");
  };

  const handleToggleReady = () => {
    battleSocketClient.toggleReady();
  };

  useEffect(() => {
    return () => {
      battleSocketClient.leaveBattle();
    };
  }, []);

  if (!userId || isLoading || !participantsPayload?.participants)
    return <Loading />;

  if (!battleRoom)
    return (
      <div className="flex justify-center items-center min-h-screen text-danger">
        배틀룸을 찾을 수 없습니다.
      </div>
    );

  return (
    <div className="max-w-full mx-auto p-6 py-16 space-y-6 min-h-screen">
      <div className="max-w-screen-lg mx-auto space-y-6 min-h-screen">
        <BattleHeader battleRoom={battleRoom} />
        <ReadyStatusIndicator participants={participantsPayload} />
        <BattleControlButtons
          isReady={isReady}
          onLeave={handleLeave}
          onToggleReady={handleToggleReady}
        />
        <BattleParticipantsList participants={participantsPayload} />
      </div>
    </div>
  );
};

export default BattleRoomClientPage;
