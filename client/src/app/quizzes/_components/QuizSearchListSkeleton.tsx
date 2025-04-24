// app/quizzes/_components/QuizSearchListSkeleton.tsx
export default function QuizSearchListSkeleton() {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
      {Array.from({ length: 6 }).map((_, index) => (
        <div
          key={index}
          className="border border-border rounded-lg p-6 bg-card-background shadow-md animate-pulse"
        >
          {/* 제목 */}
          <div className="h-6 bg-muted/30 rounded w-3/4 mb-4 shimmer" />

          {/* 메타 정보 (3개) */}
          <div className="flex gap-2 mb-4">
            <div className="h-6 w-16 rounded-full bg-muted/20 shimmer" />
            <div className="h-6 w-16 rounded-full bg-muted/20 shimmer" />
            <div className="h-6 w-20 rounded-full bg-muted/20 shimmer" />
          </div>

          {/* 태그 목록 */}
          <div className="flex flex-wrap gap-2 mb-4">
            {Array.from({ length: 3 }).map((_, i) => (
              <div
                key={i}
                className="h-6 w-14 rounded-full bg-muted/10 shimmer"
              />
            ))}
          </div>

          {/* 시도 횟수 + 평균 점수 */}
          <div className="h-4 bg-muted/20 rounded w-1/2 shimmer" />
        </div>
      ))}
    </div>
  );
}
