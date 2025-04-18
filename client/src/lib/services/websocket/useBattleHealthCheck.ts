"use client";

import { useEffect } from "react";
import { useBattleSocketStore } from "@/store/battleStore";
import { useToastStore } from "@/store/toastStore";
import battleSocketClient from "@/lib/services/websocket/battleWebSocketService";
import { useProfileStore } from "@/store/profileStore";

const HEALTH_CHECK_INTERVAL = 5000;
const TIMEOUT_THRESHOLD = 30000;

export const useBattleHealthCheck = (roomId: number) => {
  const { userProfile } = useProfileStore();
  const lastUpdatedAt = useBattleSocketStore((state) => state.lastUpdatedAt);
  const reset = useBattleSocketStore((state) => state.reset);
  const { showToast } = useToastStore();

  const userId = userProfile?.id;

  useEffect(() => {
    console.log("🔥 useBattleHealthCheck 시작", {
      roomId,
      userId,
      lastUpdatedAt,
    });

    if (!roomId || !userId) return;

    const interval = setInterval(() => {
      const now = Date.now();
      const diff = now - (lastUpdatedAt ?? 0);

      console.log("🩺 헬스체크 diff:", diff);

      // ✅ 테스트 조건 - 강제 재연결 유도
      if (diff > TIMEOUT_THRESHOLD /* or use: || true for test */) {
        console.warn("⛔ WebSocket 응답 없음 - 재연결 시도");
        showToast("연결이 끊겼습니다. 재시도 중...", "error");

        battleSocketClient.retryConnection(roomId, userId);

        // fallback: 5초 후에도 여전히 같으면 reset
        setTimeout(() => {
          if (useBattleSocketStore.getState().lastUpdatedAt === lastUpdatedAt) {
            console.warn("❌ 재연결 실패로 상태 초기화");
            reset();
          } else {
            console.info("✅ 재연결 성공 또는 메시지 수신됨");
          }
        }, 5000);
      }
    }, HEALTH_CHECK_INTERVAL);

    return () => clearInterval(interval);
  }, [lastUpdatedAt, roomId, userId, reset, showToast]);
};
