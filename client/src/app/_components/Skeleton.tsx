interface SkeletonProps {
  className?: string;
}

export default function Skeleton({ className = "" }: SkeletonProps) {
  return <div className={`animate-pulse bg-gray-300 ${className}`} />;
}
