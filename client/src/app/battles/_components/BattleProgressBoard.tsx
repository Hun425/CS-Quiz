"use client";

import { useBattleSocketStore } from "@/store/battleStore";
import { motion, AnimatePresence } from "framer-motion";
import { Users, Activity } from "lucide-react";
import InfoTooltip from "./InfoTooltip";

const BattleProgressBoard = () => {
  const progress = useBattleSocketStore((state) => state.progress);

  const hasLoaded = !!progress;
  const participants = hasLoaded
    ? Object.values(progress.participantProgress || {}).sort(
        (a, b) => b.correctAnswers - a.correctAnswers
      )
    : [];

  return (
    <motion.div
      className="p-4 rounded-xl bg-white border border-border space-y-4"
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4 }}
    >
      {/* 제목 */}
      <div className="flex items-center justify-between gap-2 text-base font-semibold text-gray-800">
        <div className="flex items-center gap-2">
          <Activity className="w-5 h-5 text-primary" />
          <span>게임 진행 현황</span>
        </div>
        {/* 상단 진행 정보 */}
        {hasLoaded && (
          <div className="gap-1 text-sm text-muted font-medium">
            <div className="flex items-center gap-2">
              <Users className="w-4 h-4 text-gray-500" />
              <span>
                문제 {progress.currentQuestionIndex + 1} /{" "}
                {progress.totalQuestions}
              </span>
            </div>
          </div>
        )}
        <InfoTooltip
          content={
            <>
              현재 문제 번호, 남은 시간, 참가자 점수 등을 보여줍니다. <br />
              빠르게 문제를 풀고 더 많은 점수를 획득하세요!
            </>
          }
          label="게임 진행 현황 설명"
        />
      </div>

      {/* 참가자 목록 */}
      <motion.ul layout className="space-y-2">
        <AnimatePresence initial={false}>
          {hasLoaded && participants.length > 0 ? (
            participants.map((p, idx) => (
              <motion.li
                key={p.userId}
                layout="position"
                initial={{ opacity: 0, scale: 0.98 }}
                animate={{ opacity: 1, scale: 1 }}
                exit={{ opacity: 0 }}
                transition={{ type: "spring", stiffness: 300, damping: 25 }}
                className={`
                  px-3 py-2 rounded-lg border flex justify-between items-center bg-white
                  ${
                    idx === 0
                      ? "bg-yellow-50 border-yellow-300"
                      : "border-gray-200"
                  }
                `}
              >
                <span className="font-medium text-gray-800 truncate">
                  {p.username}
                </span>
                <div className="grid grid-cols-3 gap-3 text-xs text-gray-600 text-right">
                  <span>✅ {p.correctAnswers}</span>
                  <span>🔥 {p.currentStreak}회</span>
                  <span>🏅 {p.currentScore}점</span>
                </div>
              </motion.li>
            ))
          ) : !hasLoaded ? (
            <motion.li
              className="text-sm text-neutral-400 text-center py-6"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
            >
              🔄 게임 정보 수신 중...
            </motion.li>
          ) : (
            <li className="text-sm text-center text-neutral-400">
              참가자가 없습니다.
            </li>
          )}
        </AnimatePresence>
      </motion.ul>
    </motion.div>
  );
};

export default BattleProgressBoard;
