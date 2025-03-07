import React from "react";
import Button from "@/app/_components/Button";

const BattleResultsPage: React.FC = () => {
  return (
    <div className="max-w-3xl mx-auto p-4 space-y-6">
      {/* 결과 헤더 */}
      <div className="bg-primary text-white p-6 rounded-lg shadow-md text-center">
        <h1 className="text-2xl font-bold">대결 결과</h1>
        <p className="opacity-80">최종 점수와 순위를 확인하세요</p>
      </div>

      {/* 우승자 섹션 */}
      <div className="bg-warning-light p-5 rounded-lg shadow-md text-center">
        <h2 className="text-lg font-bold text-warning">🏆 우승자</h2>
        <p className="text-lg font-semibold">사용자 1</p>
        <p className="text-gray-700">점수: 1000점</p>
      </div>

      {/* 순위표 */}
      <div className="bg-card p-5 rounded-lg shadow-md">
        <h2 className="text-lg font-bold border-b-2 border-primary pb-2 mb-4">
          순위
        </h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-center">
          {Array.from({ length: 5 }).map((_, index) => (
            <div
              key={index}
              className="bg-white p-4 rounded-lg shadow-sm border border-card-border"
            >
              <p className="text-lg font-semibold">사용자 {index + 1}</p>
              <p className="text-neutral">점수: {1000 - index * 100}점</p>
            </div>
          ))}
        </div>
      </div>

      {/* 버튼 */}
      <div className="flex justify-center">
        <Button variant="primary" size="large">
          배틀 목록으로
        </Button>
      </div>
    </div>
  );
};

export default BattleResultsPage;
