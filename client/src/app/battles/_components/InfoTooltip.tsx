"use client";

import { ReactNode, useId } from "react";
import { Info } from "lucide-react";

interface InfoTooltipProps {
  content: ReactNode;
  label?: string;
}

const InfoTooltip = ({
  content,
  label = "추가 설명 보기",
}: InfoTooltipProps) => {
  const tooltipId = useId();

  return (
    <div
      className="relative group inline-block cursor-pointer"
      role="tooltip"
      aria-label={label}
      aria-describedby={tooltipId}
    >
      <Info className="w-4 h-4 text-blue-500" aria-hidden />

      <div
        id={tooltipId}
        className={`
          absolute z-50 hidden group-hover:block
          bg-white text-xs text-gray-700 border border-gray-200 rounded-md p-2 w-60 shadow-md
          bottom-full right-0 mb-2
        `}
      >
        {content}
      </div>
    </div>
  );
};

export default InfoTooltip;
