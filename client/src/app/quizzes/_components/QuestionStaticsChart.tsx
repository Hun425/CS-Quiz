"use client";

import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Tooltip,
  Title,
  ChartOptions,
} from "chart.js";
import { Bar } from "react-chartjs-2";
import { QuestionStatistics } from "@/lib/types/quiz";

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, Title);

type Props = {
  statistics?: QuestionStatistics[];
};

const QuestionStatisticsChart: React.FC<Props> = ({ statistics = [] }) => {
  const isDummy = statistics.length === 0;

  const dummyStats: QuestionStatistics[] = Array.from({ length: 5 }).map(
    (_, i) => ({
      questionId: i + 1,
      correctAnswers: 0,
      totalAttempts: 0,
      correctRate: 70 - i * 10,
      averageTimeSeconds: 10 + i * 5,
    })
  );

  const chartStats = isDummy ? dummyStats : statistics;
  const labels = chartStats.map((_, idx) => `문제 ${idx + 1}`);

  const data = {
    labels,
    datasets: [
      {
        label: "정답률 (%)",
        data: chartStats.map((q) => q.correctRate),
        backgroundColor: isDummy ? "#D1D5DB" : "#60A5FA", // blue-400
        borderRadius: 6,
        borderSkipped: "bottom" as const,
        barPercentage: 0.5,
        yAxisID: "y",
      },
      {
        label: "시도 횟수",
        data: chartStats.map((q) => q.totalAttempts),
        backgroundColor: isDummy ? "#D1D5DB" : "#34D399", // emerald-400
        borderRadius: 6,
        borderSkipped: "bottom" as const,
        barPercentage: 0.5,
        yAxisID: "y1",
      },
      {
        label: "평균 시간 (초)",
        data: chartStats.map((q) => q.averageTimeSeconds),
        backgroundColor: isDummy ? "#D1D5DB" : "#FBBF24", // amber-400
        borderRadius: 6,
        borderSkipped: "bottom" as const,
        barPercentage: 0.5,
        yAxisID: "y1",
      },
    ],
  };

  const options: ChartOptions<"bar"> = {
    responsive: true,
    plugins: {
      legend: {
        display: true,
        position: "top",
        labels: {
          color: "#374151",
          font: { size: 12 },
        },
      },
      tooltip: {
        callbacks: {
          label: (ctx) => {
            const stat = chartStats[ctx.dataIndex];
            const label = ctx.dataset.label;

            if (isDummy) return "예시 데이터입니다";

            switch (label) {
              case "정답률 (%)":
                return `정답률: ${stat.correctRate.toFixed(1)}%`;
              case "시도 횟수":
                return `시도 횟수: ${stat.totalAttempts}`;
              case "평균 시간 (초)":
                return `평균 시간: ${stat.averageTimeSeconds.toFixed(1)}초`;
              default:
                return "";
            }
          },
        },
      },
      title: {
        display: true,
        text: isDummy ? "문제별 통계 (예시)" : "문제별 통계",
        font: { size: 14 },
        color: isDummy ? "#9CA3AF" : "#111827",
        padding: { bottom: 8 },
      },
    },
    scales: {
      x: {
        ticks: {
          color: "#1F2937",
          font: { size: 12 },
        },
        grid: {
          color: "#E5E7EB",
          drawTicks: false,
        },
      },
      y: {
        beginAtZero: true,
        max: 100,
        title: {
          display: true,
          text: "정답률 (%)",
          color: "#60A5FA",
          font: { size: 12 },
        },
        ticks: {
          callback: (val) => `${val}%`,
          color: "#4B5563",
          font: { size: 12 },
        },
        grid: {
          color: "#F3F4F6",
          drawTicks: false,
        },
      },
      y1: {
        beginAtZero: true,
        position: "right",
        title: {
          display: true,
          text: "시도 횟수 / 시간",
          color: "#10B981",
          font: { size: 12 },
        },
        ticks: {
          color: "#6B7280",
          font: { size: 12 },
        },
        grid: {
          drawOnChartArea: false,
        },
      },
    },
  };

  return (
    <div className="w-full min-h-64">
      <Bar data={data} options={options} />
      {isDummy && (
        <p className="text-sm text-gray-400 mt-2 text-center">
          실제 데이터가 없어 예시 차트로 표시됩니다.
        </p>
      )}
    </div>
  );
};

export default QuestionStatisticsChart;
