import { useMutation } from "@tanstack/react-query";
import httpClient from "../httpClient";
import {
  BattleRoomCreateRequest,
  BattleRoomResponse,
} from "@/lib/types/battle";

/**
 * ✅ 배틀룸 생성 훅
 * @description 새로운 배틀룸을 생성합니다.
 * @permission 로그인한 사용자만 생성할 수 있습니다.
 * @mutation
 * @returns {MutationObserverResult<CommonApiResponse<BattleRoomResponse>>} 생성된 배틀룸 정보
 */

export const useCreateBattleRoom = () => {
  return useMutation({
    mutationFn: async (data: BattleRoomCreateRequest) => {
      const response = await httpClient.post<
        CommonApiResponse<BattleRoomResponse>
      >("/battles", data);
      return response.data;
    },
  });
};
