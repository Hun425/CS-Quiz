"use client";

import { QuizTimerProvider } from "@/providers/QuizTimeProvider";
import QuizPlayPage from "./QuizPlayPage";

export default function QuizPlayWrapper() {
  return (
    <QuizTimerProvider>
      <QuizPlayPage />
    </QuizTimerProvider>
  );
}
