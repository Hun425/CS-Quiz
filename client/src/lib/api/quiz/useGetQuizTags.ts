import { useQuery } from "@tanstack/react-query";
import axios from "axios";

interface QuizTag {
  tagId: number;
  name: string;
  quizzes: number[];
  // 추가 태그 정보 필드 필요 시 확장
}

export const useGetQuizTags = (tagId: number) => {
  return useQuery<QuizTag, Error>({
    queryKey: ["quizTags", tagId],
    queryFn: () =>
      axios.get(`/api/quizzes/tags/${tagId}`).then((res) => res.data),
    enabled: !!tagId,
  });
};
