// lib/constants/seo.ts
export const SITE_NAME = "Cram";
export const BASE_URL =
  "http://ec2-13-125-187-28.ap-northeast-2.compute.amazonaws.com"; // 도메인 확정되면 여기만 변경
export const OG_IMAGE = `${BASE_URL}/images/logo.png`;

export const DEFAULT_SEO = {
  title: SITE_NAME,
  description: "벼락치기 CS 퀴즈 플랫폼 - 실시간으로 공부하고 실력 점검까지!",
  openGraph: {
    title: SITE_NAME,
    description: "퀴즈로 전공 완전 정복! 실시간으로 CS 퀴즈를 풀어보세요.",
    url: BASE_URL,
    siteName: SITE_NAME,
    images: [{ url: OG_IMAGE, width: 1200, height: 630 }],
    type: "website",
    locale: "ko_KR",
  },
  twitter: {
    card: "summary_large_image",
    title: SITE_NAME,
    description: "전공시험, 취업 준비 모두 Cram 하나로 끝!",
    images: [OG_IMAGE],
  },
};
