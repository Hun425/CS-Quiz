import { useMutation } from "@tanstack/react-query";
import httpClient from "../httpClient";
import { BattleRoomResponse } from "@/lib/types/battle";
import { CommonApiResponse } from "@/lib/types/common";

/**
 * ✅ 배틀룸 준비 상태 토글 훅
 * @description 사용자가 준비 상태를 변경합니다.
 * @permission 배틀룸에 참가한 사용자만 변경할 수 있습니다.
 * @mutation
 * @param {number} roomId 준비 상태를 변경할 배틀룸의 ID
 * @returns {MutationObserverResult<CommonApiResponse<BattleRoomResponse>>} 변경된 배틀룸 정보
 */

export const useToggleBattleReady = () => {
  return useMutation({
    mutationFn: async (roomId: number) => {
      const response = await httpClient.post<
        CommonApiResponse<BattleRoomResponse>
      >(`/battles/${roomId}/ready`);
      return response.data;
    },
  });
};
