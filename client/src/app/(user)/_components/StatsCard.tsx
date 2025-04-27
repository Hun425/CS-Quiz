const StatsCard: React.FC<{
  completedQuizzes: number;
  totalStudyTime: string;
  accuracyRate: string;
}> = ({ completedQuizzes, totalStudyTime, accuracyRate }) => {
  return (
    <div className="bg-card p-6 rounded-lg shadow-md">
      <h2 className="text-xl font-bold border-b-2 border-primary pb-2">
        학습 진행 상황
      </h2>
      <div className="mt-4 flex justify-around text-center">
        <div>
          <p className="text-2xl font-bold text-primary">{completedQuizzes}</p>
          <p className="text-neutral">완료한 문제</p>
        </div>
        <div>
          <p className="text-2xl font-bold text-success">{totalStudyTime}</p>
          <p className="text-neutral">총 학습 시간</p>
        </div>
        <div>
          <p className="text-2xl font-bold text-warning">{accuracyRate}</p>
          <p className="text-neutral">정답률</p>
        </div>
      </div>
    </div>
  );
};
export default StatsCard;
