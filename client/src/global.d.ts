/**
 * ✅ 공통 API 응답 타입 (CommonApiResponse<T>)
 * - 모든 API 응답에서 공통적으로 사용되는 기본 구조
 * - `success`: 요청 성공 여부 (true/false)
 * - `data`: 응답 데이터 (제네릭 타입 `T`)
 * - `message`: 추가적인 메시지 (선택적)
 * - `timestamp`: 응답 시간 (ISO 8601 형식)
 * - `code`: 응답 코드 (서버에서 정의한 코드 값)
 */
interface CommonApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  timestamp: string;
  code: string;
}

/**
 * ✅ 페이지네이션 응답 타입 (PageResponse<T>)
 * - 리스트 조회 시 사용되는 페이지네이션 정보 포함
 */
interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}
