"use client";

import {
  format,
  eachDayOfInterval,
  getYear,
  startOfYear,
  endOfYear,
  getMonth,
} from "date-fns";
import classNames from "classnames";

interface DailyQuizHeatmapProps {
  activityData?: Record<string, number>;
}

const LEVELS = [
  "bg-gray-200", // 활동 없음
  "bg-green-200", // 낮은 활동
  "bg-green-400", // 중간 활동
  "bg-green-600", // 높은 활동
  "bg-green-800", // 매우 높은 활동
];

// 📌 랜덤 활동 데이터 생성 함수
const generateRandomActivityData = () => {
  const activityData: Record<string, number> = {};
  const days = eachDayOfInterval({
    start: startOfYear(new Date()),
    end: endOfYear(new Date()),
  });

  days.forEach((date) => {
    // 50% 확률로 활동 추가 (0~4 사이 랜덤 값)
    if (Math.random() > 0.5) {
      activityData[format(date, "yyyy-MM-dd")] = Math.floor(Math.random() * 5);
    }
  });

  return activityData;
};

const DailyQuizHeatmap: React.FC<DailyQuizHeatmapProps> = ({
  activityData = generateRandomActivityData(), // 랜덤 데이터 기본값
}) => {
  const currentYear = getYear(new Date());
  const startDate = startOfYear(new Date());
  const endDate = endOfYear(new Date());

  // 1년치 날짜 배열 생성
  const days = eachDayOfInterval({ start: startDate, end: endDate }).map(
    (date) => ({
      date: format(date, "yyyy-MM-dd"),
      month: getMonth(date), // 월 정보 저장
    })
  );

  // 오늘 날짜
  const today = format(new Date(), "yyyy-MM-dd");

  return (
    <div className="flex flex-col items-center space-y-4">
      <h2 className="text-sm font-semibold">📆 {currentYear}년 퀴즈 활동</h2>

      {/* 월별로 구분하여 표시 */}
      <div className="grid grid-cols-12 gap-2 w-full max-w-5xl">
        {Array.from({ length: 12 }, (_, month) => (
          <div key={month} className="flex flex-col items-center space-y-1">
            <span className="text-xs text-muted">{month + 1}월</span>
            <div className="grid grid-cols-5 gap-1">
              {days
                .filter((d) => d.month === month)
                .map(({ date }) => {
                  const activityLevel = Math.min(
                    activityData[date] || 0,
                    LEVELS.length - 1
                  );
                  const isToday = date === today;

                  return (
                    <div
                      key={date}
                      className={classNames(
                        "w-3 h-3 rounded-sm transition-all",
                        LEVELS[activityLevel],
                        { "border border-primary": isToday }
                      )}
                      title={`날짜: ${date}, 퀴즈: ${
                        activityData[date] || 0
                      }회`}
                    />
                  );
                })}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default DailyQuizHeatmap;
