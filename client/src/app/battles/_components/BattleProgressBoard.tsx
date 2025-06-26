"use client";

import { useState, useEffect } from "react";
import { useBattleSocketStore } from "@/store/battleStore";
import { motion, AnimatePresence } from "framer-motion";
import { Users, Activity } from "lucide-react";
import InfoTooltip from "./InfoTooltip";

const BattleProgressBoard = () => {
  const progress = useBattleSocketStore((state) => state.progress);
  const [currentIndex, setCurrentIndex] = useState(1);
  const nextQuestion = useBattleSocketStore((s) => s.nextQuestion);

  const hasLoaded = !!progress;
  const participants = hasLoaded
    ? Object.values(progress.participantProgress || {}).sort(
        (a, b) => b.correctAnswers - a.correctAnswers
      )
    : [];

  useEffect(() => {
    if (nextQuestion) {
      setCurrentIndex((prev) => prev + 1);
    } else {
      setCurrentIndex(1);
    }
  }, [nextQuestion]);

  return (
    <motion.div
      className="p-4 rounded-xl bg-card-background space-y-4 shadow-md"
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4 }}
    >
      {/* 제목 영역 */}
      <div className="flex items-start sm:items-center justify-between gap-2 text-base font-semibold text-gray-800 flex-col sm:flex-row">
        <div className="flex items-center gap-2">
          <Activity className="w-5 h-5 text-primary" />
          <span className="text-foreground">게임 진행 현황</span>
        </div>
        <div className="flex items-center gap-2 text-sm text-muted font-medium">
          {hasLoaded && (
            <>
              <Users className="w-4 h-4 text-gray-500" />
              <span>
                문제 {currentIndex} / {progress.totalQuestions}
              </span>
            </>
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
      </div>

      {/* 참가자 리스트 */}
      <motion.ul layout className="space-y-2">
        <AnimatePresence initial={false}>
          {hasLoaded && participants.length > 0 ? (
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            participants.map((p, idx) => {
              return (
                <motion.li
                  key={p.userId}
                  layout="position"
                  initial={{ opacity: 0, scale: 0.98 }}
                  animate={{ opacity: 1, scale: 1 }}
                  exit={{ opacity: 0 }}
                  transition={{ type: "spring", stiffness: 300, damping: 25 }}
                  className={`
                    px-3 py-2 rounded-lg border flex justify-between items-center
                    ${
                      p.hasAnsweredCurrent
                        ? "bg-green-50 border-green-200"
                        : "bg-yellow-50 border-yellow-200"
                    }
                    shadow-sm transition-all
                  `}
                >
                  <span
                    className={`font-small truncate ${
                      p.hasAnsweredCurrent
                        ? "text-green-800"
                        : "text-yellow-800"
                    }`}
                  >
                    {p.username}
                  </span>
                  <div className="grid grid-cols-3 gap-3 text-xs text-gray-700 text-right font-medium">
                    <span>정답 {p.correctAnswers}</span>
                    <span>연속 {p.currentStreak}회</span>
                    <span>{p.currentScore}점</span>
                  </div>
                </motion.li>
              );
            })
          ) : !hasLoaded ? (
            <motion.li
              className="text-sm text-neutral-400 text-center py-6"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
            >
              문제를 풀고 있어요..
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
