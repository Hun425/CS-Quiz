import Image from "next/image";
import Link from "next/link";
import { Github, Twitter, Linkedin } from "lucide-react";

const Footer = () => {
  return (
    <footer className="bg-footer py-8">
      <div className="max-w-screen-2xl mx-auto px-6 md:px-8 lg:px-12">
        <div className="flex flex-col md:flex-row justify-center md:justify-between items-center md:items-start gap-y-6">
          {/* CRAM 로고 & 소개 */}
          <div className="flex-1 flex flex-col items-center md:items-start text-center md:text-left">
            <Image
              src="/images/logo.png"
              width={40}
              height={40}
              alt="CRAM Logo"
            />
            <h2 className="text-xl font-bold text-primary mt-2">CRAM</h2>
            <p className="text-sm  mt-1 text-foreground">
              컴퓨터 과학 지식을 테스트하고 향상하세요.
            </p>
          </div>

          {/* 퀵 링크 */}
          <nav className="flex-1 flex flex-col items-center text-foreground md:items-start space-y-2">
            <h3 className="text-lg font-semibold text-foreground">퀵 링크</h3>
            <Link href="/" className="hover:text-primary transition">
              홈
            </Link>
            <Link href="/quizzes" className="hover:text-primary transition">
              퀴즈
            </Link>

            <Link
              href="/battles"
              className="text-primary font-bold hover:text-red-700 transition"
            >
              실시간 퀴즈 대결
            </Link>
          </nav>

          {/* 소셜 미디어 */}
          <div className="flex-1 flex flex-col items-center md:items-start space-y-2">
            <h3 className="text-lg font-semibold ">소셜 미디어</h3>
            <div className="flex space-x-4">
              <Link
                href="https://github.com"
                target="_blank"
                rel="noopener noreferrer"
                className="hover:text-gray-900 transition"
              >
                <Github size={20} />
              </Link>
              <Link
                href="https://twitter.com"
                target="_blank"
                rel="noopener noreferrer"
                className="hover:text-blue-500 transition"
              >
                <Twitter size={20} />
              </Link>
              <Link
                href="https://linkedin.com"
                target="_blank"
                rel="noopener noreferrer"
                className="hover:text-blue-700 transition"
              >
                <Linkedin size={20} />
              </Link>
            </div>
          </div>
        </div>

        {/* 하단 저작권 정보 */}
        <div className="mt-8 border-t border-gray-300 pt-4 text-center text-sm text-foreground">
          © {new Date().getFullYear()} CRAM. All rights reserved.
        </div>
      </div>
    </footer>
  );
};

export default Footer;
