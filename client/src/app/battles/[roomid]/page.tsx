import React from "react";
import Button from "@/app/_components/Button";

const BattleRoomPage: React.FC = () => {
  return (
    <div className="max-w-4xl mx-auto p-6 space-y-6">
      {/* 배틀룸 정보 */}
      <div className="bg-primary text-white p-6 rounded-lg shadow-md">
        <h1 className="text-2xl font-bold">퀴즈 배틀 - 대기실</h1>
        <p className="opacity-80">대결이 시작되기를 기다리는 중입니다</p>
      </div>

      {/* 참가자 정보 */}
      <div className="bg-card p-6 rounded-lg shadow-md">
        <h2 className="text-xl font-bold border-b-2 border-primary pb-2 mb-4">
          참가자 목록
        </h2>
        <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
          {Array.from({ length: 6 }).map((_, index) => (
            <div
              key={index}
              className="bg-white p-4 rounded-lg shadow-md flex flex-col items-center border border-card-border"
            >
              <div className="w-20 h-20 bg-gray-300 rounded-full flex items-center justify-center text-2xl font-bold">
                U
              </div>
              <p className="mt-2 text-lg font-semibold">사용자 {index + 1}</p>
              <p className="text-neutral text-sm bg-gray-200 px-2 py-1 rounded">
                레벨 {index + 1}
              </p>
            </div>
          ))}
        </div>
      </div>

      {/* 버튼 */}
      <div className="flex justify-center gap-4">
        <Button variant="danger" size="large">
          나가기
        </Button>
        <Button variant="success" size="large">
          준비 완료
        </Button>
      </div>
    </div>
  );
};

export default BattleRoomPage;
