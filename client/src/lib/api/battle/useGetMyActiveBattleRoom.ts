import { useQuery } from "@tanstack/react-query";
import httpClient from "../httpClient";
import { BattleRoomResponse } from "@/lib/types/battle";

/**
 * ✅ 내 활성 배틀룸 조회 훅
 * @description 내가 참가한 활성 배틀룸을 조회합니다.
 * @returns {QueryObserverResult<CommonApiResponse<BattleRoomResponse>>} 활성 배틀룸 조회 결과
 * @refetchOnWindowFocus {boolean} true
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
    refetchOnWindowFocus: true,
  });
};
