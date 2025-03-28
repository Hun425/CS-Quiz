"use client";

import React, { useEffect, useState } from "react";
import { createPortal } from "react-dom";
import { useToastStore } from "@/store/toastStore";

const typeStyles = {
  success: "bg-green-500 text-white",
  warning: "bg-yellow-400 text-black",
  error: "bg-red-500 text-white",
  info: "bg-blue-500 text-white",
};

export const Toast: React.FC<{
  message: string;
  type: "success" | "error" | "info" | "warning";
}> = ({ message, type }) => {
  return (
    <div
      className={`px-4 py-3 rounded-md text-sm font-medium ${typeStyles[type]} animate-fade-in-out`}
    >
      {message}
    </div>
  );
};

export const ToastContainer: React.FC = () => {
  const { toasts } = useToastStore();
  const [toastRoot, setToastRoot] = useState<HTMLElement | null>(null);

  useEffect(() => {
    setToastRoot(document.getElementById("toast-root"));
  }, []);

  if (!toastRoot) return null;

  return createPortal(
    <div className="fixed bottom-10 right-4 z-50 flex flex-col gap-2">
      {toasts.map(({ id, message, type }) => (
        <Toast key={id} message={message} type={type} />
      ))}
    </div>,
    toastRoot
  );
};
