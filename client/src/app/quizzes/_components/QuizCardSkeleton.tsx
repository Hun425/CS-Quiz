import Skeleton from "@/app/_components/Skeleton";

const QuizCardSkeleton: React.FC = () => {
  return (
    <div className="w-full p-4 border border-border bg-card rounded-lg shadow-md animate-pulse">
      <Skeleton className="w-3/4 h-6 rounded-md" /> {/* 제목 */}
      <div className="flex gap-2 mt-2">
        <Skeleton className="w-12 h-4 rounded-md" />
        <Skeleton className="w-16 h-4 rounded-md" />
      </div>
      <div className="flex justify-between mt-4">
        <Skeleton className="w-16 h-4 rounded-md" />
        <Skeleton className="w-10 h-4 rounded-md" />
      </div>
    </div>
  );
};

export default QuizCardSkeleton;
