import DailyQuizHeatmap from "../_components/DailyQizHeatmap";
import Dashboard from "../dashboard/Dashboard";
import Profile from "../_components/Profile";

const MyPage: React.FC = () => {
  return (
    <div className="max-w-5xl mx-auto p-6  border border-border ">
      {/* 프로필 */}
      <Profile />

      {/* 대시보드 접근 */}
      <div className="bg-card p-6 rounded-lg shadow-md">
        <DailyQuizHeatmap />
        <Dashboard />
      </div>
    </div>
  );
};

export default MyPage;
