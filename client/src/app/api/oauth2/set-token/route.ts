import { NextRequest, NextResponse } from "next/server";

export async function POST(req: NextRequest) {
  const { token, refreshToken, expiresIn } = await req.json();

  if (!token || !refreshToken) {
    return NextResponse.json({ success: false }, { status: 400 });
  }

  const response = NextResponse.json({ success: true });

  response.cookies.set("accessToken", token, {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    sameSite: "lax",
    path: "/",
    maxAge: Number(expiresIn) || 60 * 60,
  });

  response.cookies.set("refreshToken", refreshToken, {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    sameSite: "lax",
    path: "/",
    maxAge: 60 * 60 * 24 * 7,
  });

  return response;
}
