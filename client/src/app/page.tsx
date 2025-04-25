import Link from "next/link";
import { Gamepad2, BarChart, CheckCircle, Code } from "lucide-react";
import classNames from "classnames";
import AuthSection from "./_components/AuthSection";

const CramMainPage = () => {
  return (
    <div className="bg-background-gradient min-h-screen max-w-screen-full px-4 md:px-8 lg:px-20 py-8 text-white transition-colors duration-300">
      <AuthSection />
      <section className="max-w-screen-xl mx-auto my-16">
        <h1 className="text-3xl foreground font-bold text-center mb-8">
          <div className="text-foreground">
            <strong>CRAM</strong>의 특별한 학습법
          </div>
        </h1>

        <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4 text-default">
          {features.map((feature, index) => (
            <Link href={feature.href} key={index}>
              <div
                className={classNames(
                  "rounded-xl border border-border bg-card-background hover:border-primary transition-colors duration-200",
                  "p-6 space-y-3 cursor-pointer group"
                )}
              >
                <div className="text-primary group-hover:text-primary-hover transition">
                  {feature.icon}
                </div>
                <h3 className="text-[15px] font-semibold text-foreground">
                  {feature.name}
                </h3>
                <p className="text-sm text-muted leading-relaxed">
                  {feature.description}
                </p>
              </div>
            </Link>
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
