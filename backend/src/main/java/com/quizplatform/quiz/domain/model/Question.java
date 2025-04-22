package com.quizplatform.quiz.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 퀴즈 문제 도메인 모델
 */
@Getter
public class Question {
    public static final int DEFAULT_TIME_LIMIT_SECONDS = 60;
    public static final int TIME_LIMIT_SECONDS_MULTIPLE_CHOICE = 60;
    public static final int TIME_LIMIT_SECONDS_TRUE_FALSE = 30;
    public static final int TIME_LIMIT_SECONDS_SHORT_ANSWER = 120;
    public static final int TIME_LIMIT_SECONDS_CODE_ANALYSIS = 300;
    public static final int TIME_LIMIT_SECONDS_DIAGRAM_BASED = 180;

    private final Long id;
    private final QuestionType questionType;
    private final String questionText;
    private final String codeSnippet;
    private final List<Option> options;
    private final String correctAnswer;
    private final String explanation;
    private final int points;
    private final Integer timeLimitSeconds;
    private final DifficultyLevel difficultyLevel;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    @Builder
    public Question(
            Long id,
            QuestionType questionType,
            String questionText,
            String codeSnippet,
            List<Option> options,
            String correctAnswer,
            String explanation,
            Integer points,
            Integer timeLimitSeconds,
            DifficultyLevel difficultyLevel,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.questionType = questionType != null ? questionType : QuestionType.MULTIPLE_CHOICE;
        this.questionText = questionText;
        this.codeSnippet = codeSnippet;
        this.options = options != null ? options : new ArrayList<>();
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
        this.points = points != null ? points : 1;
        this.timeLimitSeconds = timeLimitSeconds != null ? timeLimitSeconds : getDefaultTimeLimitForType(this.questionType);
        this.difficultyLevel = difficultyLevel != null ? difficultyLevel : DifficultyLevel.BEGINNER;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    /**
     * 사용자 답변의 정답 여부 확인
     */
    public boolean isCorrectAnswer(String answer) {
        if (answer == null) {
            return false;
        }

        switch (questionType) {
            case MULTIPLE_CHOICE:
                return correctAnswer.equals(answer.trim());
            case TRUE_FALSE:
                return correctAnswer.equalsIgnoreCase(answer.trim());
            case SHORT_ANSWER:
                // 주관식의 경우 공백과 대소문자를 무시하고 비교
                return correctAnswer.trim().equalsIgnoreCase(answer.trim());
            case CODE_ANALYSIS:
                // 코드 분석 문제는 정확한 일치 필요
                return correctAnswer.equals(answer);
            case DIAGRAM_BASED:
                // 다이어그램 기반 문제도 정확한 일치 필요
                return correctAnswer.equals(answer);
            default:
                return false;
        }
    }

    /**
     * 문제 유형별 기본 제한 시간 설정
     */
    private int getDefaultTimeLimitForType(QuestionType type) {
        switch (type) {
            case MULTIPLE_CHOICE:
                return TIME_LIMIT_SECONDS_MULTIPLE_CHOICE;
            case TRUE_FALSE:
                return TIME_LIMIT_SECONDS_TRUE_FALSE;
            case SHORT_ANSWER:
                return TIME_LIMIT_SECONDS_SHORT_ANSWER;
            case CODE_ANALYSIS:
                return TIME_LIMIT_SECONDS_CODE_ANALYSIS;
            case DIAGRAM_BASED:
                return TIME_LIMIT_SECONDS_DIAGRAM_BASED;
            default:
                return DEFAULT_TIME_LIMIT_SECONDS;
        }
    }

    /**
     * 남은 시간에 따른 보너스 점수 계산
     */
    public int calculateTimeBonus(int secondsRemaining) {
        if (secondsRemaining <= 0) {
            return 0;
        }

        double timeRatio = (double) secondsRemaining / this.timeLimitSeconds;
        if (timeRatio >= 0.7) return 3;      // 70% 이상 남았을 때 3점
        if (timeRatio >= 0.5) return 2;      // 50% 이상 남았을 때 2점
        if (timeRatio >= 0.3) return 1;      // 30% 이상 남았을 때 1점
        return 0;
    }

    /**
     * 시간 초과 여부 확인
     */
    public boolean isTimeExpired(LocalDateTime startTime) {
        return LocalDateTime.now().isAfter(startTime.plusSeconds(this.timeLimitSeconds));
    }

    /**
     * 문제 복사 (데일리 퀴즈 생성 등에 사용)
     */
    public Question copy() {
        return Question.builder()
                .questionType(this.questionType)
                .questionText(this.questionText)
                .codeSnippet(this.codeSnippet)
                .options(new ArrayList<>(this.options))
                .correctAnswer(this.correctAnswer)
                .explanation(this.explanation)
                .difficultyLevel(this.difficultyLevel)
                .points(this.points)
                .timeLimitSeconds(this.timeLimitSeconds)
                .build();
    }

    /**
     * 문제 옵션을 나타내는 내부 클래스
     */
    @Getter
    public static class Option {
        private final String key;
        private final String value;

        @Builder
        public Option(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}