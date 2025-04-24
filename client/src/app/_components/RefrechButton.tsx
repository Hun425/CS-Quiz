"use client";
import React, { useState } from "react";
import { RotateCcw } from "lucide-react";
import Button from "./Button";

const RefreshButton: React.FC = () => {
  const [isRefreshing, setIsRefreshing] = useState(false);

  const handleRefresh = () => {
    setIsRefreshing(true);
    window.location.reload(); // CSR 환경에서는 강제 새로고침
  };

  return (
    <Button
      variant="outline"
      size="medium"
      onClick={handleRefresh}
      className="flex items-center gap-2"
      disabled={isRefreshing}
    >
      <RotateCcw className={`w-5 h-5 ${isRefreshing ? "animate-spin" : ""}`} />
      새로고침
    </Button>
  );
};

export default RefreshButton;
