import { useRouter } from "next/navigation";
import { useEffect } from "react";

/**
 * router.push를 가로채서 라우팅 전 confirm 창을 띄우는 훅
 */
export default function useBeforeRouteLeave(
  shouldBlock: boolean,
  onLeave: () => void
): void {
  const router = useRouter();

  useEffect(() => {
    if (!shouldBlock) return;

    const originalPush = router.push.bind(router);

    const wrappedPush: typeof router.push = (url, options) => {
      const confirmLeave = window.confirm(
        "퀴즈가 초기화됩니다. 나가시겠습니까?"
      );
      if (confirmLeave) {
        onLeave();
        return originalPush(url, options);
      } else {
        // 라우팅 취소
        return Promise.resolve(); // router.push는 Promise 반환
      }
    };

    // 타입스크립트는 push가 readonly라 생각할 수 있음, 이때 Object.defineProperty로 우회
    Object.defineProperty(router, "push", {
      value: wrappedPush,
      writable: true,
    });

    return () => {
      Object.defineProperty(router, "push", {
        value: originalPush,
        writable: true,
      });
    };
  }, [shouldBlock, onLeave, router]);
}
