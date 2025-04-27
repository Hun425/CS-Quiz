"use client";

import { BattleRoomResponse } from "@/lib/types/battle";

interface Props {
  battleRoom: BattleRoomResponse;
}

const BattleHeader: React.FC<Props> = ({ battleRoom }) => {
  return (
    <div className="space-y-2">
      <header
        className="bg-primary text-white px-6 py-6 rounded-xl flex flex-col gap-4"
        aria-label="퀴즈 대기실 헤더"
        role="banner"
      >
        {/* 타이틀 + Room Code */}
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2">
          <h1
            className="text-2xl text-white font-bold tracking-tight truncate"
            aria-label={`퀴즈 제목: ${battleRoom.quizTitle} 대기실`}
          >
            {battleRoom.quizTitle}
          </h1>

          <p
            className="text-sm font-medium bg-white/10 px-3 py-1 rounded-md border border-white/20 inline-block"
            aria-label={`방 코드: ${battleRoom.roomCode}`}
          >
            Room Code: <strong>{battleRoom.roomCode}</strong>
          </p>
        </div>

        {/* 안내 텍스트 */}
        <div className="flex flex-col gap-1 text-sm">
          <p className="text-sm text-white/80">
            ⚡{" "}
            <strong className="text-yellow-300">
              모든 참가자가 준비 완료되면
            </strong>{" "}
            5초 후 자동으로 시작됩니다.
          </p>
          <p className="text-sm text-white/80">
            ✅ 하단의{" "}
            <span className="text-yellow-300 font-semibold">[준비 완료]</span>{" "}
            버튼을 눌러주세요.
          </p>
          <p className="text-sm text-white/80">
            🚪 화면을 이탈하면{" "}
            <span className="text-yellow-300 font-semibold">자동으로 퇴장</span>{" "}
            처리됩니다.
          </p>
        </div>
      </header>
    </div>
  );
};

export default BattleHeader;
