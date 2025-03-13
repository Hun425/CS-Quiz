"use client";

import React from "react";
import { createPortal } from "react-dom";
import { useToastStore } from "@/store/toastStore";
import { useEffect, useState } from "react";

const typeStyles = {
  success: "bg-success text-white border-success-light",
  warning: "bg-warning text-black border-warning-light",
  error: "bg-danger text-white border-danger-light",
  info: "bg-primary text-white border-card-border",
};

// 🔹 개별 Toast 컴포넌트
export const Toast: React.FC<{
  message: string;
  type: "success" | "error" | "info" | "warning";
}> = ({ message, type }) => {
  return (
    <div
      className={`px-8 py-4 rounded-lg shadow-md border-2 text-lg ${typeStyles[type]} animate-fade-in-out`}
    >
      {message}
    </div>
  );
};

// 🔹 Portal을 활용한 Toast 리스트 컴포넌트
export const ToastContainer: React.FC = () => {
  const { toasts } = useToastStore();
  const [toastRoot, setToastRoot] = useState<HTMLElement | null>(null);

  useEffect(() => {
    setToastRoot(document.getElementById("toast-root"));
  }, []);

  if (!toastRoot) return null;
  return createPortal(
    <div className="fixed top-5 right-5 z-50 flex flex-col gap-2">
      {toasts.map(({ id, message, type }) => (
        <Toast key={id} message={message} type={type} />
      ))}
    </div>,
    toastRoot
  );
};
