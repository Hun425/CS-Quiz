import { useQuery } from "@tanstack/react-query";
import httpClient from "../httpClient";
import { BattleRoomResponse } from "@/lib/types/battle";
import { CommonApiResponse } from "@/lib/types/common";

/**
 * ✅ 내 활성 배틀룸 조회 훅
 * @description 내가 참가한 활성 배틀룸을 조회합니다.
 * @returns {QueryObserverResult<CommonApiResponse<BattleRoomResponse>>} 활성 배틀룸 조회 결과
 * @cache 5분
 */
export const useGetMyActiveBattleRoom = () => {
  return useQuery({
    queryKey: ["myActiveBattleRoom"],
    queryFn: async () => {
      const response = await httpClient.get<
        CommonApiResponse<BattleRoomResponse>
      >("/battles/my-active");
      return response.data;
    },
    staleTime: 1000 * 60 * 5, // 5분 동안 캐싱
  });
};
