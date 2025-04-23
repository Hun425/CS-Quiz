package com.quizplatform.common.event;

/**
 * 카프카 토픽 상수 정의
 * 모든 모듈에서 공유되는 토픽 이름을 관리
 */
public class Topics {
    // Battle 모듈 토픽
    public static final String BATTLE_CREATED = "battle-created";
    public static final String BATTLE_STARTED = "battle-started";
    public static final String BATTLE_COMPLETED = "battle-completed";
    public static final String BATTLE_CANCELLED = "battle-cancelled";
    
    // Quiz 모듈 토픽
    public static final String QUIZ_CREATED = "quiz-created";
    public static final String QUIZ_UPDATED = "quiz-updated";
    public static final String QUIZ_COMPLETED = "quiz-completed";
    public static final String HIGH_SCORE_ACHIEVED = "high-score-achieved";
    public static final String DAILY_QUIZ_CREATED = "daily-quiz-created";
    
    // 모듈 간 통신 토픽
    public static final String QUIZ_ANSWER_VALIDATION_REQUEST = "quiz-answer-validation-request";
    public static final String QUIZ_ANSWER_VALIDATION_RESULT = "quiz-answer-validation-result";
    public static final String QUIZ_SCORE_CALCULATION_REQUEST = "quiz-score-calculation-request";
    public static final String QUIZ_SCORE_CALCULATION_RESULT = "quiz-score-calculation-result";
    
    // User 모듈 토픽
    public static final String USER_REGISTERED = "user-registered";
    public static final String USER_LEVEL_UP = "user-level-up";
    public static final String USER_ACHIEVEMENT_EARNED = "user-achievement-earned";
}
