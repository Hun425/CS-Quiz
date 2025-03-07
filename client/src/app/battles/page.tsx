"use client";
import Button from "../_components/Button";

const BattlesPage: React.FC = () => {
  const activeBattles = Array.from({ length: 4 }); // 예제 데이터

  return (
    <div className="max-w-screen-2xl mx-auto p-6 space-y-6">
      {/* 헤더 */}
      <div className="bg-primary text-white p-6 rounded-lg shadow-md flex justify-between items-center">
        <div>
          <h2 className="text-2xl font-bold">퀴즈 대결</h2>
          <p className="opacity-80">
            빠른 문제 풀이와 정답률을 겨루며 승리를 차지하세요! 실시간으로
            퀴즈를 풀며 경쟁해보세요!
          </p>
        </div>
        <Button variant="primary" size="medium">
          새 대결 만들기
        </Button>
      </div>
      {/* 대결 방법 */}
      <div className="bg-card p-6 rounded-lg shadow-md">
        <h2 className="text-xl font-bold border-b-2 border-primary pb-2">
          대결 방법
        </h2>
        <ul className="list-disc list-inside text-neutral space-y-2 mt-4">
          <li>
            <strong>대결 참가:</strong> 위 목록에서 참가하려는 대결을 선택하거나
            새로운 대결을 만듭니다.
          </li>
          <li>
            <strong>준비 완료:</strong> 대결방에 입장하면{" "}
            <strong>준비완료</strong> 버튼을 클릭하여 준비 상태로 변경합니다.
          </li>
          <li>
            <strong>대결 시작:</strong> 모든 참가자가 준비 완료되면 대결이
            자동으로 시작됩니다.
          </li>
          <li>
            <strong>정답 제출:</strong> 문제를 풀어 빠르고 정확하게 답변을
            제출하세요. 정답률과 응답 시간에 따라 점수가 부여됩니다.
          </li>
        </ul>
      </div>

      {/* 활성화된 배틀룸 목록 */}
      <div className="bg-card p-6 rounded-lg shadow-md">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-bold border-b-2 border-primary pb-2">
            활성화된 대결
          </h2>
          <Button variant="outline" size="small">
            새로고침
          </Button>
        </div>
        {activeBattles.length > 0 ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {activeBattles.map((_, index) => (
              <div
                key={index}
                className="bg-white p-4 rounded-lg shadow-md flex flex-col"
              >
                <h3 className="text-lg font-semibold">배틀룸 {index + 1}</h3>
                <p className="text-neutral text-sm">문제 수: {10 + index}개</p>
                <div className="flex justify-between mt-2">
                  <p className="text-neutral text-sm">참가자: {index + 1}/4</p>
                  <Button variant="primary" size="small">
                    참가하기
                  </Button>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center text-neutral py-6">
            <p className="text-lg font-semibold">
              현재 활성화된 대결이 없습니다.
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default BattlesPage;
