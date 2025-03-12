"use client";

import React, {
  createContext,
  useContext,
  useEffect,
  useState,
  ReactNode,
} from "react";
import { createPortal } from "react-dom";
import { useToastStore } from "@/store/toastStore"; // Zustand 기반 Store
import { ToastContainer } from "@/app/_components/Toast";

// ✅ `ToastContextType`을 명확히 정의
interface ToastContextType {
  showToast: (
    message: string,
    type: "success" | "error" | "info" | "warning"
  ) => void;
}

const ToastContext = createContext<ToastContextType | undefined>(undefined);

export const useToast = () => {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error("useToast는 ToastProvider 내에서 사용해야 합니다.");
  }
  return context;
};

export const ToastProvider = ({ children }: { children: ReactNode }) => {
  const showToast = useToastStore((state) => state.showToast); // Zustand에서 showToast 가져오기
  const [mounted, setMounted] = useState(false);
  const [portalRoot, setPortalRoot] = useState<HTMLElement | null>(null);

  useEffect(() => {
    setMounted(true);

    // Portal Root 생성
    let toastRoot = document.getElementById("toast-root");
    if (!toastRoot) {
      toastRoot = document.createElement("div");
      toastRoot.id = "toast-root";
      document.body.appendChild(toastRoot);
    }
    setPortalRoot(toastRoot);
  }, []);

  if (!mounted || !portalRoot) return null;

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}
      {createPortal(<ToastContainer />, portalRoot)}
    </ToastContext.Provider>
  );
};
