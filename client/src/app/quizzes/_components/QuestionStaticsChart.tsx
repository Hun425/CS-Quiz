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
  statistics: QuestionStatistics[];
};

const getBarColor = (rate: number) => {
  if (rate >= 80) return "#4CAF50"; // green
  if (rate >= 50) return "#FFC107"; // yellow
  return "#F44336"; // red
};

const QuestionStatisticsChart: React.FC<Props> = ({ statistics }) => {
  const labels = statistics.map((_, idx) => `문제 ${idx + 1}`);

  const data = {
    labels,
    datasets: [
      {
        label: "정답률 (%)",
        data: statistics.map((q) => q.correctRate),
        backgroundColor: statistics.map((q) => getBarColor(q.correctRate)),
        borderRadius: 4,
        borderSkipped: false,
      },
    ],
  };

  const options: ChartOptions<"bar"> = {
    indexAxis: "y", // 가로 막대
    responsive: true,
    plugins: {
      legend: { display: false },
      tooltip: {
        callbacks: {
          label: (ctx) => {
            const stat = statistics[ctx.dataIndex];
            return [
              `정답률: ${stat.correctRate.toFixed(1)}%`,
              `시도 횟수: ${stat.totalAttempts}`,
              `평균 시간: ${stat.averageTimeSeconds.toFixed(1)}초`,
            ];
          },
        },
      },
      title: {
        display: true,
        text: "문제별 정답률",
        font: { size: 16 },
        padding: { bottom: 10 },
      },
    },
    scales: {
      x: {
        beginAtZero: true,
        max: 100,
        ticks: {
          callback: (val) => `${val}%`,
          color: "#6B7280",
        },
        grid: { display: false },
      },
      y: {
        ticks: {
          color: "#374151",
        },
        grid: { display: false },
      },
    },
  };

  return (
    <div className="w-full max-w-2xl mx-auto">
      <Bar data={data} options={options} />
    </div>
  );
};

export default QuestionStatisticsChart;
