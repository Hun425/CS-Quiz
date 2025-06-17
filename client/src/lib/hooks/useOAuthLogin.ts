import { Provider } from "../types/auth";

/**
 * ✅ OAuth2 로그인 로직을 관리하는 커스텀 훅 (useOAuthLogin)
 * - 소셜 로그인 버튼 클릭 시, 백엔드에 로그인 요청을 보내는 함수
 * - 로그인 중인지 여부를 상태로 관리
 * - 서비스 로직에 집중하고, UI 로직을 분리하여 관리
 * @returns {loginWithProvider}
 */

export const useOAuthLogin = () => {
  const loginWithProvider = (provider: Provider) => {
    // 환경 변수 디버깅
    console.log("🔍 Environment Variables Debug:");
    console.log("- NEXT_PUBLIC_API_BASE_URL:", process.env.NEXT_PUBLIC_API_BASE_URL);
    console.log("- NODE_ENV:", process.env.NODE_ENV);
    
    // 클라이언트 사이드에서만 실행되므로 NEXT_PUBLIC_ 환경 변수 사용
    const apiUrl = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080";
    
    console.log("🚀 Final OAuth Login URL:", `${apiUrl}/api/oauth2/authorize/${provider}`);
    window.location.href = `${apiUrl}/api/oauth2/authorize/${provider}`;
  };

  return { loginWithProvider };
};
