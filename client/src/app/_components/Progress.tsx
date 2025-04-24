interface ProgressProps {
  value: number;
}

const Progress: React.FC<ProgressProps> = ({ value }) => {
  return (
    <div className="relative w-full h-4 bg-white rounded-full overflow-hidden border border-gray-300">
      <div
        className="h-full bg-primary transition-all duration-300"
        style={{ width: `${value}%` }}
      />
      <span className="absolute inset-0 flex items-center justify-center text-xs text-primary font-semibold">
        {Math.round(value)}%
      </span>
    </div>
  );
};

export default Progress;
