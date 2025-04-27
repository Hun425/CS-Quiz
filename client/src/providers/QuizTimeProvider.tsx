import { createContext, useContext, useEffect, useState } from "react";

interface QuizTimerContextType {
  timeTaken: number;
  startTimer: () => void;
  stopTimer: () => void;
}

const QuizTimerContext = createContext<QuizTimerContextType | undefined>(
  undefined
);

export const QuizTimerProvider = ({
  children,
}: {
  children: React.ReactNode;
}) => {
  const [timeTaken, setTimeTaken] = useState(0);
  const [isRunning, setIsRunning] = useState(false);

  useEffect(() => {
    let interval: NodeJS.Timeout;
    if (isRunning) {
      interval = setInterval(() => {
        setTimeTaken((prev) => prev + 1);
      }, 1000);
    }

    return () => clearInterval(interval);
  }, [isRunning]);

  const startTimer = () => setIsRunning(true);
  const stopTimer = () => setIsRunning(false);

  return (
    <QuizTimerContext.Provider value={{ timeTaken, startTimer, stopTimer }}>
      {children}
    </QuizTimerContext.Provider>
  );
};

// ✅ Context 사용 Hook
export const useQuizTimer = () => {
  const context = useContext(QuizTimerContext);
  if (!context) {
    throw new Error("useQuizTimer must be used within a QuizTimerProvider");
  }
  return context;
};
