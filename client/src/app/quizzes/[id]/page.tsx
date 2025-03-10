"use client";

import { useParams } from "next/navigation";

export default function QuizDetailPage() {
  const { id } = useParams();
  return <div>퀴즈 상세 페이지 - ID: {id}</div>;
}
