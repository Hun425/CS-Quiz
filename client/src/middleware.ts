import { NextRequest, NextResponse } from "next/server";

export function middleware(req: NextRequest) {
  console.log("🔥 middleware 실행중!");

  const token = req.cookies.has("access_token");
  console.log("🍪 access_token:", token);

  // 로그인 + 로그인 페이지
  if (token && req.nextUrl.pathname.startsWith("/login")) {
    return NextResponse.redirect(new URL("/", req.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/"],
};
