const Sidebar = () => {
  return (
    <aside className="hidden lg:block w-1/4 bg-card border border-border p-6 rounded-lg shadow-md">
      <h2 className="text-lg font-semibold mb-4 text-primary">
        📢 로그인하고 연습을 시작하세요!
      </h2>
      <button className="w-full py-2 bg-primary text-white rounded-md">
        로그인
      </button>

      {/* 로그인 혜택 설명 추가 */}
      <p className="text-sm text-muted mt-3">
        🔥 로그인하면 나에게 맞춘{" "}
        <span className="font-semibold text-primary">추천 퀴즈</span>를 받을 수
        있어요.
        <br />
        📊 내 학습량을 분석하고{" "}
        <span className="font-semibold text-primary">취약한 개념</span>을 보완해
        보세요!
      </p>

      <h3 className="text-md font-semibold mt-6 mb-3">📚 추천 코스</h3>
      <ul>
        <li>🔹 백엔드 개발</li>
        <li>🔹 자바 중급</li>
        <li>🔹 데이터 엔지니어링</li>
      </ul>

      <h3 className="text-md font-semibold mt-6 mb-3">💼 추천 포지션</h3>
      <ul>
        <li>💼 미들급 백엔드 개발자</li>
        <li>💼 웹 프론트엔드/백엔드 개발자</li>
      </ul>
    </aside>
  );
};

export default Sidebar;
