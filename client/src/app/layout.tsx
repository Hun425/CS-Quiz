import QueryProvider from "@/providers/QueryProvider";
import Header from "./_components/Header";
import Footer from "./_components/Footer";
import BottomNav from "./_components/BottomNav";
import { ToastContainer } from "./_components/Toast";
import "@/styles/globals.css";
import { METADATA } from "@/lib/constants/seo";
import { genPageMetadata } from "@/lib/utils/metadata";

export const metadata = genPageMetadata(METADATA.home);

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
          <main className="mt-16 min-w-full ">{children}</main>
          <BottomNav />
          <Footer />
        </QueryProvider>
        <div id="toast-root" />
        <ToastContainer />
      </body>
    </html>
  );
}
