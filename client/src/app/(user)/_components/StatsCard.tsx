const StatCard = ({
  title,
  value,
  isDummy = false,
}: {
  title: string;
  value: string;
  isDummy?: boolean;
}) => {
  return (
    <div
      className={`relative p-2 rounded-md text-center border transition-all ${
        isDummy
          ? "bg-gray-100 text-gray-400 border-gray-300"
          : "bg-card-background text-foreground border-border shadow-sm"
      }`}
    >
      {/* ✅ '예시' 뱃지 표시 */}
      {isDummy && (
        <div
          className="absolute top-1 right-1 text-[10px] px-1 py-[1px] rounded bg-gray-300 text-gray-600"
          title="실제 데이터가 없어 예시 값이 표시됩니다."
        >
          예시
        </div>
      )}

      <p className="text-xs sm:text-base">{title}</p>
      <p className="text-base sm:text-lg font-bold">{value}</p>
    </div>
  );
};

export default StatCard;
