import { useMutation } from "@tanstack/react-query";
import httpClient from "../httpClient";
import { CommonApiResponse } from "@/lib/types/common";

/**
 * ✅ 배틀룸 나가기 훅
 * @description 사용자가 특정 배틀룸에서 나갑니다.
 * @permission 배틀룸에 참가한 사용자만 나갈 수 있습니다.
 * @mutation
 * @param {number} roomId 나갈 배틀룸의 ID
 * @returns {MutationObserverResult<CommonApiResponse<void>>} 배틀룸 나가기 성공 여부
 */

export const useLeaveBattleRoom = () => {
  return useMutation({
    mutationFn: async (roomId: number) => {
      const response = await httpClient.post<CommonApiResponse<void>>(
        `/battles/${roomId}/leave`
      );
      return response.data;
    },
  });
};
