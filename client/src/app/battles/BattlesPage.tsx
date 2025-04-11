"use client";

import { useState } from "react";
import { useGetActiveBattleRooms } from "@/lib/api/battle/useGetActiveBattleRooms";
import { useGetMyActiveBattleRoom } from "@/lib/api/battle/useGetMyActiveBattleRoom";
import CreateBattleRoomModal from "./_components/CreateBattleRoomModal";
import { BattleRoomResponse } from "@/lib/types/battle";
import BattleRoomCard from "./_components/BattleRoomCard";
import Button from "../_components/Button";

const BattlesPage: React.FC = () => {
  const {
    data: activeRoomsData,
    isLoading: isActiveRoomsLoading,
    refetch: refetchActiveRooms, // 🔁 전체 목록 리패치
  } = useGetActiveBattleRooms();

  const {
    data: myBattleRoomData,
    isLoading: isMyBattleRoomLoading,
    refetch: refetchMyRoom, // ✅ 참여중 대결 리패치
  } = useGetMyActiveBattleRoom();

  const [isModalOpen, setIsModalOpen] = useState(false);
  const handleCreateRoomSuccess = async () => {
    await refetchActiveRooms();
    await refetchMyRoom();
    setIsModalOpen(false);
  };

  return (
    <div className="bg-sub-background min-h-screen max-w-screen-xl mx-auto px-4 py-8 sm:px-6 lg:px-8">
      <div className="w-full mx-auto space-y-6">
        {/* 헤더 */}
        <div className="bg-primary text-white p-6 rounded-lg shadow-lg flex flex-col sm:flex-row justify-between items-center transition-all">
          <div className="text-center sm:text-left">
            <h2 className="text-2xl font-bold tracking-tight text-white">
              실시간 퀴즈 대결
            </h2>
            <p className="text-md mt-2 opacity-90 text-white">
              실력과 스피드를 겨루는 실시간 퀴즈 대결에 도전해보세요!
            </p>
          </div>
          <Button
            variant="danger"
            size="medium"
            className="text-white mt-4 sm:mt-0 hover:scale-105 transition-transform duration-200 shadow-md hover:shadow-lg"
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
          className="bg-background p-6 rounded-lg shadow-sm border border-card-border transition-all"
          aria-label="참여 중인 퀴즈 대결"
        >
          <h2 className="text-xl font-bold border-b-2 border-primary pb-2 mb-2">
            🏆 참여 중인 대결
          </h2>
          {isMyBattleRoomLoading ? (
            <p className="flex justify-center items-center h-20 text-muted text-center rounded-md bg-subtle">
              로딩 중...
            </p>
          ) : myBattleRoomData?.data ? (
            <BattleRoomCard room={myBattleRoomData.data} />
          ) : (
            <p className="flex justify-center items-center h-20 text-muted text-center rounded-md bg-subtle">
              현재 참여 중인 대결이 없습니다.
            </p>
          )}
        </section>

        {/* 활성화된 배틀룸 목록 */}
        <section
          className="bg-background p-6 rounded-lg shadow-sm border border-card-border transition-all h-[32rem] sm:h-[28rem]"
          aria-label="현재 활성화된 퀴즈 대결 목록"
        >
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-bold border-b-2 border-primary pb-2">
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
            <p className="text-center py-4 text-muted animate-fade-in-out">
              로딩 중...
            </p>
          ) : activeRoomsData?.data?.length ? (
            <ul className="flex flex-col gap-4 overflow-y-auto pr-1 h-full">
              {activeRoomsData.data.map((room: BattleRoomResponse) => (
                <li
                  key={room.id}
                  aria-label={`${room.quizTitle} 퀴즈 대결 카드`}
                  className="w-full"
                >
                  <BattleRoomCard room={room} />
                </li>
              ))}
            </ul>
          ) : (
            <p className="flex justify-center items-center h-full text-muted text-center rounded-md bg-subtle">
              현재 활성화된 대결이 없습니다.
            </p>
          )}
        </section>

        {/* 🔹 새 배틀룸 생성 모달 */}
        <CreateBattleRoomModal
          isOpen={isModalOpen}
          onClose={() => setIsModalOpen(false)}
          onSuccess={handleCreateRoomSuccess}
          aria-label="새로운 퀴즈 대결 생성 모달 창"
        />
      </div>
    </div>
  );
};

export default BattlesPage;
