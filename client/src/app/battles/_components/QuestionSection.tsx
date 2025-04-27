// components/BattleQuestionSection.tsx
"use client";

interface BattleQuestionSectionProps {
  questionText?: string;
  currentIndex: number;
  total: number;
}

const BattleQuestionSection = ({
  questionText = "문제가 아직 시작되지 않았습니다.",
  currentIndex,
  total,
}: BattleQuestionSectionProps) => {
  return (
    <div className="bg-white p-6 rounded-lg shadow-md border border-gray-200">
      <div className="text-sm text-gray-600 mb-2">
        문제 {currentIndex + 1} / {total}
      </div>
      <div className="text-lg font-semibold text-default">{questionText}</div>
    </div>
  );
};

export default BattleQuestionSection;
