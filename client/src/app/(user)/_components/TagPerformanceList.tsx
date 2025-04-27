const TagPerformanceList = ({
  title,
  icon,
  colorClass,
  isStrength,
  items,
}: {
  title: string;
  icon: React.ReactNode;
  colorClass: string;
  isStrength: boolean;
  items: {
    tagId: number;
    tagName: string;
    correctRate: number;
    strength: boolean;
  }[];
}) => {
  const filtered = items.filter((tp) => tp.strength === isStrength).slice(0, 3);

  return (
    <div className="rounded-xl p-3 sm:p-4 border shadow-sm transition">
      <h3
        className={`text-md font-semibold flex items-center gap-2 ${colorClass}`}
      >
        {icon} {title}
      </h3>
      {filtered.length > 0 ? (
        <ul className="mt-2">
          {filtered.map((topic) => (
            <li key={topic.tagId} className="text-sm">
              {isStrength ? "✅" : "❌"} {topic.tagName} (정답률:{" "}
              {topic.correctRate}%)
            </li>
          ))}
        </ul>
      ) : (
        <p className="text-muted text-sm">{title}가 없습니다.</p>
      )}
    </div>
  );
};

export default TagPerformanceList;
