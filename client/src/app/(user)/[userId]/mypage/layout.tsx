"use client";

import Profile from "../../_components/Profile";

export default function Layout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <div className="max-w-screen-xl w-full mx-auto px-4 sm:px-8 lg:px-16 py-8 sm:py-12 lg:py-16 flex flex-col lg:flex-row gap-6 min-h-screen">
      <aside className="w-full lg:w-1/4 bg-card rounded-md">
        <Profile />
      </aside>

      {/* ✅ 메인 콘텐츠 */}
      <main className="w-full lg:w-3/4 bg-card p-6 sm:p-8 shadow-md border border-border rounded-md">
        {children}
      </main>
    </div>
  );
}
