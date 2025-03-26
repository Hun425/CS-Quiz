import Link from "next/link";
import { Gamepad2, BarChart, CheckCircle, Code } from "lucide-react";

import AuthSection from "./_components/AuthSection";

const CramMainPage = () => {
  return (
    <div className="min-h-screen max-w-screen-full px-4 md:px-8 lg:px-20 pt-16">
      <AuthSection />
      <section className="max-w-screen-xl mx-auto my-16">
        <h1 className="text-3xl font-bold text-center mb-8">
          <strong className="text-primary">CRAM</strong>의 특별한 학습법
        </h1>

        <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4 text-default">
          {features.map((feature, index) => (
            <div
              key={index}
              className={`bg-gradient-to-br ${feature.color} p-6 rounded-xl shadow-md hover:shadow-xl transition-all transform hover:-translate-y-2 hover:rotate-1 duration-300 cursor-pointer`}
            >
              <Link href={`${feature.href}`}>
                <div className="flex flex-col items-center space-y-3 text-default">
                  <div className="bg-white p-3 rounded-full shadow-md">
                    {feature.icon}
                  </div>
                  <h3 className="text-lg font-semibold">{feature.name}</h3>
                  <p className="text-sm text-center">{feature.description}</p>
                </div>
              </Link>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
};

export default CramMainPage;

const features = [
  {
    name: "다양한 퀴즈 유형",
    description:
      "객관식, 참/거짓, 코드 분석 등 다양한 유형의 퀴즈를 제공합니다.",
    icon: <CheckCircle size={36} className="text-primary" />,
    color: "from-indigo-50 to-indigo-100",
    href: "/quizzes",
  },
  {
    name: "실시간 대결",
    description: "다른 사용자와 실시간으로 대결하며 지식을 겨루어 보세요.",
    icon: <Gamepad2 size={36} className="text-danger" />,
    color: "from-red-50 to-red-100",
    href: "/battles",
  },
  {
    name: "성과 추적",
    description: "자신의 성과를 추적하고 시간에 따른 향상도를 확인하세요.",
    icon: <BarChart size={36} className="text-secondary" />,
    color: "from-yellow-50 to-yellow-100",
    href: "/mypage",
  },
  {
    name: "맞춤형 추천",
    description: "사용자의 수준과 관심사에 따라 맞춤형 퀴즈를 추천해 드립니다.",
    icon: <Code size={36} className="text-green-600" />,
    color: "from-green-50 to-green-100",
    href: "/quizzes",
  },
];
