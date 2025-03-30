import { BattleRoomResponse } from "@/lib/types/battle";

interface Props {
  battleRoom: BattleRoomResponse;
}

const BattleHeader: React.FC<Props> = ({ battleRoom }) => {
  return (
    <div className="space-y-2">
      <header
        className="bg-primary text-white px-6 py-4 rounded-xl shadow-lg flex flex-col sm:flex-row sm:items-center sm:justify-between transition-all"
        aria-label="퀴즈 대기실 헤더"
        role="banner"
      >
        <h1
          className="text-xl sm:text-2xl font-bold tracking-tight text-white"
          aria-label={`퀴즈 제목: ${battleRoom.quizTitle} 대기실`}
        >
          {battleRoom.quizTitle}
        </h1>

        <span
          className="mt-2 sm:mt-0 text-sm bg-white/10 text-white px-3 py-1 rounded-md border border-white/20"
          aria-label={`방 코드: ${battleRoom.roomCode}`}
        >
          Room Code: <strong>{battleRoom.roomCode}</strong>
        </span>
      </header>

      <div className="bg-yellow-50 border-l-4 border-yellow-400 text-yellow-800 px-4 py-3 rounded-md shadow-sm">
        <p className="text-sm font-medium">
          ⚠️ 모든 참가자가 준비 완료되면 대결이 <strong>자동으로 시작</strong>
          됩니다.
        </p>
        <p className="text-sm mt-1">
          하단의{" "}
          <span className="font-semibold text-yellow-900">[✅ 준비 완료]</span>{" "}
          버튼을 눌러 대결을 준비하세요!
        </p>
      </div>
    </div>
  );
};

export default BattleHeader;
