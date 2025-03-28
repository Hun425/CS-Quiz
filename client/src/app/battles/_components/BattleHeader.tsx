interface BattleHeaderProps {
  quizTitle: string;
  roomCode: string;
}

const BattleHeader = ({ quizTitle, roomCode }: BattleHeaderProps) => {
  return (
    <header
      className="bg-primary text-white px-6 py-4 rounded-xl shadow-lg flex flex-col sm:flex-row sm:items-center sm:justify-between transition-all"
      aria-label="퀴즈 대기실 헤더"
      role="banner"
    >
      <h1
        className="text-xl sm:text-2xl font-bold tracking-tight text-white"
        aria-label={`퀴즈 제목: ${quizTitle}`}
      >
        {quizTitle}
      </h1>

      <span
        className="mt-2 sm:mt-0 text-sm bg-white/10 text-white px-3 py-1 rounded-md border border-white/20"
        aria-label={`방 코드: ${roomCode}`}
      >
        Room Code: <strong>{roomCode}</strong>
      </span>
    </header>
  );
};

export default BattleHeader;
