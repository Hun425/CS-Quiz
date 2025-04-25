"use client";

import { useEffect } from "react";
import { useBattleSocketStore } from "@/store/battleStore";
import Button from "@/app/_components/Button";
import { useRouter } from "next/navigation";
import { Trophy } from "lucide-react";
import { motion } from "framer-motion";
import battleSocketClient from "@/lib/services/websocket/battleWebSocketService";

const BattleResultsPage: React.FC = () => {
  const router = useRouter();
  const endPayload = useBattleSocketStore((state) => state.endPayload);

  // ✅ 브라우저 뒤로가기 시 battles로 강제 이동
  useEffect(() => {
    const handlePopState = () => {
      battleSocketClient.leaveBattle();
      useBattleSocketStore.getState().reset();
      router.push("/battles");
    };

    window.addEventListener("popstate", handlePopState);
    return () => {
      window.removeEventListener("popstate", handlePopState);
    };
  }, [router]);

  //페이지 벗어나면 소켓 종료 및 스토어 초기화
  useEffect(() => {
    return () => {
      battleSocketClient.leaveBattle();
      useBattleSocketStore.getState().reset();
    };
  }, []);

  if (!endPayload) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <p className="text-muted-foreground">
          결과 데이터를 불러오는 중입니다...
        </p>
      </div>
    );
  }

  const sortedResults = [...endPayload.results].sort(
    (a, b) => b.finalScore - a.finalScore
  );
  const winner = sortedResults[0];

  return (
    <div className="max-w-4xl mx-auto p-4 space-y-8 min-h-screen bg-background">
      {/* 🏆 우승자 섹션 */}
      <div className="bg-yellow-100 dark:bg-warning-light p-6 rounded-xl text-center shadow-md">
        <h2 className="text-lg font-bold text-warning mb-2 flex items-center justify-center gap-2">
          <Trophy className="w-5 h-5 text-yellow-600" /> 우승자
        </h2>
        <p className="text-xl font-semibold">{winner.username}</p>
        <p className="text-muted-foreground text-sm mt-1">
          점수: {winner.finalScore}점 · 정답: {winner.correctAnswers}개 ·
          경험치: {winner.experienceGained}
        </p>
      </div>

      {/* 🥇 전체 순위 */}
      <div className="bg-card p-6 rounded-xl shadow space-y-4">
        <h2 className="text-lg font-bold text-primary border-b border-border pb-2">
          전체 순위
        </h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          {sortedResults.map((user, index) => {
            const correctCount = user.correctAnswers;
            const totalCount = Object.keys(user.questionResults).length;
            const correctRatio = totalCount > 0 ? correctCount / totalCount : 0;

            return (
              <div
                key={user.userId}
                className={`p-4 rounded-lg shadow-sm border text-foreground space-y-2 bg-subbackground border-card-border ${
                  index === 0 ? "ring-2 ring-yellow-300" : ""
                }`}
              >
                <p className="text-base font-semibold text-foreground">
                  {index + 1}위 · {user.username}
                </p>
                <p className="text-sm text-muted-foreground">
                  점수: {user.finalScore}점 · 정답: {user.correctAnswers}개
                </p>
                <p className="text-xs text-muted-foreground">
                  평균 시간: {user.averageTimeSeconds.toFixed(1)}초
                </p>
                {/* 정답률 프로그레스 바 */}
                <div className="w-full h-3 bg-gray-200 rounded-full overflow-hidden">
                  <motion.div
                    initial={{ width: 0 }}
                    animate={{ width: `${Math.round(correctRatio * 100)}%` }}
                    transition={{ duration: 1, ease: "easeOut" }}
                    className="h-full bg-green-500 rounded-full"
                  />
                </div>
                <p className="text-xs text-muted-foreground">
                  정답률: {(correctRatio * 100).toFixed(1)}%
                </p>
              </div>
            );
          })}
        </div>
      </div>

      {/* 🔙 돌아가기 버튼 */}
      <div className="flex justify-center pt-4">
        <Button
          variant="primary"
          size="large"
          onClick={() => {
            battleSocketClient.leaveBattle();
            useBattleSocketStore.getState().reset();
            router.push("/battles");
          }}
        >
          돌아가기
        </Button>
      </div>
    </div>
  );
};

export default BattleResultsPage;
