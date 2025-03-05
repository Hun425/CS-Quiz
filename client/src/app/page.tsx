"use client";
import { Book, Brain, Rocket, Star, ChevronRight } from "lucide-react";
import { Gamepad2, BarChart, CheckCircle, Code } from "lucide-react";
import Button from "./_components/Button";

const CramMainPage = () => {
  const categories = [
    {
      name: "자료구조",
      icon: <Book className="text-primary" />, // 새로운 테마 색상 적용
      color: "bg-primary/10", // 투명한 핑크 배경
    },
    {
      name: "알고리즘",
      icon: <Brain className="text-secondary" />, // 오렌지 색상
      color: "bg-secondary/10", // 투명한 오렌지 배경
    },
    {
      name: "네트워크",
      icon: <Rocket className="text-success" />, // 라임 그린
      color: "bg-success/10", // 투명한 라임 배경
    },
    {
      name: "데이터베이스",
      icon: <Star className="text-warning" />, // 노랑 색상
      color: "bg-warning/10", // 투명한 노랑 배경
    },
  ];

  const features = [
    {
      name: "다양한 퀴즈 유형",
      description:
        "객관식, 참/거짓, 코드 분석 등 다양한 유형의 퀴즈를 제공합니다.",
      icon: <CheckCircle size={36} className="text-primary" />,
      color: "from-indigo-50 to-indigo-100",
    },
    {
      name: "실시간 대결",
      description: "다른 사용자와 실시간으로 대결하며 지식을 겨루어 보세요.",
      icon: <Gamepad2 size={36} className="text-danger" />,
      color: "from-red-50 to-red-100",
    },
    {
      name: "성과 추적",
      description: "자신의 성과를 추적하고 시간에 따른 향상도를 확인하세요.",
      icon: <BarChart size={36} className="text-secondary" />,
      color: "from-yellow-50 to-yellow-100",
    },
    {
      name: "맞춤형 추천",
      description:
        "사용자의 수준과 관심사에 따라 맞춤형 퀴즈를 추천해 드립니다.",
      icon: <Code size={36} className="text-green-600" />,
      color: "from-green-50 to-green-100",
    },
  ];

  return (
    <div className="min-h-screen px-4 md:px-8 lg:px-20">
      {/* 개선된 소개 섹션 */}
      <section className="max-w-screen-lg mx-auto bg-linear-to-r/oklch from-indigo-500 to-teal-400 text-white p-8 md:p-12 rounded-xl shadow-lg mb-6 flex flex-col items-center text-center animate-fadeIn">
        <Star className="text-warning text-4xl mb-4 animate-pulse" />{" "}
        {/* 별 모양 아이콘 추가, 경쾌한 애니메이션 */}
        <h1 className="text-4xl md:text-5xl font-bold mb-4 drop-shadow-md">
          CS 퀴즈 플랫폼, Cram
        </h1>
        <p className="text-xl md:text-2xl mb-8 leading-relaxed">
          벼락치기처럼 빠르고 재미있게 컴퓨터 과학 지식을 마스터하세요!
          실시간으로 다른 사용자와 경쟁하며 실력을 폭발적으로 향상시켜 보세요.
          지금 시작!
        </p>
        <Button
          variant="primary"
          size="large"
          onClick={() => console.log("Login clicked!")}
        >
          로그인하고 시작하기
        </Button>
      </section>

      <section className="max-w-screen-lg mx-auto my-16 px-4">
        <h2 className="text-3xl font-bold text-center text-foreground mb-8">
          Cram의 특별한 벼락치기 특징
        </h2>
        <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4">
          {features.map((feature, index) => (
            <div
              key={index}
              className={`bg-gradient-to-br ${feature.color} p-6 rounded-xl shadow-md hover:shadow-xl transition-all transform hover:-translate-y-2 hover:rotate-1 duration-300 cursor-pointer`}
            >
              <div className="flex flex-col items-center space-y-3">
                <div className="bg-white p-3 rounded-full shadow-md">
                  {feature.icon}
                </div>
                <h3 className="text-lg font-semibold text-foreground">
                  {feature.name}
                </h3>
                <p className="text-sm text-neutral text-center leading-relaxed">
                  {feature.description}
                </p>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* 오늘의 퀴즈 & 추천 퀴즈 */}
      <section className="max-w-screen-lg mx-auto mb-6 flex flex-col md:flex-row gap-4">
        <div className="flex-1 bg-background/10 p-4 rounded-lg shadow-md text-center">
          <h2 className="text-lg font-semibold text-foreground mb-2">
            오늘의 벼락치기 퀴즈
          </h2>
          <p className="text-sm md:text-base">
            오늘의 퀴즈가 아직 준비되지 않았습니다. 잠시 후 다시 확인해주세요.
          </p>
        </div>
        <div className="flex-1 bg-background/10 p-4 rounded-lg shadow-md text-center">
          <h2 className="text-lg font-semibold text-foreground mb-2">
            추천 벼락치기 퀴즈
          </h2>
          <p className="text-sm md:text-base">
            로그인하면 맞춤형 퀴즈를 추천해드립니다.
          </p>
        </div>
      </section>

      {/* 학습 카테고리 */}
      <section className="max-w-screen-lg mx-auto mb-6">
        <h2 className="text-lg font-semibold text-foreground mb-4">
          벼락치기 학습 카테고리
        </h2>
        <div className="grid grid-cols-2 gap-3 md:grid-cols-4 md:gap-4">
          {categories.map((category, index) => (
            <div
              key={index}
              className={`${category.color} p-3 md:p-4 rounded-xl shadow-md flex items-center justify-between hover:scale-105 transition-transform`}
            >
              <div className="flex items-center space-x-2 md:space-x-3">
                {category.icon}
                <span className="text-sm md:text-base font-medium">
                  {category.name}
                </span>
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
