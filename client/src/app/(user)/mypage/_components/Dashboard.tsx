"use client";

import {
  BarChart,
  Medal,
  Activity,
  TrendingDown,
  TrendingUp,
} from "lucide-react";
import Image from "next/image";

import { useUserAchievements } from "@/lib/api/user/useUserAchievements";
import { useUserStatistics } from "@/lib/api/user/useUserStatistic";
import { useUserTopicPerformance } from "@/lib/api/user/useUserTopicPerformance";
import { useUserRecentActivities } from "@/lib/api/user/useUserRecentActivities";
import Skeleton from "@/app/_components/Skeleton";

const Dashboard: React.FC = () => {
  const { data: statistics, isLoading: isLoadingStats } = useUserStatistics();
  const { data: activities, isLoading: isLoadingActivities } =
    useUserRecentActivities();
  const { data: achievements, isLoading: isLoadingAchievements } =
    useUserAchievements();
  const { data: topicPerformance, isLoading: isLoadingTopics } =
    useUserTopicPerformance();

  return (
    <div className="max-w-5xl mx-auto p-6 space-y-6">
      {/* 🔹 퀴즈 통계 */}
      <div className="bg-background p-4 rounded-lg shadow-sm">
        <h2 className="text-lg font-semibold mb-3 flex items-center gap-2">
          <BarChart className="w-5 h-5" /> 퀴즈 통계
        </h2>
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
      </div>

      {/* 🔹 최근 활동 */}
      <div className="bg-background p-4 rounded-lg shadow-sm mt-4">
        <h2 className="text-lg font-semibold mb-3 flex items-center gap-2">
          <Activity className="w-5 h-5" /> 최근 활동
        </h2>
        {isLoadingActivities ? (
          <Skeleton />
        ) : activities && activities.length > 0 ? (
          <ul className="space-y-2">
            {activities.map((activity) => (
              <li key={activity.id} className="p-2 border-b border-gray-200">
                {activity.type === "QUIZ_ATTEMPT"
                  ? `퀴즈 "${activity.quizTitle}" 시도 - 점수: ${activity.score}`
                  : activity.type === "ACHIEVEMENT_EARNED"
                  ? `업적 달성: ${activity.achievementName}`
                  : `레벨업! 새로운 레벨: ${activity.newLevel}`}
                <span className="text-gray-500 text-sm">
                  {" "}
                  ({activity.timestamp})
                </span>
              </li>
            ))}
          </ul>
        ) : (
          <p className="text-muted">최근 활동이 없습니다.</p>
        )}
      </div>

      {/* 🔹 강점 & 약점 태그 (Topic Performance) */}
      <div className="bg-background p-4 rounded-lg shadow-sm mt-4">
        <h2 className="text-lg font-semibold mb-3 flex items-center gap-2">
          <TrendingUp className="w-5 h-5 text-green-600" /> 강점 태그 vs 약점
          태그
        </h2>
        {isLoadingTopics ? (
          <Skeleton />
        ) : topicPerformance ? (
          <div className="grid grid-cols-2 gap-4">
            {/* 강점 태그 */}
            <div>
              <h3 className="text-md font-semibold flex items-center gap-2 text-green-600">
                <TrendingUp className="w-4 h-4" /> 강점 태그
              </h3>
              {topicPerformance.filter((tp) => tp.strength).length > 0 ? (
                <ul className="mt-2">
                  {topicPerformance
                    .filter((tp) => tp.strength)
                    .map((topic) => (
                      <li key={topic.tagId} className="text-sm">
                        ✅ {topic.tagName} (정답률: {topic.correctRate}%)
                      </li>
                    ))}
                </ul>
              ) : (
                <p className="text-muted text-sm">강점 태그가 없습니다.</p>
              )}
            </div>

            {/* 약점 태그 */}
            <div>
              <h3 className="text-md font-semibold flex items-center gap-2 text-red-600">
                <TrendingDown className="w-4 h-4" /> 약점 태그
              </h3>
              {topicPerformance.filter((tp) => !tp.strength).length > 0 ? (
                <ul className="mt-2">
                  {topicPerformance
                    .filter((tp) => !tp.strength)
                    .map((topic) => (
                      <li key={topic.tagId} className="text-sm">
                        ❌ {topic.tagName} (정답률: {topic.correctRate}%)
                      </li>
                    ))}
                </ul>
              ) : (
                <p className="text-muted text-sm">약점 태그가 없습니다.</p>
              )}
            </div>
          </div>
        ) : (
          <p className="text-muted">
            태그별 퍼포먼스 데이터를 불러올 수 없습니다.
          </p>
        )}
      </div>

      {/* 🔹 업적 */}
      <div className="bg-background p-4 rounded-lg shadow-sm mt-4">
        <h2 className="text-lg font-semibold mb-3 flex items-center gap-2">
          <Medal className="w-5 h-5" /> 업적
        </h2>
        {isLoadingAchievements ? (
          <Skeleton />
        ) : achievements && achievements.length > 0 ? (
          <ul className="grid grid-cols-2 gap-4">
            {achievements.map((ach) => (
              <li key={ach.id} className="flex items-center gap-3">
                <Image src={ach.iconUrl} alt={ach.name} className="w-10 h-10" />
                <div>
                  <p className="font-semibold">{ach.name}</p>
                  <p className="text-sm text-muted">{ach.description}</p>
                  <p className="text-xs text-gray-500">
                    {ach.earnedAt
                      ? `획득: ${ach.earnedAt}`
                      : `진행도: ${ach.progress}%`}
                  </p>
                </div>
              </li>
            ))}
          </ul>
        ) : (
          <p className="text-muted">획득한 업적이 없습니다.</p>
        )}
      </div>
    </div>
  );
};

export default Dashboard;
