import Link from "next/link";
import { QuizSummaryResponse } from "@/types/api";

interface QuizCardProps {
  quiz: QuizSummaryResponse;
}

const QuizCard: React.FC<QuizCardProps> = ({ quiz }) => {
  // 난이도에 따른 색상 및 라벨
  const getDifficultyColor = (level: string) => {
    switch (level) {
      case "BEGINNER":
        return "#4caf50"; // 초록색
      case "INTERMEDIATE":
        return "#ff9800"; // 주황색
      case "ADVANCED":
        return "#f44336"; // 빨간색
      default:
        return "#9e9e9e"; // 회색
    }
  };

  const getDifficultyLabel = (level: string) => {
    switch (level) {
      case "BEGINNER":
        return "입문";
      case "INTERMEDIATE":
        return "중급";
      case "ADVANCED":
        return "고급";
      default:
        return "알 수 없음";
    }
  };

  // 퀴즈 타입 한글 표시
  const getQuizTypeLabel = (type: string) => {
    switch (type) {
      case "DAILY":
        return "데일리 퀴즈";
      case "TAG_BASED":
        return "태그 기반";
      case "TOPIC_BASED":
        return "주제 기반";
      case "CUSTOM":
        return "커스텀";
      default:
        return "알 수 없음";
    }
  };

  // 날짜 포맷팅
  //   const formatDate = (dateString: string) => {
  //     const date = new Date(dateString);
  //     return new Intl.DateTimeFormat("ko-KR", {
  //       year: "numeric",
  //       month: "long",
  //       day: "numeric",
  //     }).format(date);
  //   };

  return (
    <div
      className="quiz-card"
      style={{
        border: "1px solid #e0e0e0",
        borderRadius: "8px",
        padding: "1.5rem",
        backgroundColor: "white",
        transition: "transform 0.2s, box-shadow 0.2s",
        cursor: "pointer",
        height: "100%",
        display: "flex",
        flexDirection: "column",
        boxShadow: "0 2px 4px rgba(0,0,0,0.1)",
      }}
    >
      <Link
        href={`/quizzes/${quiz.id}`}
        style={{
          textDecoration: "none",
          color: "inherit",
          display: "flex",
          flexDirection: "column",
          height: "100%",
        }}
      >
        <h3
          style={{ margin: "0 0 0.5rem", fontSize: "1.2rem", color: "#1976d2" }}
        >
          {quiz.title}
        </h3>

        <div
          style={{
            display: "flex",
            flexWrap: "wrap",
            gap: "0.5rem",
            marginBottom: "1rem",
          }}
        >
          <span
            style={{
              backgroundColor: getDifficultyColor(quiz.difficultyLevel),
              color: "white",
              padding: "0.25rem 0.5rem",
              borderRadius: "4px",
              fontSize: "0.75rem",
            }}
          >
            {getDifficultyLabel(quiz.difficultyLevel)}
          </span>
          <span
            style={{
              backgroundColor: "#e0e0e0",
              padding: "0.25rem 0.5rem",
              borderRadius: "4px",
              fontSize: "0.75rem",
            }}
          >
            {getQuizTypeLabel(quiz.quizType)}
          </span>
          <span
            style={{
              backgroundColor: "#e0e0e0",
              padding: "0.25rem 0.5rem",
              borderRadius: "4px",
              fontSize: "0.75rem",
            }}
          >
            {quiz.questionCount}문제
          </span>
        </div>
      </Link>
    </div>
  );
};

export default QuizCard;
