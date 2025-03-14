import { useQuery } from "@tanstack/react-query";
import axios from "axios";

interface SearchResult {
  id: number;
  title: string;
  // 검색 결과에 필요한 필드 추가
}

export const useSearchQuizzes = (query: string) => {
  return useQuery<SearchResult[], Error>({
    queryKey: ["searchQuizzes", query],
    queryFn: () =>
      axios.get(`/api/quizzes/search?query=${query}`).then((res) => res.data),
    enabled: !!query,
  });
};
