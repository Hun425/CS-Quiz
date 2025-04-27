"use client";

import { Bar } from "react-chartjs-2";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Tooltip,
} from "chart.js";
import { QuizDifficultyType } from "@/lib/types/quiz";

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip);

type DifficultyChartProps = {
  distribution?: Partial<Record<QuizDifficultyType, number>>;
};

const DifficultyChart: React.FC<DifficultyChartProps> = ({
  distribution = {},
}) => {
  const isDummy = Object.values(distribution).every((v) => v === undefined);

  const BEGINNER = distribution.BEGINNER ?? 30;
  const INTERMEDIATE = distribution.INTERMEDIATE ?? 40;
  const ADVANCED = distribution.ADVANCED ?? 30;

  const total = BEGINNER + INTERMEDIATE + ADVANCED;

  // ✅ 퍼센트로 계산
  const beginnerPercent = total ? (BEGINNER / total) * 100 : 0;
  const intermediatePercent = total ? (INTERMEDIATE / total) * 100 : 0;
  const advancedPercent = total ? (ADVANCED / total) * 100 : 0;

  const chartData = {
    labels: [""],
    datasets: [
      {
        label: `쉬움 (${beginnerPercent.toFixed(1)}%)`,
        data: [beginnerPercent],
        backgroundColor: isDummy ? "rgba(76, 175, 80, 0.3)" : "#4CAF50",
      },
      {
        label: `보통 (${intermediatePercent.toFixed(1)}%)`,
        data: [intermediatePercent],
        backgroundColor: isDummy ? "rgba(255, 152, 0, 0.3)" : "#FF9800",
      },
      {
        label: `어려움 (${advancedPercent.toFixed(1)}%)`,
        data: [advancedPercent],
        backgroundColor: isDummy ? "rgba(244, 67, 54, 0.3)" : "#F44336",
      },
    ],
  };

  return (
    <div className="w-full h-24 space-y-2">
      {isDummy && (
        <p className="text-muted-foreground text-xs">
          데이터가 없어 기본 비율로 표시됩니다.
        </p>
      )}
      <Bar
        data={chartData}
        options={{
          indexAxis: "y",
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            x: {
              stacked: true,
              beginAtZero: true,
              max: 100,
              ticks: {
                callback: (value) => `${value}%`,
                color: "#6B7280",
              },
              grid: { display: false },
            },
            y: {
              stacked: true,
              ticks: { display: false },
              grid: { display: false },
            },
          },
          plugins: {
            legend: {
              display: true,
              position: "bottom",
              labels: {
                font: { size: 12 },
              },
            },
            tooltip: {
              callbacks: {
                label: (ctx) => {
                  const value =
                    typeof ctx.raw === "number" ? ctx.raw : Number(ctx.raw);
                  const percent = value.toFixed(1);
                  return `${ctx.dataset.label}: ${percent}%${
                    value === 0 ? " (예시)" : ""
                  }`;
                },
              },
            },
          },
        }}
      />
    </div>
  );
};

export default DifficultyChart;
