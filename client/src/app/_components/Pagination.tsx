import React from "react";

const Pagination = () => {
  return (
    <div className="flex items-center justify-between border-t border-border bg-card px-4 py-3 sm:px-6">
      {/* 📌 모바일 네비게이션 (이전/다음 버튼) */}
      <div className="flex flex-1 justify-between sm:hidden">
        <button className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-200 rounded-md shadow hover:bg-gray-300">
          Previous
        </button>
        <button className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-200 rounded-md shadow hover:bg-gray-300">
          Next
        </button>
      </div>

      {/* 📌 데스크탑 페이지네이션 */}
      <div className="hidden sm:flex sm:flex-1 sm:items-center sm:justify-between">
        <p className="text-sm text-gray-700">
          Showing <span className="font-medium">1</span> to{" "}
          <span className="font-medium">10</span> of{" "}
          <span className="font-medium">97</span> results
        </p>

        <nav
          className="isolate inline-flex -space-x-px rounded-md shadow-sm"
          aria-label="Pagination"
        >
          {/* 🔹 이전 페이지 버튼 */}
          <button className="relative inline-flex items-center rounded-l-md px-3 py-2 text-gray-500 bg-gray-100 ring-1 ring-inset ring-gray-300 hover:bg-gray-200">
            <span className="sr-only">Previous</span>
            <svg className="size-5" viewBox="0 0 20 20" fill="currentColor">
              <path
                fillRule="evenodd"
                d="M11.78 5.22a.75.75 0 0 1 0 1.06L8.06 10l3.72 3.72a.75.75 0 1 1-1.06 1.06l-4.25-4.25a.75.75 0 0 1 0-1.06l4.25-4.25a.75.75 0 0 1 1.06 0Z"
                clipRule="evenodd"
              />
            </svg>
          </button>

          {/* 🔹 페이지 숫자 버튼 */}
          {[1, 2, 3, "...", 8, 9, 10].map((num, index) =>
            num === "..." ? (
              <span
                key={index}
                className="px-4 py-2 text-sm font-semibold text-gray-500 ring-1 ring-inset ring-gray-300"
              >
                {num}
              </span>
            ) : (
              <button
                key={index}
                className={`px-4 py-2 text-sm font-semibold ${
                  num === 1
                    ? "bg-indigo-600 text-white"
                    : "text-gray-900 bg-gray-100"
                } ring-1 ring-inset ring-gray-300 hover:bg-gray-200`}
              >
                {num}
              </button>
            )
          )}

          {/* 🔹 다음 페이지 버튼 */}
          <button className="relative inline-flex items-center rounded-r-md px-3 py-2 text-gray-500 bg-gray-100 ring-1 ring-inset ring-gray-300 hover:bg-gray-200">
            <span className="sr-only">Next</span>
            <svg className="size-5" viewBox="0 0 20 20" fill="currentColor">
              <path
                fillRule="evenodd"
                d="M8.22 5.22a.75.75 0 0 1 1.06 0l4.25 4.25a.75.75 0 0 1 0 1.06l-4.25 4.25a.75.75 0 0 1-1.06-1.06L11.94 10 8.22 6.28a.75.75 0 0 1 0-1.06Z"
                clipRule="evenodd"
              />
            </svg>
          </button>
        </nav>
      </div>
    </div>
  );
};

export default Pagination;
