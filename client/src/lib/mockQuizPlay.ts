export const mockQuizPlay = {
  id: 1,
  title: "JavaScript 기본 개념 퀴즈",
  description: "자바스크립트 기초 개념을 테스트하는 퀴즈입니다.",
  timeLimit: 10, // 분
  questions: [
    {
      id: 101,
      questionText: "JavaScript에서 변수를 선언하는 키워드는?",
      options: ["var", "let", "const", "function"],
      questionType: "MULTIPLE_CHOICE",
    },
    {
      id: 102,
      questionText: "자바스크립트에서 `==`와 `===`의 차이는?",
      options: ["같음", "===가 타입까지 비교", "==이 더 엄격함"],
      questionType: "MULTIPLE_CHOICE",
    },
  ],
};
