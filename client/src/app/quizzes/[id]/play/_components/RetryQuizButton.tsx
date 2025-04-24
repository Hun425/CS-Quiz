"use client";

import { useRouter } from "next/navigation";
import { useQuizStore } from "@/store/quizStore";
import Button from "@/app/_components/Button";

interface Props {
  quizId: number;
}

export default function RetryQuizButton({ quizId }: Props) {
  const { resetQuiz } = useQuizStore();
  const router = useRouter();

  const handleRetry = async () => {
    resetQuiz();
    router.replace(`/quizzes/${quizId}/play`);
  };

  return (
    <Button variant="secondary" onClick={handleRetry}>
      🔁 다시 풀기
    </Button>
  );
}
