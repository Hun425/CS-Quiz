import React from "react";
import { Book, Brain } from "lucide-react";

const BottomNav = () => {
  return (
    <>
      <nav className="fixed bottom-0 left-0 right-0 bg-white shadow-2xl p-3 md:hidden">
        <div className="flex justify-around">
          <button className="flex flex-col items-center text-purple-600">
            <Book size={22} />
            <span className="text-xs">학습</span>
          </button>
          <button className="flex flex-col items-center text-gray-500">
            <Brain size={22} />
            <span className="text-xs">퀴즈</span>
          </button>
        </div>
      </nav>
    </>
  );
};

export default BottomNav;
