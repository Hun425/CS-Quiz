import Link from "next/link";
import Image from "next/image";
import ThemeToggle from "./ThemeToggle";
import SearchBar from "./SearchBar";
import LoginButton from "./LoginButton";

const Header: React.FC = () => {
  return (
    <header className="w-full h-[64px] fixed top-0 left-0 right-0 z-50 bg-background  border-b border-border">
      <div className="max-w-screen-2xl mx-auto flex items-center justify-between h-full px-4 md:px-6 lg:px-8">
        {/* 로고 */}
        <div className="flex space-x-2">
          <Link href={"/"} className="flex items-center space-x-2">
            <Image
              src="/images/logo.png"
              width={30}
              height={30}
              alt="CRAM Logo"
              className="rounded-full"
            />
            <h1 className="font-primary text-2xl font-bold tracking-tight">
              CRAM
            </h1>
          </Link>
          <ThemeToggle />
        </div>

        {/* 메뉴 & 검색 */}
        <div className=" flex items-center space-x-4 flex-1 justify-center ">
          {/* 검색 */}
          <div className="hidden sm:flex w-full sm:max-w-sm md:max-w-md lg:max-w-lg">
            <SearchBar />
          </div>

          {/* 메뉴 */}
          <nav className="hidden lg:flex space-x-4">
            <Link
              href="/quizzes"
              className="text-foreground font-semibold hover:scale-105"
            >
              퀴즈
            </Link>
            <Link
              href="/battles"
              className="text-primary font-semibold hover:scale-105"
            >
              실시간 퀴즈 대결
            </Link>
          </nav>
        </div>
        <LoginButton />
      </div>
    </header>
  );
};

export default Header;
