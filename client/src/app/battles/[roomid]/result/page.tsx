import React from "react";
import Button from "@/app/_components/Button";

const mockResults = [
  {
    userId: 1,
    username: "사용자 1",
    finalScore: 1000,
    correctAnswers: 10,
    experienceGained: 500,
    isWinner: true,
  },
  {
    userId: 2,
    username: "사용자 2",
    finalScore: 900,
    correctAnswers: 9,
    experienceGained: 450,
    isWinner: false,
  },
  {
    userId: 3,
    username: "사용자 3",
    finalScore: 850,
    correctAnswers: 8,
    experienceGained: 400,
    isWinner: false,
  },
  {
    userId: 4,
    username: "사용자 4",
    finalScore: 800,
    correctAnswers: 7,
    experienceGained: 350,
    isWinner: false,
  },
];

const BattleResultsPage: React.FC = () => {
  const winner = mockResults[0];
  const others = mockResults.slice(1);

  return (
    <div className="max-w-4xl mx-auto p-4 space-y-8 min-h-screen bg-background">
      {/* 결과 헤더 */}
      <div className="bg-primary text-white p-6 rounded-xl shadow text-center">
        <h1 className="text-2xl font-bold">🎉 대결 결과</h1>
        <p className="opacity-80 text-sm mt-1">최종 점수와 순위를 확인하세요</p>
      </div>

      {/* 우승자 섹션 */}
      <div className="bg-yellow-100 dark:bg-warning-light p-6 rounded-xl text-center shadow-md">
        <h2 className="text-lg font-bold text-warning mb-2">🏆 우승자</h2>
        <p className="text-xl font-semibold text-foreground">
          {winner.username}
        </p>
        <p className="text-muted-foreground text-sm mt-1">
          점수: {winner.finalScore}점 · 정답: {winner.correctAnswers}개 · 경험치
          +{winner.experienceGained}
        </p>
      </div>

      {/* 순위표 */}
      <div className="bg-card p-6 rounded-xl shadow space-y-4">
        <h2 className="text-lg font-bold text-primary border-b border-border pb-2">
          다른 순위
        </h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          {others.map((user) => (
            <div
              key={user.userId}
              className="bg-white dark:bg-card-background p-4 rounded-lg shadow-sm border border-card-border text-center space-y-1"
            >
              <p className="text-base font-semibold text-foreground">
                {user.username}
              </p>
              <p className="text-sm text-muted-foreground">
                점수: {user.finalScore}점
              </p>
              <p className="text-xs text-muted-foreground">
                정답: {user.correctAnswers}개 · 경험치 +{user.experienceGained}
              </p>
            </div>
          ))}
        </div>
      </div>

      {/* 버튼 */}
      <div className="flex justify-center pt-4">
        <Button variant="primary" size="large">
          배틀 목록으로 돌아가기
        </Button>
      </div>
    </div>
  );
};

export default BattleResultsPage;
