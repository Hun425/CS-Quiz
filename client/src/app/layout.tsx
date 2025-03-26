import type { Metadata } from "next";
import QueryProvider from "@/providers/QueryProvider";
import Header from "./_components/Header";
import Footer from "./_components/Footer";
import BottomNav from "./_components/BottomNav";
import { ToastContainer } from "./_components/Toast";
import "@/styles/globals.css";
import TokenExpirationPopup from "@/app/_components/TokenExpirationPopup";

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
          <Header />
          <main className="mt-16 min-w-full">{children}</main>
          <BottomNav />
          <Footer />
        </QueryProvider>
        <div id="toast-root" />
        <ToastContainer />
        {/* <TokenExpirationPopup /> */}
      </body>
    </html>
  );
}
