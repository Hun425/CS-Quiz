"use client";

import {
  PlayCircle,
  Star,
  Flame,
  Trophy,
  Crown,
  CalendarCheck,
  Zap,
  BookOpen,
  HelpCircle,
} from "lucide-react";

export type Achievement = {
  id: number;
  name: string;
  description: string;
  iconUrl?: string;
  progress?: number;
  earnedAt?: string | null;
};

// 이름 기반 아이콘 매핑
const iconMap: Record<string, React.ReactNode> = {
  "첫 퀴즈 완료": <PlayCircle className="text-blue-500" />,
  "완벽한 점수": <Star className="text-yellow-500" />,
  "3연승": <Flame className="text-red-500" />,
  "5연승": <Trophy className="text-orange-500" />,
  "10연승": <Crown className="text-purple-600" />,
  "데일리 퀴즈 마스터": <CalendarCheck className="text-green-600" />,
  "빠른 해결사": <Zap className="text-pink-500" />,
  "지식 탐구자": <BookOpen className="text-indigo-500" />,
};

type Props = {
  achievement: Achievement;
};

const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  return `${date.getFullYear()}.${date.getMonth() + 1}.${date.getDate()}`;
};

const AchievementBadge = ({ achievement }: Props) => {
  const isEarned = Boolean(achievement.earnedAt);

  return (
    <div
      className={`
      flex flex-col sm:flex-row sm:items-center
      gap-1 sm:gap-4
      rounded-xl p-3 sm:p-4
      border shadow-sm transition
      ${
        isEarned
          ? "bg-white border-green-300"
          : "bg-subbackground text-muted-foreground"
      }
    `}
    >
      {/* 아이콘 */}
      <div className="w-7 h-7 shrink-0">
        {iconMap[achievement.name] ?? <HelpCircle className="text-gray-400" />}
      </div>

      {/* 텍스트 */}
      <div className="flex flex-col flex-1">
        <span className="text-sm font-semibold leading-snug">
          {achievement.name}
        </span>
        <span className="text-xs text-muted-foreground leading-snug line-clamp-2">
          {achievement.description}
        </span>
      </div>

      {/* 상태 */}
      <div className="text-[11px] font-medium text-right min-w-[72px] sm:ml-auto sm:text-xs leading-tight">
        {isEarned ? (
          <>
            획득 완료 ✅
            <br />
            <span className="text-muted-foreground">
              {formatDate(achievement.earnedAt!)}
            </span>
          </>
        ) : (
          <>
            {achievement.progress ?? 0}%<br />
            진행중
          </>
        )}
      </div>
    </div>
  );
};

export default AchievementBadge;
