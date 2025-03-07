import type { Metadata } from "next";
import QueryProvider from "@/providers/QueryProvider";
import Header from "./_components/Header";
import Footer from "./_components/Footer";
import BottomNav from "./_components/BottomNav";
import "./globals.css";

export const metadata: Metadata = {
  title: "Cram",
  description: "CS 퀴즈 플랫폼",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <QueryProvider>
        <body className={`antialiased`}>
          <Header />
          <main className="pt-16 pb-16 min-h-screen">{children}</main>
          <BottomNav />
          <Footer />
        </body>
      </QueryProvider>
    </html>
  );
}
