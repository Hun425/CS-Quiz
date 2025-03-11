"use client";

import { useRouter } from "next/navigation";
import {
  Book,
  Brain,
  Rocket,
  Star,
  ChevronRight,
  Sparkles,
} from "lucide-react";
import {
  Gamepad2,
  BarChart,
  CheckCircle,
  Code,
  CalendarDays,
} from "lucide-react";
import Button from "./_components/Button";

const CramMainPage = () => {
  const categories = [
    {
      name: "자료구조",
      icon: <Book className="text-primary" />,
      color: "bg-primary/10",
      href: "/quiz/data-structure",
    },
    {
      name: "알고리즘",
      icon: <Brain className="text-secondary" />,
      color: "bg-secondary/10",
      href: "/quiz/data-structure",
    },
    {
      name: "네트워크",
      icon: <Rocket className="text-success" />,
      color: "bg-success/10",
      href: "/quiz/data-structure",
    },
    {
      name: "데이터베이스",
      icon: <Star className="text-warning" />,
      color: "bg-warning/10",
      href: "/quiz/data-structure",
    },
  ];

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
      href: "/dashboard",
    },
    {
      name: "맞춤형 추천",
      description:
        "사용자의 수준과 관심사에 따라 맞춤형 퀴즈를 추천해 드립니다.",
      icon: <Code size={36} className="text-green-600" />,
      color: "from-green-50 to-green-100",
      href: "/quizzes/custom-quiz",
    },
  ];
  const router = useRouter();

  return (
    <div className="min-h-screen max-w-screen-full px-4 md:px-8 lg:px-20 mt-8">
      <section className="bg-background border border-card-border shadow-sm max-w-screen-2xl mx-auto text-foreground p-12 rounded-xl shadow-lg flex flex-col items-center text-center">
        <h1 className="text-5xl font-bold mb-4 text-primary drop-shadow-md">
          쉽고 재미있는 CS 퀴즈 학습
        </h1>
        <p className="text-xl text-neutral max-w-3xl leading-relaxed">
          <strong>실시간 경쟁</strong>과 <strong>퀴즈 챌린지</strong>로 CS
          지식을 쌓아보세요. <br />
          재미있게 배우고, 빠르게 성장하세요.
        </p>
        <Button
          variant="primary"
          size="large"
          className="mt-6 px-6 py-3 font-semibold "
          onClick={() => router.push("/login")}
        >
          로그인하고 시작하기 🚀
        </Button>
      </section>

      <section className="max-w-screen-2xl mx-auto my-16 ">
        <h1 className="text-3xl font-bold text-center mb-8">
          <strong className="text-primary">CRAM</strong>의 특별한 학습법
        </h1>

        <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4 text-default">
          {features.map((feature, index) => (
            <div
              key={index}
              className={`bg-gradient-to-br ${feature.color} p-6 rounded-xl shadow-md hover:shadow-xl transition-all transform hover:-translate-y-2 hover:rotate-1 duration-300 cursor-pointer`}
            >
              <div
                className="flex flex-col items-center space-y-3 text-default"
                onClick={() => router.push(feature.href)}
              >
                <div className="bg-white p-3 rounded-full shadow-md">
                  {feature.icon}
                </div>
                <h3 className="text-lg font-semibold">{feature.name}</h3>
                <p className="text-sm text-center">{feature.description}</p>
              </div>
            </div>
          ))}
        </div>
      </section>

      <section className="max-w-screen-xl min-h-[250px] mx-auto mb-6 flex flex-col md:flex-row gap-4">
        {/* 오늘의 퀴즈 */}
        <div className="flex-1 bg-card border-2 border-card-border p-5 rounded-xl shadow-md hover:shadow-lg transition text-center flex flex-col justify-center">
          <CalendarDays size={28} className="text-primary mx-auto mb-3" />
          <h2 className="text-lg font-semibold text-foreground mb-3">
            오늘의 퀴즈
          </h2>
          <p className="text-sm md:text-base text-neutral">
            오늘의 퀴즈가 아직 준비되지 않았습니다.
            <br />
            잠시 후 다시 확인해주세요.
          </p>
        </div>

        {/* 추천 퀴즈 */}
        <div className="flex-1 bg-card border-2 border-card-border p-5 rounded-xl shadow-md hover:shadow-lg transition text-center flex flex-col justify-center">
          <Sparkles size={28} className="text-secondary mx-auto mb-3" />
          <h2 className="text-lg font-semibold text-foreground mb-3">
            추천 퀴즈
          </h2>
          <p className="text-sm md:text-base text-neutral">
            로그인하면 맞춤형 퀴즈를 추천해드립니다.
          </p>
        </div>
      </section>

      <section className="max-w-screen-xl mx-auto mb-8">
        <h2 className="text-xl font-semibold text-foreground mb-6 text-center">
          📚 학습 카테고리
        </h2>
        <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
          {categories.map((category, index) => (
            <div
              key={index}
              className={`${category.color} p-4 rounded-lg shadow-md flex items-center justify-between hover:scale-103 hover:bg-opacity-30 hover:shadow-lg transition-transform border border-neutral`}
            >
              <div className="flex items-center space-x-3">
                <div className="bg-white p-2 rounded-full shadow">
                  {category.icon}
                </div>
                <span className="text-md font-semibold">{category.name}</span>
              </div>
              <ChevronRight className="text-neutral" />
            </div>
          ))}
        </div>
      </section>
    </div>
  );
};

export default CramMainPage;
