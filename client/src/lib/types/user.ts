/**
 * 📌 전체사용자 인터페이스
 * @param userId - 조회할 사용자 ID (me일 경우 생략 가능)
 */

/**
 * @api /api/users/me/profile
 * @api /api/users/{userID}/profile
 * @response UserProfile (사용자 프로필 정보)
 */
export interface UserProfile {
  id: number; // 사용자 ID
  username: string; // 사용자 닉네임
  email?: string; // 이메일 주소
  profileImage?: string | null; // 프로필 이미지 URL (없을 경우 null)
  level: number; // 현재 레벨
  experience: number; // 현재 경험치
  requiredExperience: number; // 다음 레벨까지 필요한 경험치
  totalPoints: number; // 총 포인트
  joinedAt: string; // 가입 날짜 (ISO 8601 형식)
  lastLogin: string; // 마지막 로그인 시간 (ISO 8601 형식)
}

export interface UserProfileUpdateRequest {
  username?: string; // 사용자 닉네임
  profileImage?: string | null; // 프로필 이미지 URL (없을 경우 null)
}

/**
 * @api /api/users/me/topic-performance
 * @api /api/users/{userId}/topic-performance
 * @response TopicPerformance (특정 주제(태그)별 퀴즈 수행 성과 정보)
 */
export interface TopicPerformance {
  tagId: number /** 태그 ID */;
  tagName: string /** 태그명 (예: "알고리즘", "자료구조") */;
  quizzesTaken: number /** 해당 태그 관련 퀴즈를 푼 횟수 */;
  averageScore: number /** 평균 점수 */;
  correctRate: number /** 정답률 (0~100%) */;
  strength: boolean /** 해당 태그가 사용자의 강점인지 여부 */;
}

/**
 * @api /api/users/me/statistics
 * @api /api/users/{userId}/statistics
 * @method get
 * @response UserStatistics (사용자의 퀴즈 관련 통계 정보)
 */
export interface UserStatistics {
  totalQuizzesTaken: number /** 사용자가 푼 총 퀴즈 수 */;
  totalQuizzesCompleted: number /** 완료한 퀴즈 수 (예: 모든 문제를 푼 퀴즈) */;
  averageScore: number /** 평균 점수 */;
  totalCorrectAnswers: number /** 맞힌 문제 총 개수 */;
  totalQuestions: number /** 총 문제 개수 */;
  correctRate: number /** 정답률 (0~100%) */;
  totalTimeTaken: number /** 퀴즈를 푸는 데 걸린 총 시간 (초 단위) */;
  bestScore: number /** 최고 점수 */;
  worstScore: number /** 최저 점수 */;
}

/**
 * @api /api/users/me/recent-activities
 * @api /api/users/{userId}/recent-activities
 * @response RecentActivity (사용자의 최근 활동 정보)
 */
export interface RecentActivity {
  id: number /** 활동 ID */;
  type: "QUIZ_ATTEMPT" | "ACHIEVEMENT_EARNED" | "LEVEL_UP" /** 활동 유형 */;
  quizId?: number /** 퀴즈 ID (QUIZ_ATTEMPT일 경우) */;
  quizTitle?: string /** 퀴즈 제목 (QUIZ_ATTEMPT일 경우) */;
  score?: number /** 획득 점수 (QUIZ_ATTEMPT일 경우) */;
  achievementId?: number /** 업적 ID (ACHIEVEMENT_EARNED일 경우) */;
  achievementName?: string /** 업적 이름 (ACHIEVEMENT_EARNED일 경우) */;
  newLevel?: number /** 레벨 업 후 새로운 레벨 (LEVEL_UP일 경우) */;
  timestamp: string /** 활동 발생 시간 (ISO 8601 형식) */;
}

/**
 * @api /api/users/me/achievements
 * @api /api/users/{userId}/achievements
 * @response Achievement (사용자가 획득할 수 있는 업적 정보)
 */
export interface Achievement {
  id: number /** 업적 ID */;
  name: string /** 업적 이름 */;
  description: string /** 업적 설명 */;
  iconUrl: string /** 업적 아이콘 URL */;
  earnedAt: string | null /** 업적 달성 날짜 (미획득 시 null) */;
  progress: number /** 달성 진행도 (0~100%) */;
  requirementDescription: string /** 업적 획득 조건 설명 */;
}

export type ActivityType = "QUIZ_ATTEMPT" | "ACHIEVEMENT_EARNED" | "LEVEL_UP";

export interface ActivityResponse {
  id: number;
  type: ActivityType;
  quizTitle?: string | null;
  score?: number | null;
  achievementName?: string | null;
  newLevel?: number | null;
  timestamp?: string | null;
}
