import { NextRequest, NextResponse } from "next/server";

export function middleware(req: NextRequest) {
  console.log("🔥 middleware 실행됨! 경로:", req.nextUrl.pathname);

  if (req.nextUrl.pathname === "/api") {
    console.log("🔄 /api → /login으로 리디렉트!");
    return NextResponse.redirect(new URL("/login", req.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/api"],
};
