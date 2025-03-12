// 🔹 공통 API 응답 타입
interface CommonApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  timestamp: string;
  code: string;
}
