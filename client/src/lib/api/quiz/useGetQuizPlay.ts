import { useQuery } from "@tanstack/react-query";
import axios from "axios";

interface QuizPlay {
  playId: number;
  quizId: number;
  status: string;
  // 추가 플레이 정보 필드 필요 시 확장
}

export const useGetQuizPlay = (quizId: number) => {
  return useQuery<QuizPlay, Error>({
    queryKey: ["quizPlay", quizId],
    queryFn: () =>
      axios.get(`/api/quizzes/${quizId}/play`).then((res) => res.data),
    enabled: !!quizId,
  });
};
