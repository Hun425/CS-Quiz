import { create } from "zustand";
// 🔹 Toast 메시지 타입
type ToastErrorType = "success" | "warning" | "error" | "info";

interface ToastItem {
  id: number;
  message: string;
  type: ToastErrorType;
}

// ✅ `ToastState` 인터페이스 수정
interface ToastState {
  toasts: ToastItem[];
  showToast: (message: string, type: ToastErrorType) => void;
}

export const useToastStore = create<ToastState>((set) => ({
  toasts: [],
  showToast: (message, type) => {
    const id = Date.now();
    set((state) => {
      const newToasts = [...state.toasts, { id, message, type }];
      return { toasts: newToasts.length > 3 ? newToasts.slice(-3) : newToasts };
    });

    // 3초 후 자동 삭제
    setTimeout(() => {
      set((state) => ({
        toasts: state.toasts.filter((toast) => toast.id !== id),
      }));
    }, 3000);
  },
}));
