import battleWebSocketService from "@/lib/services/websocket/battleWebSocketService";
import Button from "@/app/_components/Button";

interface Props {
  questionId: number;
  answer: string | null;
  timeSpentSecond: number;
}

export default function SubmitAnswerButton({
  questionId,
  answer,
  timeSpentSecond,
}: Props) {
  return (
    <Button
      className="mt-4 px-4 py-2 bg-primary text-white rounded hover:opacity-90"
      onClick={() => {
        battleWebSocketService.submitAnswer(
          questionId,
          timeSpentSecond,
          answer
        );
      }}
    >
      제출하기
    </Button>
  );
}
