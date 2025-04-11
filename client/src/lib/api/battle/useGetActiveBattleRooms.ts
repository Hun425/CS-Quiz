import { useQuery } from "@tanstack/react-query";
import httpClient from "../httpClient";
import { BattleRoomResponse } from "@/lib/types/battle";

/**
 * ✅ 활성 배틀룸 목록 조회 훅
 * @description 현재 활성화된 배틀룸 목록을 조회합니다.
 * @permission 모든 사용자가 조회할 수 있습니다.
 * @returns {QueryObserverResult<CommonApiResponse<BattleRoomResponse[]>>} 활성 배틀룸 목록
 * @cache 2분
 */

export const useGetActiveBattleRooms = () => {
  return useQuery({
    queryKey: ["activeBattleRooms"],
    queryFn: async () => {
      const response = await httpClient.get<
        CommonApiResponse<BattleRoomResponse[]>
      >("/battles/active");
      return response.data;
    },
    staleTime: 1000 * 60 * 2, // 2분 동안 캐싱
  });
};
