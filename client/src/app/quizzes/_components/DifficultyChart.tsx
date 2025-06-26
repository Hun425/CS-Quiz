"use client";

import { Doughnut } from "react-chartjs-2";
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from "chart.js";
import { QuizDifficultyType } from "@/lib/types/quiz";

ChartJS.register(ArcElement, Tooltip, Legend);

type DifficultyChartProps = {
  distribution?: Partial<Record<QuizDifficultyType, number>>;
  totalCount?: number;
};

const DifficultyChart: React.FC<DifficultyChartProps> = ({
  distribution = {},
  totalCount,
}) => {
  const isDummy =
    distribution.BEGINNER === undefined &&
    distribution.INTERMEDIATE === undefined &&
    distribution.ADVANCED === undefined;

  const BEGINNER = distribution.BEGINNER ?? (isDummy ? 3 : 0);
  const INTERMEDIATE = distribution.INTERMEDIATE ?? (isDummy ? 4 : 0);
  const ADVANCED = distribution.ADVANCED ?? (isDummy ? 3 : 0);

  const total = totalCount ?? BEGINNER + INTERMEDIATE + ADVANCED;

  const chartLabels: string[] = [];
  const chartValues: number[] = [];
  const chartColors: string[] = [];

  if (BEGINNER > 0) {
    chartLabels.push("쉬움");
    chartValues.push(BEGINNER);
    chartColors.push(isDummy ? "rgba(76, 175, 80, 0.3)" : "#4CAF50");
  }
  if (INTERMEDIATE > 0) {
    chartLabels.push("보통");
    chartValues.push(INTERMEDIATE);
    chartColors.push(isDummy ? "rgba(255, 152, 0, 0.3)" : "#FF9800");
  }
  if (ADVANCED > 0) {
    chartLabels.push("어려움");
    chartValues.push(ADVANCED);
    chartColors.push(isDummy ? "rgba(244, 67, 54, 0.3)" : "#F44336");
  }

  const chartData = {
    labels: chartLabels,
    datasets: [
      {
        data: chartValues,
        backgroundColor: chartColors,
        borderWidth: 1,
      },
    ],
  };

  return (
    <div className="w-full flex flex-col items-center">
      {isDummy && (
        <p className="text-muted-foreground text-sm mb-2">
          통계 데이터가 없어 기본 예시 비율로 표시됩니다.
        </p>
      )}
      <div className="w-[200px] sm:w-[240px]">
        <Doughnut
          data={chartData}
          options={{
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: {
                position: "bottom",
                labels: {
                  font: {
                    size: 12,
                  },
                },
              },
              tooltip: {
                callbacks: {
                  label: (ctx) => {
                    const label = ctx.label || "";
                    const value = Number(ctx.raw);
                    const percent = total
                      ? ((value / total) * 100).toFixed(1)
                      : "0.0";
                    return `${label}: ${value}개 (${percent}%)${
                      isDummy ? " (예시)" : ""
                    }`;
                  },
                },
              },
            },
          }}
        />
      </div>
    </div>
  );
};

export default DifficultyChart;
