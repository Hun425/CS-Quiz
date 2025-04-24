import { Award, BookOpen } from "lucide-react";

const RecommendSection = () => {
  return (
    <div>
      {/* 로드맵 & 추천 채용 */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="bg-card p-6 rounded-lg shadow-md flex items-center gap-4">
          <BookOpen size={40} className="text-primary" />
          <div>
            <h3 className="text-lg font-bold">로드맵</h3>
            <p className="text-neutral">Java 개발자를 위한 실전 코스 진행 중</p>
          </div>
        </div>
        <div className="bg-card p-6 rounded-lg shadow-md flex items-center gap-4">
          <Award size={40} className="text-warning" />
          <div>
            <h3 className="text-lg font-bold">추천 채용</h3>
            <p className="text-neutral">데이터 엔지니어 (Python, Node.js)</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RecommendSection;
