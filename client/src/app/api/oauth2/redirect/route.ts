import { NextRequest, NextResponse } from "next/server";

/**
 * 🔹 OAuth2 로그인 요청의 경우 page redirect 방식을 적용하기 때문에 별도의 fetch 요청 필요없음
 * - 로그인 성공 시, 쿠키에 토큰 저장/갱신
 * @param req - NextRequest
 * @returns {NextResponse}
 */

export async function GET(req: NextRequest) {
  const { searchParams } = new URL(req.url);
  const token = searchParams.get("token");
  const refreshToken = searchParams.get("refreshToken");
  // const email = searchParams.get("email");
  // const username = searchParams.get("username");
  const expiresIn = searchParams.get("expiresIn");

  if (!token || !refreshToken) {
    return NextResponse.redirect(
      new URL("/login?error=invalid_token", req.url)
    );
  }

  console.log("🔐 로그인 성공: ", { searchParams });

  // ✅ 로그인 성공 시, 쿠키에 토큰 저장
  const response = NextResponse.redirect(new URL("/quizzes", req.url));
  response.cookies.set("access_token", token, {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    maxAge: Number(expiresIn) / 1000,
    path: "/",
  });
  response.cookies.set("refresh_token", refreshToken, {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    maxAge: 60 * 60 * 24 * 7,
    path: "/",
  });

  return response;
}
