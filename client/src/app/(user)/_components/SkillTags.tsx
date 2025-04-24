const SkillTags: React.FC<{ skills: string[] }> = ({ skills }) => {
  return (
    <div className="bg-card p-6 rounded-lg shadow-md">
      <h3 className="text-lg font-bold">스킬 태그</h3>
      <div className="mt-4 flex gap-2 flex-wrap">
        {skills.map((skill) => (
          <span
            key={skill}
            className="bg-primary text-white px-3 py-1 rounded-full text-sm"
          >
            {skill}
          </span>
        ))}
      </div>
    </div>
  );
};
export default SkillTags;
