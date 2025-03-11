const GrowthLog: React.FC<{ logs: string[] }> = ({ logs }) => {
  return (
    <div className="bg-card p-6 rounded-lg shadow-md">
      <h3 className="text-lg font-bold">성장 로그</h3>
      <ul className="mt-4 space-y-2 text-neutral">
        {logs.map((log, index) => (
          <li key={index}>✅ {log}</li>
        ))}
      </ul>
    </div>
  );
};
export default GrowthLog;
