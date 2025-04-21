package com.quizplatform.modules.quiz.dto;


import com.quizplatform.modules.quiz.domain.DifficultyLevel;
import com.quizplatform.modules.quiz.domain.QuizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmitRequest {
    // 퀴즈 시도 ID (startQuiz에서 생성된 ID)
    private Long quizAttemptId;

    // 각 문제 ID에 대응하는 답변 맵
    private Map<Long, String> answers;

    // 총 소요 시간 (초)
    private Integer timeTaken;

    @Getter
    @Builder
    public static class QuizSearchCondition {
        // 기본 검색 조건
        private String title;                    // 제목 검색
        private DifficultyLevel difficultyLevel; // 난이도
        private QuizType quizType;              // 퀴즈 유형
        private List<Long> tagIds;              // 태그 ID 목록
        private Integer minQuestions;            // 최소 문제 수
        private Integer maxQuestions;            // 최대 문제 수
        private String orderBy;                  // 정렬 기준

        // 고급 검색 조건
        private Boolean isPublic;               // 공개 여부
        private Double minAverageScore;         // 최소 평균 점수
        private Double maxAverageScore;         // 최대 평균 점수
        private Integer minAttempts;            // 최소 시도 횟수
        private LocalDateTime createdAfter;     // 생성일 시작
        private LocalDateTime createdBefore;    // 생성일 종료
        private Long creatorId;                 // 생성자 ID

        // 정렬 옵션 상수
        public static final String ORDER_BY_CREATED_DATE = "createdAt";
        public static final String ORDER_BY_POPULARITY = "attemptCount";
        public static final String ORDER_BY_AVERAGE_SCORE = "avgScore";
        public static final String ORDER_BY_DIFFICULTY = "difficultyLevel";

        // 검색 조건 유효성 검사
        public void validate() {
            // 태그 목록이 null인 경우 빈 리스트로 초기화
            if (tagIds == null) {
                tagIds = new ArrayList<>();
            }

            // 문제 수 범위 검증
            if (minQuestions != null && maxQuestions != null && minQuestions > maxQuestions) {
                throw new IllegalArgumentException("최소 문제 수는 최대 문제 수보다 클 수 없습니다.");
            }

            // 평균 점수 범위 검증
            if (minAverageScore != null && maxAverageScore != null && minAverageScore > maxAverageScore) {
                throw new IllegalArgumentException("최소 평균 점수는 최대 평균 점수보다 클 수 없습니다.");
            }

            // 생성일 범위 검증
            if (createdAfter != null && createdBefore != null && createdAfter.isAfter(createdBefore)) {
                throw new IllegalArgumentException("시작일은 종료일보다 늦을 수 없습니다.");
            }

            // 정렬 기준 검증
            if (StringUtils.hasText(orderBy) && !isValidOrderBy(orderBy)) {
                throw new IllegalArgumentException("유효하지 않은 정렬 기준입니다.");
            }
        }

        // 정렬 기준 유효성 검사
        private boolean isValidOrderBy(String orderBy) {
            return orderBy.equals(ORDER_BY_CREATED_DATE) ||
                    orderBy.equals(ORDER_BY_POPULARITY) ||
                    orderBy.equals(ORDER_BY_AVERAGE_SCORE) ||
                    orderBy.equals(ORDER_BY_DIFFICULTY);
        }

        // WHERE 절에 들어갈 조건이 있는지 확인
        public boolean hasConditions() {
            return StringUtils.hasText(title) ||
                    difficultyLevel != null ||
                    quizType != null ||
                    !tagIds.isEmpty() ||
                    minQuestions != null ||
                    maxQuestions != null ||
                    isPublic != null ||
                    minAverageScore != null ||
                    maxAverageScore != null ||
                    minAttempts != null ||
                    createdAfter != null ||
                    createdBefore != null ||
                    creatorId != null;
        }

        // 기본 정렬 기준 반환
        public String getEffectiveOrderBy() {
            return StringUtils.hasText(orderBy) ? orderBy : ORDER_BY_CREATED_DATE;
        }

        // 검색 조건 요약 문자열 생성
        @Override
        public String toString() {
            StringBuilder summary = new StringBuilder("검색 조건: ");

            if (StringUtils.hasText(title)) {
                summary.append("제목:'").append(title).append("' ");
            }
            if (difficultyLevel != null) {
                summary.append("난이도:").append(difficultyLevel).append(" ");
            }
            if (quizType != null) {
                summary.append("유형:").append(quizType).append(" ");
            }
            if (!tagIds.isEmpty()) {
                summary.append("태그 수:").append(tagIds.size()).append(" ");
            }
            if (minQuestions != null || maxQuestions != null) {
                summary.append("문제 수:");
                if (minQuestions != null) summary.append(minQuestions);
                summary.append("~");
                if (maxQuestions != null) summary.append(maxQuestions);
                summary.append(" ");
            }

            return summary.toString().trim();
        }

        // 빌더 패턴을 사용한 기본값 설정
        public static class QuizSearchConditionBuilder {
            private List<Long> tagIds = new ArrayList<>();
            private String orderBy = ORDER_BY_CREATED_DATE;
            private Boolean isPublic = true;

            // 기존 빌더 메서드들은 그대로 유지...

            // 태그 ID 추가 메서드
            public QuizSearchConditionBuilder addTagId(Long tagId) {
                if (this.tagIds == null) {
                    this.tagIds = new ArrayList<>();
                }
                this.tagIds.add(tagId);
                return this;
            }

            // 날짜 범위 설정 헬퍼 메서드
            public QuizSearchConditionBuilder inLastDays(int days) {
                this.createdAfter = LocalDateTime.now().minusDays(days);
                this.createdBefore = LocalDateTime.now();
                return this;
            }
        }
    }
}