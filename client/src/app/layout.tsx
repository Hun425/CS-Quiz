import type { Metadata } from "next";
import QueryProvider from "@/providers/QueryProvider";
import { ToastProvider } from "@/providers/ToastProvider";
import Header from "./_components/Header";
import Footer from "./_components/Footer";
import BottomNav from "./_components/BottomNav";
import "@/styles/globals.css";

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
    <html lang="en">
      <body className={`antialiased`}>
        <QueryProvider>
          <ToastProvider>
            <Header />
            <main className="my-16">{children}</main>
            <BottomNav />
            <Footer />
          </ToastProvider>
        </QueryProvider>
        <div id="toast-root" />
      </body>
    </html>
  );
}
