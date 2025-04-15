"use client";

import {
  BarChart,
  Medal,
  Activity,
  TrendingDown,
  TrendingUp,
} from "lucide-react";

import { useUserAchievements } from "@/lib/api/user/useUserAchievements";
import { useUserStatistics } from "@/lib/api/user/useUserStatistic";
import { useUserTopicPerformance } from "@/lib/api/user/useUserTopicPerformance";
import { useUserRecentActivities } from "@/lib/api/user/useUserRecentActivities";
import AchievementBadge from "@/app/_components/AchivementBadge";
import TagPerformanceList from "./TagPerformanceList";
import Skeleton from "@/app/_components/Skeleton";
import { ActivityResponse, ActivityType } from "@/lib/types/user";

interface DashboardProps {
  userId?: number;
}

const activityMessageMap: Record<
  ActivityType,
  (activity: ActivityResponse) => string
> = {
  QUIZ_ATTEMPT: (a) =>
    `퀴즈 "${a.quizTitle ?? "제목 없는 퀴즈"}" 시도 - 점수: ${
      a.score ?? "미정"
    }`,
  ACHIEVEMENT_EARNED: (a) =>
    `업적 달성: ${a.achievementName ?? "이름 없는 업적"}`,
  LEVEL_UP: (a) => `레벨업! 새로운 레벨: ${a.newLevel ?? "레벨 정보 없음"}`,
};

const Dashboard: React.FC<DashboardProps> = ({ userId }) => {
  const { data: statistics, isLoading: isLoadingStats } =
    useUserStatistics(userId);
  const { data: activities, isLoading: isLoadingActivities } =
    useUserRecentActivities(userId);
  const { data: achievements, isLoading: isLoadingAchievements } =
    useUserAchievements(userId);
  const { data: topicPerformance, isLoading: isLoadingTopics } =
    useUserTopicPerformance(userId);

  console.log("Dashboard - Statistics:", statistics);
  console.log("Dashboard - Activities:", activities);
  const SectionWrapper = ({
    title,
    icon,
    ariaLabel,
    children,
  }: {
    title: string;
    icon: React.ReactNode;
    ariaLabel: string;
    children: React.ReactNode;
  }) => (
    <section className="bg-background p-4 space-y-3" aria-label={ariaLabel}>
      <h2 className="text-lg font-semibold flex items-center gap-2 border-b-2 border-primary pb-2 mb-2">
        {icon} {title}
      </h2>
      {children}
    </section>
  );

  return (
    <div className="max-w-5xl mx-auto p-6 space-y-6">
      {/* 🔹 퀴즈 통계 */}
      <SectionWrapper
        title="퀴즈 통계"
        icon={<BarChart className="w-5 h-5" />}
        ariaLabel="사용자의 퀴즈 통계 섹션"
      >
        {isLoadingStats ? (
          <Skeleton />
        ) : statistics ? (
          <ul className="grid grid-cols-2 gap-4">
            <li>
              총 푼 퀴즈 수: <strong>{statistics.totalQuizzesCompleted}</strong>
            </li>
            <li>
              완료한 퀴즈 수:{" "}
              <strong>{statistics.totalQuizzesCompleted}</strong>
            </li>
            <li>
              평균 점수: <strong>{statistics.averageScore.toFixed(1)}</strong>
            </li>
            <li>
              정답률: <strong>{statistics.correctRate.toFixed(1)}%</strong>
            </li>
          </ul>
        ) : (
          <p className="text-muted">통계 데이터를 불러올 수 없습니다.</p>
        )}
      </SectionWrapper>
      {/* 🔹 최근 활동 */}
      <SectionWrapper
        title="최근 활동"
        icon={<Activity className="w-5 h-5" />}
        ariaLabel="사용자의 최근 퀴즈 활동 섹션"
      >
        {isLoadingActivities ? (
          <Skeleton />
        ) : activities && activities.length > 0 ? (
          <ul className="space-y-2">
            {activities.slice(0, 5).map((activity) => {
              const message =
                activityMessageMap[activity.type as ActivityType]?.(activity) ??
                "기록되지 않은 활동";

              return (
                <li
                  key={activity.id + activity.type}
                  className="p-1 border-b border-gray-200 text-sm"
                >
                  {message}
                  {activity.timestamp && (
                    <span className="text-gray-500 text-xs ml-1">
                      (
                      {new Date(activity.timestamp).toLocaleDateString("ko-KR")}
                      )
                    </span>
                  )}
                </li>
              );
            })}
          </ul>
        ) : (
          <p className="text-muted">최근 활동이 없습니다.</p>
        )}
      </SectionWrapper>

      {/* 🔹 강점 & 약점 태그 */}
      <SectionWrapper
        title="강점 태그 vs 약점 태그"
        icon={<TrendingUp className="w-5 h-5 text-green-600" />}
        ariaLabel="사용자의 태그별 강점과 약점 섹션"
      >
        {isLoadingTopics ? (
          <Skeleton />
        ) : topicPerformance ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <TagPerformanceList
              title="강점 태그"
              icon={<TrendingUp className="w-4 h-4" />}
              colorClass="text-green-600"
              isStrength
              items={topicPerformance}
            />
            <TagPerformanceList
              title="약점 태그"
              icon={<TrendingDown className="w-4 h-4" />}
              colorClass="text-red-600"
              isStrength={false}
              items={topicPerformance}
            />
          </div>
        ) : (
          <p className="text-muted">
            태그별 퍼포먼스 데이터를 불러올 수 없습니다.
          </p>
        )}
      </SectionWrapper>

      {/* 🔹 업적 */}
      <SectionWrapper
        title="업적"
        icon={<Medal className="w-5 h-5" />}
        ariaLabel="사용자의 퀴즈 업적 섹션"
      >
        {isLoadingAchievements ? (
          <Skeleton />
        ) : achievements && achievements.length > 0 ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {achievements.map((ach) => (
              <AchievementBadge key={ach.id} achievement={ach} />
            ))}
          </div>
        ) : (
          <p className="text-muted">획득한 업적이 없습니다.</p>
        )}
      </SectionWrapper>
    </div>
  );
};

export default Dashboard;
