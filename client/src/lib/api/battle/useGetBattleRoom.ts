import { useQuery } from "@tanstack/react-query";
import httpClient from "../httpClient";
import { BattleRoomResponse } from "@/lib/types/battle";

/**
 * ✅ 배틀룸 조회 훅
 * @description 특정 배틀룸의 상세 정보를 조회합니다.
 * @permission 모든 사용자가 조회할 수 있습니다.
 * @param {number} roomId 조회할 배틀룸의 ID
 * @returns {QueryObserverResult<CommonApiResponse<BattleRoomResponse>>} 배틀룸 상세 정보
 * @cache 5분
 */

export const useGetBattleRoom = (roomId: number) => {
  return useQuery({
    queryKey: ["battleRoom", roomId],
    queryFn: async () => {
      const response = await httpClient.get<
        CommonApiResponse<BattleRoomResponse>
      >(`/battles/${roomId}`);

      return response.data;
    },
    enabled: typeof roomId === "number" && !isNaN(roomId),
    staleTime: 1000 * 60 * 5, // 5분 동안 캐싱
  });
};
