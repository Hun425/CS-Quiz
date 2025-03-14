import { useQuery } from "@tanstack/react-query";
import axios from "axios";

interface DailyQuiz {
  id: number;
  title: string;
  date: string;
  // 일일 퀴즈에 필요한 필드 추가
}

export const useGetDailyQuizzes = () => {
  return useQuery<DailyQuiz[], Error>({
    queryKey: ["dailyQuizzes"],
    queryFn: () => axios.get("/api/quizzes/daily").then((res) => res.data),
  });
};
