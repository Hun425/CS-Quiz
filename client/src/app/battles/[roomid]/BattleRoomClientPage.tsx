"use client";

import { useEffect, useState } from "react";
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
  const battleStatus = useBattleSocketStore((s) => s.status);

  const [countdown, setCountdown] = useState<number | null>(null);
  const [showCountdownModal, setShowCountdownModal] = useState(false);

  // 시작 카운트다운 로직
  useEffect(() => {
    if (battleStatus === "READY") {
      setCountdown(5);
      setShowCountdownModal(true);

      const interval = setInterval(() => {
        setCountdown((prev) => {
          if (prev === null) return null;
          if (prev === 1) {
            clearInterval(interval);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);

      return () => clearInterval(interval);
    }
  }, [battleStatus]);

  // IN_PROGRESS 되면 바로 이동
  useEffect(() => {
    if (battleStatus === "IN_PROGRESS") {
      router.replace(`/battles/${roomId}/play`);
    }
  }, [battleStatus, router, roomId]);

  // 참가자 연결 실패 시 3초 후 나가기
  useEffect(() => {
    if (!participantsPayload) {
      const timeout = setTimeout(() => {
        battleSocketClient.leaveBattle();
        router.replace("/battles");
      }, 3000);
      return () => clearTimeout(timeout);
    }
  }, [participantsPayload, router]);

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
        <p className="text-xl font-semibold">연결에 문제가 발생했어요 😭</p>
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

      {/* 시작 모달 */}
      {showCountdownModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-background rounded-xl p-8 text-center space-y-4 shadow-lg">
            {countdown !== null && countdown > 0 ? (
              <>
                <h2 className="text-2xl font-bold text-primary">
                  게임이 곧 시작됩니다!
                </h2>
                <p className="text-lg text-muted-foreground">
                  {countdown}초 후에 시작
                </p>
              </>
            ) : (
              <h2 className="text-2xl font-bold text-primary animate-pulse">
                잠시만 기다려주세요...
              </h2>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default BattleRoomClientPage;
