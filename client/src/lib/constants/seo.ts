import { IMetadata } from "@/lib/types/meta";

export const SITE_URL = "https://cram.co.kr";
export const DEFAULT_OG_IMAGE = `${SITE_URL}/images/logo.png`;

export const METADATA: Record<string, IMetadata> = {
  home: {
    title: {
      default: "Cram",
      template: "%s | Cram",
    },
    description: "벼락치기 CS 퀴즈 플랫폼 - 실시간으로 공부하고 실력 점검까지!",
    openGraph: {
      title: "Cram - 벼락치기 CS 퀴즈 플랫폼",
      description: "퀴즈로 전공 완전 정복! 실시간으로 CS 퀴즈를 풀어보세요.",
      url: SITE_URL,
      siteName: "Cram",
      images: [
        {
          url: DEFAULT_OG_IMAGE,
          width: 1200,
          height: 630,
          alt: "Cram 로고 이미지",
        },
      ],
      locale: "ko_KR",
      type: "website",
    },
    twitter: {
      card: "summary_large_image",
      title: "Cram - 벼락치기 CS 퀴즈 플랫폼",
      description: "전공시험, 취업 준비 모두 Cram 하나로 끝!",
      images: [DEFAULT_OG_IMAGE],
    },
  },
  login: {
    title: "로그인",
    description:
      "소셜 계정으로 간편하게 로그인하고 Cram의 모든 기능을 즐겨보세요!",
    openGraph: {
      title: "로그인 | Cram",
      description: "Google, Kakao 등 소셜 계정으로 간편하게 로그인하세요.",
      url: `${SITE_URL}/login`,
      siteName: "Cram",
      images: [
        {
          url: `${SITE_URL}/images/og-login.png`,
          width: 1200,
          height: 630,
          alt: "Cram 로그인 페이지",
        },
      ],
      locale: "ko_KR",
      type: "website",
    },
    twitter: {
      card: "summary_large_image",
      title: "로그인 | Cram",
      description: "Cram에 로그인하고 다양한 CS 퀴즈 기능을 이용해보세요!",
      images: [`${SITE_URL}/images/og-login.png`],
    },
  },

  quizList: {
    title: "퀴즈 목록 | Cram",
    description: "다양한 주제와 난이도의 퀴즈를 검색하고 풀어보세요!",
    openGraph: {
      title: "퀴즈 목록 | Cram",
      description: "지금 다양한 분야의 퀴즈를 탐색해보세요!",
      url: `${SITE_URL}/quizzes`,
      siteName: "Cram",
      images: [
        {
          url: `${SITE_URL}/images/og-quiz.png`,
          width: 1200,
          height: 630,
          alt: "퀴즈 검색 페이지 이미지",
        },
      ],
      locale: "ko_KR",
      type: "website",
    },
    twitter: {
      card: "summary_large_image",
      title: "퀴즈 목록 | Cram",
      description: "주제별 퀴즈를 검색하고 원하는 퀴즈를 풀어보세요.",
      images: [`${SITE_URL}/images/og-quiz.png`],
    },
  },

  battles: {
    title: "실시간 퀴즈 대결",
    description:
      "다른 사용자들과 속도와 정확도를 겨루며 실시간으로 퀴즈 대결을 즐겨보세요!",
    openGraph: {
      title: "실시간 퀴즈 대결 | Cram",
      description: "지금 바로 대결을 시작해보세요!",
      url: `${SITE_URL}/battles`,
      siteName: "Cram",
      images: [
        {
          url: `${SITE_URL}/images/og-battle.png`,
          width: 1200,
          height: 630,
          alt: "Cram 퀴즈 대결 페이지",
        },
      ],
      locale: "ko_KR",
      type: "website",
    },
    twitter: {
      card: "summary_large_image",
      title: "실시간 퀴즈 대결 | Cram",
      description: "실시간으로 유저들과 퀴즈를 풀고 경쟁해보세요!",
      images: [`${SITE_URL}/images/og-battle.png`],
    },
  },
};
