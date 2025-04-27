import Link from "next/link";
import Image from "next/image";
import ThemeToggle from "./ThemeToggle";
import LoginButton from "./LoginButton";

const Header: React.FC = () => {
  return (
    <header className="w-full h-[64px] fixed top-0 left-0 right-0 z-50 bg-background border-b border-border">
      <div className="max-w-screen-full mx-auto flex items-center justify-between h-full px-4 md:px-6 lg:px-8">
        {/* 왼쪽 로고 */}
        <div className="flex items-center gap-2 flex-shrink-0">
          <Link href="/" className="flex items-center space-x-2">
            <Image
              src="/images/logo.png"
              width={30}
              height={30}
              alt="CRAM Logo"
              className="rounded-full"
            />
            <h1 className="font-primary text-xl sm:text-2xl font-bold tracking-tight">
              CRAM
            </h1>
          </Link>
          <ThemeToggle />
        </div>

        {/* 가운데 네비게이션 */}
        <nav className="hidden md:flex items-center justify-center gap-4 flex-1 px-2 text-sm">
          <Link
            href="/quizzes"
            className="text-foreground font-bold hover:scale-105"
          >
            퀴즈
          </Link>
          <Link
            href="/battles"
            className="text-primary font-bold hover:scale-105"
          >
            실시간 퀴즈 대결
          </Link>
        </nav>

        {/* 로그인 버튼 */}
        <div className="flex items-center justify-end flex-shrink-0">
          <LoginButton />
        </div>
      </div>
    </header>
  );
};

export default Header;
