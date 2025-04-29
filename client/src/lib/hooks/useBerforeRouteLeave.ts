"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";

/**
 * @param shouldBlock true이면 confirm 띄우고, false이면 confirm 없이 이동
 * @param onLeave 이동 시 해야 할 행동 (ex: resetQuiz)
 */
export default function useBeforeRouteLeave(
  shouldBlock: boolean,
  onLeave: () => void
) {
  const router = useRouter();

  useEffect(() => {
    const handleBeforeUnload = (e: BeforeUnloadEvent) => {
      if (shouldBlock) {
        e.preventDefault();
        e.returnValue = "";
      }
    };

    const handlePopState = () => {
      if (shouldBlock) {
        const confirmLeave = window.confirm(
          "퀴즈가 초기화됩니다. 나가시겠습니까?"
        );
        if (!confirmLeave) {
          // 뒤로가기 막기: push로 다시 현재 경로 고정
          router.push(window.location.pathname);
        } else {
          onLeave();
        }
      }
    };

    window.addEventListener("beforeunload", handleBeforeUnload);
    window.addEventListener("popstate", handlePopState);

    return () => {
      window.removeEventListener("beforeunload", handleBeforeUnload);
      window.removeEventListener("popstate", handlePopState);
    };
  }, [shouldBlock, onLeave, router]);
}
