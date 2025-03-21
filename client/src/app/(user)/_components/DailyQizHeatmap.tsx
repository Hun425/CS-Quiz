"use client";

import {
  format,
  eachDayOfInterval,
  getYear,
  startOfYear,
  endOfYear,
  getMonth,
  subDays,
} from "date-fns";
import classNames from "classnames";
import { useMemo } from "react";

interface DailyQuizHeatmapProps {
  activityData?: Record<string, number>;
}

// ✅ Tailwind 색상 변수 적용
const LEVELS = [
  "bg-border", // 활동 없음 (연한 회색)
  "bg-primary/20 hover:shadow-md hover:scale-105 transition-transform", // 낮은 활동
  "bg-primary/50 hover:shadow-md hover:scale-105 transition-transform", // 중간 활동
  "bg-primary-hover hover:shadow-md hover:scale-105 transition-transform", // 높은 활동
  "bg-tertiary hover:shadow-md hover:scale-105 transition-transform", // 매우 높은 활동
];

// 📌 랜덤 활동 데이터 생성 함수
const generateRandomActivityData = () => {
  const activityData: Record<string, number> = {};
  const days = eachDayOfInterval({
    start: startOfYear(new Date()),
    end: endOfYear(new Date()),
  });

  days.forEach((date) => {
    if (Math.random() > 0.5) {
      activityData[format(date, "yyyy-MM-dd")] = Math.floor(Math.random() * 5);
    }
  });

  return activityData;
};

const DailyQuizHeatmap: React.FC<DailyQuizHeatmapProps> = ({
  activityData,
}) => {
  const currentYear = getYear(new Date());
  const startDate = startOfYear(new Date());
  const endDate = endOfYear(new Date());
  const today = format(new Date(), "yyyy-MM-dd");

  // 📌 랜덤 데이터 초기값을 useMemo로 캐싱하여 불필요한 렌더링 방지
  const defaultActivityData = useMemo(() => generateRandomActivityData(), []);
  const finalActivityData = activityData || defaultActivityData;

  // 📆 1년치 날짜 배열을 월별로 그룹화하여 성능 최적화
  const monthGroupedDays = useMemo(() => {
    return eachDayOfInterval({ start: startDate, end: endDate }).reduce(
      (acc, date) => {
        const formattedDate = format(date, "yyyy-MM-dd");
        const month = getMonth(date);

        if (!acc[month]) acc[month] = [];
        acc[month].push({ date: formattedDate });

        return acc;
      },
      {} as Record<number, { date: string }[]>
    );
  }, []);

  // 📆 최근 7일 활동 데이터 가져오기 (모바일 전용)
  const recentDays = useMemo(() => {
    return eachDayOfInterval({
      start: subDays(new Date(), 6),
      end: new Date(),
    }).map((date) => ({
      date: format(date, "yyyy-MM-dd"),
      activity: finalActivityData[format(date, "yyyy-MM-dd")] || 0,
    }));
  }, [finalActivityData]);

  return (
    <div className="flex flex-col items-center space-y-4 p-4">
      <h2 className="text-sm font-semibold text-primary">
        📆 {currentYear}년 퀴즈 활동
      </h2>

      {/* 🌐 데스크톱: 히트맵 */}
      <div className="hidden md:block w-full">
        <div className="grid grid-cols-12 gap-2 w-full max-w-5xl md:max-w-full">
          {Object.entries(monthGroupedDays).map(([month, days]) => (
            <div key={month} className="flex flex-col items-center space-y-1">
              <span className="text-xs text-muted">{Number(month) + 1}월</span>
              <div className="grid grid-cols-5 md:grid-cols-4 gap-1 md:gap-0.5">
                {days.map(({ date }) => {
                  const activityLevel = Math.min(
                    finalActivityData[date] || 0,
                    LEVELS.length - 1
                  );
                  const isToday = date === today;

                  return (
                    <div
                      key={date}
                      className={classNames(
                        "w-3 h-3 md:w-2 md:h-2 rounded-sm border transition-all",
                        LEVELS[activityLevel],
                        {
                          "border-2 border-primary shadow-md scale-105":
                            isToday,
                        }
                      )}
                      title={`날짜: ${date}, 퀴즈: ${
                        finalActivityData[date] || 0
                      }회`}
                    />
                  );
                })}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* 📱 모바일: 최근 7일 리스트 */}
      <div className="md:hidden w-full">
        <h3 className="text-sm font-semibold text-primary mb-2">
          📅 최근 7일 활동
        </h3>
        <ul className="space-y-2">
          {recentDays.map(({ date, activity }) => (
            <li
              key={date}
              className={classNames(
                "flex justify-between items-center p-2 rounded-md border border-border",
                activity > 0 ? "bg-primary/20" : "bg-border"
              )}
            >
              <span className="text-sm">{format(new Date(date), "MM/dd")}</span>
              <span className="text-sm font-semibold">{activity}회</span>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
};

export default DailyQuizHeatmap;
