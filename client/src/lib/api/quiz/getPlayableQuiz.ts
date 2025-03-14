import httpClient from "../httpClient";

export const getPlayableQuiz = async (quizId: number) => {
  const response = await httpClient.get(`/quiz/${quizId}/playable`);
  return response.data;
};
