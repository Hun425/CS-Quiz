package com.quizplatform.modules.quiz.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 퀴즈 통계 정보 클래스
 * 
 * <p>퀴즈에 대한 각종 통계 정보를 계산하고 관리합니다.
 * 총 시도 횟수, 평균 점수, 완료율, 난이도 분포, 개별 문제 통계 등을 포함합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Getter
@Builder
public class QuizStatistics {
    /**
     * 총 시도 횟수
     */
    private int totalAttempts;
    
    /**
     * 평균 점수
     */
    private double averageScore;
    
    /**
     * 퀴즈 완료율
     */
    private double completionRate;
    
    /**
     * 평균 풀이 시간 (초 단위)
     */
    private int averageTimeSeconds;
    
    /**
     * 난이도별 문제 분포
     */
    private Map<DifficultyLevel, Integer> difficultyDistribution;
    
    /**
     * 개별 문제 통계 목록
     */
    private List<QuestionStatistic> questionStatistics;

    /**
     * 퀴즈 객체로부터 통계 정보 생성
     * 
     * @param quiz 통계를 생성할 퀴즈
     * @return 생성된 퀴즈 통계 객체
     */
    public static QuizStatistics from(Quiz quiz) {
        return QuizStatistics.builder()
                .totalAttempts(quiz.getAttemptCount())
                .averageScore(quiz.getAvgScore())
                .completionRate(calculateCompletionRate(quiz))
                .averageTimeSeconds(calculateAverageTime(quiz))
                .difficultyDistribution(calculateDifficultyDistribution(quiz))
                .questionStatistics(createQuestionStatistics(quiz))
                .build();
    }

    /**
     * 퀴즈 완료율 계산
     * 
     * @param quiz 계산 대상 퀴즈
     * @return 완료율 (0.0 ~ 1.0)
     */
    private static double calculateCompletionRate(Quiz quiz) {
        // 구현 필요
        return 0.0;
    }

    /**
     * 평균 풀이 시간 계산
     * 
     * @param quiz 계산 대상 퀴즈
     * @return 평균 풀이 시간 (초 단위)
     */
    private static int calculateAverageTime(Quiz quiz) {
        // 구현 필요
        return 0;
    }

    /**
     * 난이도별 문제 분포 계산
     * 
     * @param quiz 계산 대상 퀴즈
     * @return 난이도별 문제 수 맵
     */
    private static Map<DifficultyLevel, Integer> calculateDifficultyDistribution(Quiz quiz) {
        // 구현 필요
        return new HashMap<>();
    }

    /**
     * 개별 문제 통계 생성
     * 
     * @param quiz 통계를 생성할 퀴즈
     * @return 문제별 통계 목록
     */
    private static List<QuestionStatistic> createQuestionStatistics(Quiz quiz) {
        // 구현 필요
        return new ArrayList<>();
    }

    /**
     * 개별 문제 통계 정보 클래스
     */
    @Getter
    @Builder
    public static class QuestionStatistic {
        /**
         * 문제 ID
         */
        private Long questionId;
        
        /**
         * 정답 개수
         */
        private int correctAnswers;
        
        /**
         * 총 시도 횟수
         */
        private int totalAttempts;
        
        /**
         * 정답률
         */
        private double correctRate;
        
        /**
         * 평균 풀이 시간 (초 단위)
         */
        private int averageTimeSeconds;
    }
}