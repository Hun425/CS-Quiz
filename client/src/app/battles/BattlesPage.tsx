"use client";

import { useState, useEffect } from "react";
import { useGetActiveBattleRooms } from "@/lib/api/battle/useGetActiveBattleRooms";
import { useGetMyActiveBattleRoom } from "@/lib/api/battle/useGetMyActiveBattleRoom";
import CreateBattleRoomModal from "./_components/CreateBattleRoomModal";
import BattleRoomCard from "./_components/BattleRoomCard";
import Button from "../_components/Button";
import { useQueryClient } from "@tanstack/react-query";
import { motion, AnimatePresence } from "framer-motion";
import { ChevronDown, ChevronUp } from "lucide-react";

const BattlesPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [showDescription, setShowDescription] = useState(false);

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

  useEffect(() => {
    queryClient.invalidateQueries({ queryKey: ["activeBattleRooms"] });
    queryClient.invalidateQueries({ queryKey: ["myActiveBattleRoom"] });
  }, [queryClient]);

  const handleCreateRoomSuccess = async () => {
    await refetchActiveRooms();
    await refetchMyRoom();
    setIsModalOpen(false);
  };

  const myRoom = myBattleRoomData?.data;
  const activeRooms = activeRoomsData?.data;
  const hasMyRoom = myRoom && !Array.isArray(myRoom);
  const hasActiveRooms = Array.isArray(activeRooms) && activeRooms.length > 0;

  return (
    <div className="bg-sub-background min-h-screen max-w-screen-lg mx-auto px-3 py-6 sm:px-4 lg:px-6">
      <div className="w-full mx-auto space-y-5">
        {/* 🔥 게임 설명 섹션 (Accordion) */}
        <section
          className="bg-primary text-white rounded-lg border border-card-border"
          aria-label="퀴즈 대결 방법 안내"
        >
          <button
            onClick={() => setShowDescription((prev) => !prev)}
            className="w-full flex justify-between items-center p-4 font-bold text-xl focus:outline-none"
          >
            📌 퀴즈 배틀
            {showDescription ? (
              <ChevronUp className="w-6 h-6" />
            ) : (
              <ChevronDown className="w-6 h-6" />
            )}
          </button>
          <AnimatePresence initial={false}>
            {showDescription && (
              <motion.div
                initial={{ height: 0, opacity: 0 }}
                animate={{ height: "auto", opacity: 1 }}
                exit={{ height: 0, opacity: 0 }}
                transition={{ duration: 0.3 }}
                className="overflow-hidden p-4 pt-0 space-y-2 text-white text-sm"
              >
                <ol className="list-decimal list-inside space-y-1">
                  <li>
                    대결 참가: 참가하려는 대결을 선택하거나 새로운 대결을
                    만듭니다.
                  </li>
                  <li>
                    준비 완료: 대결방에 입장하면 <strong>준비 완료</strong>{" "}
                    버튼을 클릭하여 준비 상태로 변경합니다.
                  </li>
                  <li>
                    대결 시작: 모든 참가자가 준비 완료되면 대결이 자동으로
                    시작됩니다.
                  </li>
                  <li>
                    정답 제출: 문제를 풀어 빠르고 정확하게 답변을 제출하세요.
                    정답률과 응답 시간에 따라 점수가 부여됩니다.
                  </li>
                </ol>
              </motion.div>
            )}
          </AnimatePresence>
        </section>

        {/* 🏆 참여 중인 대결 섹션 */}
        <section
          className="bg-background p-6 rounded-lg border border-card-border transition-all"
          aria-label="참여 중인 퀴즈 대결"
        >
          <h2 className="text-xl font-bold border-b-2 border-primary pb-2 mb-2">
            🏆 참여 중인 배틀
          </h2>

          {isMyBattleRoomLoading ? (
            <p>로딩 중...</p>
          ) : hasMyRoom ? (
            <BattleRoomCard room={myRoom} />
          ) : (
            <div className="flex flex-col items-center gap-4">
              <p>현재 참여 중인 대결이 없습니다.</p>
              <Button
                variant="danger"
                size="medium"
                className="hover:scale-105 transition-transform duration-200 shadow-md hover:shadow-lg"
                onClick={() => setIsModalOpen(true)}
                aria-label="새로운 퀴즈 대결 생성하기"
              >
                새 대결 만들기
              </Button>
            </div>
          )}
        </section>

        {/* 🎯 활성화된 대결 목록 */}
        <section
          className="bg-background p-5 rounded-md border border-card-border h-[30rem] sm:h-[26rem]"
          aria-label="현재 활성화된 퀴즈 대결 목록"
        >
          <div className="flex justify-between items-center mb-3">
            <h2 className="text-lg font-bold border-b-2 border-primary pb-2">
              🎯 배틀 목록
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
      </div>
      {/* 🔹 새 배틀룸 생성 모달 */}
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
