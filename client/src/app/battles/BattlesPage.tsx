"use client";

import { useState } from "react";
import { useGetActiveBattleRooms } from "@/lib/api/battle/useGetActiveBattleRooms";
import { useGetMyActiveBattleRoom } from "@/lib/api/battle/useGetMyActiveBattleRoom";
import CreateBattleRoomModal from "./_components/CreateBattleRoomModal";
import BattleRoomCard from "./_components/BattleRoomCard";
import Button from "../_components/Button";
import { useQueryClient } from "@tanstack/react-query"; // ✅ 추가
import { useEffect } from "react";

const BattlesPage: React.FC = () => {
  const queryClient = useQueryClient();
  useEffect(() => {
    // ✅ 페이지 들어올 때 강제 캐시 무효화
    queryClient.invalidateQueries({ queryKey: ["activeBattleRooms"] });
    queryClient.invalidateQueries({ queryKey: ["myActiveBattleRoom"] });
  }, [queryClient]);

  const {
    data: activeRoomsData,
    isLoading: isActiveRoomsLoading,
    refetch: refetchActiveRooms,
  } = useGetActiveBattleRooms();

  const {
    data: myBattleRoomData,
    isLoading: isMyBattleRoomLoading,
    refetch: refetchMyRoom,
  } = useGetMyActiveBattleRoom();

  const [isModalOpen, setIsModalOpen] = useState(false);
  const handleCreateRoomSuccess = async () => {
    await refetchActiveRooms();
    await refetchMyRoom();
    setIsModalOpen(false);
  };

  // 준비
  const myRoom = myBattleRoomData?.data;
  const activeRooms = activeRoomsData?.data;

  // ⛑ 안전하게 검사 (null, undefined, 빈배열, 잘못된 타입 모두 커버)
  const hasMyRoom = myRoom && !Array.isArray(myRoom); // 단일 객체만 허용

  const hasActiveRooms = Array.isArray(activeRooms) && activeRooms.length > 0;

  return (
    <div className="bg-sub-background min-h-screen max-w-screen-lg mx-auto px-3 py-6 sm:px-4 lg:px-6">
      <div className="w-full mx-auto space-y-5">
        <div className="bg-primary text-white p-5 rounded-md flex flex-col sm:flex-row justify-between items-center">
          <div className="text-center sm:text-left">
            <h2 className="text-xl font-bold tracking-tight text-white">
              실시간 퀴즈 대결
            </h2>
            <p className="text-sm mt-1 opacity-90 text-white">
              실력과 스피드를 겨루는 퀴즈 대결에 도전해보세요!
            </p>
          </div>
          <Button
            variant="danger"
            size="medium"
            className="text-white mt-3 sm:mt-0 hover:scale-105 transition-transform duration-200 shadow-md hover:shadow-lg"
            onClick={() => setIsModalOpen(true)}
            aria-label="새로운 퀴즈 대결 생성하기"
          >
            새 대결 만들기
          </Button>
        </div>

        {/* 대결 방법 안내 */}
        <section
          className="bg-background p-6 rounded-lg  border border-card-border"
          aria-label="퀴즈 대결 방법 안내"
        >
          <h2 className="text-xl font-bold border-b-2 border-primary pb-2 mb-4">
            📌 대결 방법
          </h2>
          <ol className="list-decimal list-inside space-y-1">
            <li>
              대결 참가: 참가하려는 대결을 선택하거나 새로운 대결을 만듭니다.
            </li>
            <li>
              준비 완료: 대결방에 입장하면 <strong>준비 완료</strong> 버튼을
              클릭하여 준비 상태로 변경합니다.
            </li>
            <li>
              대결 시작: 모든 참가자가 준비 완료되면 대결이 자동으로 시작됩니다.
            </li>
            <li>
              정답 제출: 문제를 풀어 빠르고 정확하게 답변을 제출하세요. 정답률과
              응답 시간에 따라 점수가 부여됩니다.
            </li>
          </ol>
        </section>

        {/* 내 활성 배틀룸 */}
        <section
          className="bg-background p-6 rounded-lg  border border-card-border transition-all"
          aria-label="참여 중인 퀴즈 대결"
        >
          <h2 className="text-xl font-bold border-b-2 border-primary pb-2 mb-2">
            🏆 참여 중인 대결
          </h2>
          {isMyBattleRoomLoading ? (
            <p>로딩 중...</p>
          ) : hasMyRoom ? (
            <BattleRoomCard room={myRoom} />
          ) : (
            <p>현재 참여 중인 대결이 없습니다.</p>
          )}
        </section>

        {/* 활성화된 배틀룸 목록 */}
        <section
          className="bg-background p-5 rounded-md  border border-card-border h-[30rem] sm:h-[26rem]"
          aria-label="현재 활성화된 퀴즈 대결 목록"
        >
          <div className="flex justify-between items-center mb-3">
            <h2 className="text-lg font-bold border-b-2 border-primary pb-2">
              🎯 활성화된 대결
            </h2>
            <Button
              variant="outline"
              size="small"
              onClick={() => refetchActiveRooms()}
              aria-label="활성화된 퀴즈 목록 새로고침"
            >
              새로고침
            </Button>
          </div>

          {isActiveRoomsLoading ? (
            <p>로딩 중...</p>
          ) : hasActiveRooms ? (
            <ul className="overflow-y-auto space-y-2">
              {activeRooms!.map((room) => (
                <li key={room.id}>
                  <BattleRoomCard room={room} />
                </li>
              ))}
            </ul>
          ) : (
            <p>현재 활성화된 대결이 없습니다.</p>
          )}
        </section>
        {/* 🔹 새 배틀룸 생성 모달 */}
      </div>
      <CreateBattleRoomModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSuccess={handleCreateRoomSuccess}
        aria-label="새로운 퀴즈 대결 생성 모달 창"
      />
    </div>
  );
};

export default BattlesPage;
