import Profile from "../_components/Profile";
import Link from "next/link";

export default function MyPageLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <div className="max-w-6xl mx-auto p-6 flex flex-col lg:flex-row gap-6">
      {/* ✅ 사이드바 (프로필 + 네비게이션) */}
      <aside className="w-full lg:w-1/4 bg-card p-4 rounded-lg shadow-md">
        <Profile />
        {/* 🔹 네비게이션 버튼 (클릭하면 해당 페이지로 이동) */}
        <nav className="mt-4 flex flex-col gap-2">
          <Link
            href="/mypage/dashboard"
            className="w-full px-4 py-2 text-left text-sm font-semibold text-gray-700 bg-gray-100 rounded hover:bg-gray-200"
          >
            내 활동 기록
          </Link>
          <Link
            href="/mypage/settings"
            className="w-full px-4 py-2 text-left text-sm font-semibold text-gray-700 bg-gray-100 rounded hover:bg-gray-200"
          >
            설정
          </Link>
        </nav>
      </aside>

      {/* ✅ 메인 콘텐츠 영역 (라우팅된 페이지가 렌더링될 곳) */}
      <main className="w-full lg:w-3/4 bg-card p-6 rounded-lg shadow-md">
        {children}
      </main>
    </div>
  );
}
