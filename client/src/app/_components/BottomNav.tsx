"react";
import Link from "next/link";
import { ClipboardList, Sword, User } from "lucide-react";

const BottomNav = () => {
  return (
    <nav className="fixed bottom-0 left-0 right-0 bg-white shadow-lg p-3 rounded-t-xl md:hidden border-t border-border z-50">
      <div className="flex justify-around">
        {/* 퀴즈 */}
        <Link
          href={"/quizzes"}
          className="flex flex-col items-center text-default hover:text-primary transition"
        >
          <ClipboardList size={22} />
          <span className="text-xs">퀴즈</span>
        </Link>

        {/* 배틀 */}
        <Link
          href="/battles"
          className="flex flex-col items-center text-default hover:text-primary transition"
        >
          <Sword size={22} />
          <span className="text-xs">배틀</span>
        </Link>

        {/* 마이페이지 */}
        <Link
          href="/mypage"
          className="flex flex-col items-center text-default hover:text-primary transition"
        >
          <User size={22} />
          <span className="text-xs">마이페이지</span>
        </Link>
      </div>
    </nav>
  );
};

export default BottomNav;
