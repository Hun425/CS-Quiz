import { Calendar } from "lucide-react";

const WeeklyGoal: React.FC<{ weeklyGoal: string; currentProgress: string }> = ({
  weeklyGoal,
  currentProgress,
}) => {
  return (
    <div className="bg-card p-6 rounded-lg shadow-md flex items-center gap-4">
      <Calendar size={40} className="text-primary" />
      <div>
        <h3 className="text-lg font-bold">주간 학습 목표</h3>
        <p className="text-neutral">이번 주 목표: {weeklyGoal}</p>
        <p className="text-success">현재 진행: {currentProgress}</p>
      </div>
    </div>
  );
};
export default WeeklyGoal;
