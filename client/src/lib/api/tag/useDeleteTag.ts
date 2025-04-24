import { useMutation, useQueryClient } from "@tanstack/react-query";

import httpClient from "../httpClient";

const deleteTag = async (tagId: number) => {
  const response = await httpClient.delete<CommonApiResponse<object>>(
    `/tags/${tagId}`
  );
  return response.data;
};

export const useDeleteTag = () => {
  const queryClient = useQueryClient();

  return useMutation<CommonApiResponse<object>, Error, number>({
    mutationFn: deleteTag,
    onSuccess: (data, tagId) => {
      console.log("태그 삭제 성공:", data);
      queryClient.invalidateQueries({ queryKey: ["allTags"] });
      queryClient.invalidateQueries({ queryKey: ["tagDetail", tagId] });
    },
    onError: (error) => {
      console.error("태그 삭제 실패:", error);
      alert("태그 삭제 중 오류가 발생했습니다.");
    },
  });
};
