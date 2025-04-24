import Profile from "../_components/Profile";
import Link from "next/link";

export default function MyPageLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <div className="max-w-screen-xl w-full mx-auto px-4 sm:px-8 lg:px-16 py-8 sm:py-12 lg:py-16 flex flex-col lg:flex-row gap-6 min-h-screen">
      {/* ✅ 사이드바 */}
      <aside className="w-full lg:w-1/4 bg-card rounded-md">
        <Profile />
        <nav className="mt-6 flex flex-col gap-2">
          <Link
            href="/mypage/dashboard"
            className="w-full px-4 py-2 text-sm font-semibold text-foreground bg-sub-foreground border border-border rounded hover:bg-gray-500 transition-colors"
          >
            활동 기록
          </Link>
          <Link
            href="/mypage/create"
            className="w-full px-4 py-2 text-sm font-semibold text-foreground bg-sub-foreground border border-border rounded hover:bg-gray-500 transition-colors"
          >
            퀴즈 생성하기
          </Link>
          <Link
            href="/mypage/settings"
            className="w-full px-4 py-2 text-sm font-semibold text-foreground bg-sub-foreground border border-border rounded hover:bg-gray-500 transition-colors"
          >
            설정
          </Link>
        </nav>
      </aside>

      {/* ✅ 메인 콘텐츠 */}
      <main className="w-full lg:w-3/4 bg-card p-6 sm:p-8 shadow-md border border-border rounded-md">
        {children}
      </main>
    </div>
  );
}
