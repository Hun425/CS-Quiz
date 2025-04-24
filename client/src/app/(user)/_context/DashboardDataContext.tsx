import { createContext, useContext } from "react";

interface DashboardData {
  completedQuizzes: number;
  totalStudyTime: string;
  accuracyRate: string;
  weeklyGoal: string;
  currentProgress: string;
  growthLogs: string[];
  skills: string[];
  roadmap: string;
  recommendedJob: string;
}

const DashboardDataContext = createContext<DashboardData | null>(null);

export const useDashboardData = () => {
  const context = useContext(DashboardDataContext);
  if (!context) {
    throw new Error(
      "useDashboardData must be used within a DashboardDataProvider"
    );
  }
  return context;
};

export default DashboardDataContext;
