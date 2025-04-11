"use client";

import { Bar } from "react-chartjs-2";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
} from "chart.js";
import React from "react";

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend
);

type DifficultyChartProps = {
  data: { name: string; value: number }[];
};

const DifficultyChart: React.FC<DifficultyChartProps> = ({ data }) => {
  if (!data || data.length === 0) {
    return <p className="text-gray-500 mt-2 text-sm">데이터가 없습니다.</p>;
  }

  const chartData = {
    labels: data.map((item) => item.name),
    datasets: [
      {
        label: "문제 수",
        data: data.map((item) => item.value),
        backgroundColor: ["#4CAF50", "#FF9800", "#F44336"],
      },
    ],
  };

  return (
    <div className="w-full h-24">
      <Bar
        data={chartData}
        options={{
          responsive: true,
          indexAxis: "y",
          maintainAspectRatio: false,
        }}
      />
    </div>
  );
};

export default DifficultyChart;
