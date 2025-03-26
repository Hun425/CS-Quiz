import useTokenExpirationWarning from "@/lib/hooks/useTokenExpirationWarning";

const TokenExpirationPopup = () => {
  const { showPopup, handleRefreshToken } = useTokenExpirationWarning();

  if (!showPopup) return null;

  return (
    <div className="fixed top-0 left-0 w-full h-full bg-black bg-opacity-50 flex justify-center items-center">
      <div className="bg-white p-4 rounded shadow-lg">
        <p>토큰이 곧 만료됩니다. 연장하시겠습니까?</p>
        <button
          className="bg-blue-500 text-white p-2 mt-2"
          onClick={handleRefreshToken}
        >
          토큰 연장하기
        </button>
      </div>
    </div>
  );
};

export default TokenExpirationPopup;
