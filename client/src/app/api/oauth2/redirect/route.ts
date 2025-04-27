import { NextRequest, NextResponse } from "next/server";

/**
 * 🔹 OAuth2 로그인 요청
 * - JWT를 localStorage에 저장할 수 있도록 `api/oauth2/callback`으로 리다이렉트
 */
export async function GET(req: NextRequest) {
  const { searchParams } = new URL(req.url);
  const token = searchParams.get("token");
  const refreshToken = searchParams.get("refreshToken");
  const expiresIn = searchParams.get("expiresIn");

  if (!token || !refreshToken) {
    return NextResponse.redirect(
      new URL("/login?error=invalid_token", req.url),
      303
    );
  }

  // ✅ /api/oauth2/callback 페이지로 이동하여 JWT 저장
  const response = new NextResponse(null, { status: 303 });
  response.headers.set(
    "Location",
    `/api/oauth2/callback?token=${token}&refreshToken=${refreshToken}&expiresIn=${expiresIn}`
  );

  return response;
}
