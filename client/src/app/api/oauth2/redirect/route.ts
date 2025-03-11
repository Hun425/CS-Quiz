import { NextRequest, NextResponse } from "next/server";

export async function GET(req: NextRequest) {
  const { searchParams } = new URL(req.url);
  const token = searchParams.get("token");
  const refreshToken = searchParams.get("refreshToken");
  const email = searchParams.get("email");
  const username = searchParams.get("username");
  const expiresIn = searchParams.get("expiresIn");

  console.log("token:", searchParams);

  if (!token || !refreshToken) {
    return NextResponse.redirect(
      new URL("/login?error=invalid_token", req.url)
    );
  }

  const response = NextResponse.redirect(new URL("/", req.url));
  response.cookies.set("access_token", token, {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    maxAge: Number(expiresIn) / 1000,
    path: "/",
  });
  response.cookies.set("refresh_token", refreshToken, {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    maxAge: 60 * 60 * 24 * 7, // 7일
    path: "/",
  });

  return response;
}
