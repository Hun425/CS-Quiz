import { useMutation } from "@tanstack/react-query";
import httpClient from "../httpClient";
import { BattleRoomResponse } from "@/lib/types/battle";

/**
 * ✅ 배틀룸 참가 훅
 * @description 사용자가 특정 배틀룸에 참가합니다.
 * @permission 로그인한 사용자만 참가할 수 있습니다.
 * @mutation
 * @param {number} roomId 참가할 배틀룸의 ID
 * @returns {MutationObserverResult<CommonApiResponse<BattleRoomResponse>>} 참가한 배틀룸 정보
 */

export const useJoinBattleRoom = () => {
  return useMutation({
    mutationFn: async (roomId: number) => {
      const response = await httpClient.post<
        CommonApiResponse<BattleRoomResponse>
      >(`/battles/${roomId}/join`);
      return response.data;
    },
  });
};
