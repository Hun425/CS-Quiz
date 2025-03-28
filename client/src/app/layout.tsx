import type { Metadata } from "next";
import QueryProvider from "@/providers/QueryProvider";
import Header from "./_components/Header";
import Footer from "./_components/Footer";
import BottomNav from "./_components/BottomNav";
import { ToastContainer } from "./_components/Toast";
import "@/styles/globals.css";

export async function generateMetadata(): Promise<Metadata> {
  const siteUrl =
    process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:3000";
  const imageUrl = `${siteUrl}/images/logo.png`;

  return {
    title: {
      default: "Cram",
      template: "%s | Cram",
    },
    description: "벼락치기 CS 퀴즈 플랫폼 - 실시간으로 공부하고 실력 점검까지!",
    openGraph: {
      title: "Cram - 벼락치기 CS 퀴즈 플랫폼",
      description: "퀴즈로 전공 완전 정복! 실시간으로 CS 퀴즈를 풀어보세요.",
      url: siteUrl,
      siteName: "Cram",
      images: [
        {
          url: imageUrl,
          width: 1200,
          height: 630,
        },
      ],
      locale: "ko_KR",
      type: "website",
    },
    twitter: {
      card: "summary_large_image",
      title: "Cram - 벼락치기 CS 퀴즈 플랫폼",
      description: "전공시험, 취업 준비 모두 Cram 하나로 끝!",
      images: [imageUrl],
    },
  };
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body className={`antialiased`}>
        <QueryProvider>
          <Header />
          <main className="mt-16 min-w-full">{children}</main>
          <BottomNav />
          <Footer />
        </QueryProvider>
        <div id="toast-root" />
        <ToastContainer />
      </body>
    </html>
  );
}
