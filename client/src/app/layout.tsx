import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import Header from "./_components/Header";
import Footer from "./_components/Footer";
import BottomNav from "./_components/BottomNav";
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Cram",
  description: "CS 퀴즈 플랫폼폼",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased`}
      >
        <Header />
        <main className="pt-16 pb-16">{children}</main>
        <BottomNav />
        <Footer />
      </body>
    </html>
  );
}
