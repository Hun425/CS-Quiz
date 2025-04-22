package com.quizplatform.quiz.application.service;

import com.quizplatform.quiz.application.port.in.GetQuizStatisticsUseCase;
import com.quizplatform.quiz.application.port.out.LoadQuestionAttemptPort;
import com.quizplatform.quiz.application.port.out.LoadQuizAttemptPort;
import com.quizplatform.quiz.application.port.out.LoadQuestionPort;
import com.quizplatform.quiz.application.port.out.LoadQuizPort;
import com.quizplatform.quiz.domain.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 퀴즈 통계 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizStatisticsService implements GetQuizStatisticsUseCase {
    private final LoadQuizPort loadQuizPort;
    private final LoadQuestionPort loadQuestionPort;
    private final LoadQuizAttemptPort loadQuizAttemptPort;
    private final LoadQuestionAttemptPort loadQuestionAttemptPort;

    @Override
    public Optional<QuizStatistics> getQuizStatistics(Long quizId) {
        // 퀴즈 조회
        Optional<Quiz> quizOptional = loadQuizPort.findByIdWithQuestions(quizId);
        if (quizOptional.isEmpty()) {
            return Optional.empty();
        }
        
        Quiz quiz = quizOptional.get();
        
        // 퀴즈 시도 조회
        List<QuizAttempt> attempts = loadQuizAttemptPort.findByQuizId(quiz.getId());
        
        // 통계 계산
        return Optional.of(calculateStatistics(quiz, attempts));
    }

    @Override
    public Optional<QuizStatistics> getUserQuizStatistics(Long userId, Long quizId) {
        // 퀴즈 조회
        Optional<Quiz> quizOptional = loadQuizPort.findByIdWithQuestions(quizId);
        if (quizOptional.isEmpty()) {
            return Optional.empty();
        }
        
        Quiz quiz = quizOptional.get();
        
        // 사용자의 퀴즈 시도 조회
        List<QuizAttempt> attempts = loadQuizAttemptPort.findByUserIdAndQuizId(userId, quizId);
        
        // 통계 계산
        return Optional.of(calculateStatistics(quiz, attempts));
    }

    /**
     * 퀴즈 통계를 계산합니다.
     */
    private QuizStatistics calculateStatistics(Quiz quiz, List<QuizAttempt> attempts) {
        // 각 문제별 통계 초기화
        Map<Long, QuestionStatisticData> questionStatsMap = initializeQuestionStats(quiz);
        
        // 모든 문제 시도 정보 수집
        for (QuizAttempt attempt : attempts) {
            List<QuestionAttempt> questionAttempts = loadQuestionAttemptPort.findByQuizAttemptId(attempt.getId());
            
            // 문제별 통계 업데이트
            for (QuestionAttempt qa : questionAttempts) {
                QuestionStatisticData qStats = questionStatsMap.get(qa.getQuestionId());
                if (qStats != null) {
                    qStats.addAttempt(qa.isCorrect(), qa.getTimeTaken());
                }
            }
        }
        
        // 난이도 분포 계산
        Map<DifficultyLevel, Integer> difficultyDistribution = calculateDifficultyDistribution(quiz);
        
        // 문제별 통계 변환
        List<QuizStatistics.QuestionStatistic> questionStatistics = convertQuestionStats(questionStatsMap);
        
        return QuizStatistics.builder()
                .quizId(quiz.getId())
                .totalAttempts(attempts.size())
                .averageScore(calculateAverageScore(attempts))
                .completionRate(calculateCompletionRate(attempts))
                .averageTimeSeconds(calculateAverageTime(attempts))
                .difficultyDistribution(difficultyDistribution)
                .questionStatistics(questionStatistics)
                .build();
    }

    /**
     * 퀴즈의 문제별 통계 데이터를 초기화합니다.
     */
    private Map<Long, QuestionStatisticData> initializeQuestionStats(Quiz quiz) {
        Map<Long, QuestionStatisticData> stats = new HashMap<>();
        for (Question question : quiz.getQuestions()) {
            stats.put(question.getId(), new QuestionStatisticData(question.getId()));
        }
        return stats;
    }

    /**
     * 난이도 분포를 계산합니다.
     */
    private Map<DifficultyLevel, Integer> calculateDifficultyDistribution(Quiz quiz) {
        return quiz.getQuestions().stream()
                .collect(Collectors.groupingBy(
                        Question::getDifficultyLevel,
                        Collectors.summingInt(q -> 1)
                ));
    }

    /**
     * 평균 점수를 계산합니다.
     */
    private double calculateAverageScore(List<QuizAttempt> attempts) {
        return attempts.stream()
                .filter(QuizAttempt::isCompleted)
                .mapToInt(QuizAttempt::getScore)
                .average()
                .orElse(0.0);
    }

    /**
     * 완료율을 계산합니다.
     */
    private double calculateCompletionRate(List<QuizAttempt> attempts) {
        if (attempts.isEmpty()) {
            return 0.0;
        }
        
        long completedCount = attempts.stream()
                .filter(QuizAttempt::isCompleted)
                .count();
        
        return 100.0 * completedCount / attempts.size();
    }

    /**
     * 평균 소요 시간을 계산합니다.
     */
    private int calculateAverageTime(List<QuizAttempt> attempts) {
        return attempts.stream()
                .filter(QuizAttempt::isCompleted)
                .mapToInt(attempt -> attempt.getTimeTaken() != null ? attempt.getTimeTaken() : 0)
                .filter(time -> time > 0)
                .average()
                .orElse(0.0)
                .intValue();
    }

    /**
     * 문제별 통계 데이터를 변환합니다.
     */
    private List<QuizStatistics.QuestionStatistic> convertQuestionStats(
            Map<Long, QuestionStatisticData> questionStatsMap) {
        return questionStatsMap.values().stream()
                .map(data -> QuizStatistics.QuestionStatistic.builder()
                        .questionId(data.getQuestionId())
                        .correctAnswers(data.getCorrectAnswers())
                        .totalAttempts(data.getTotalAttempts())
                        .correctRate(data.getCorrectRate())
                        .averageTimeSeconds(data.getAverageTimeSeconds())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 문제별 통계 데이터 클래스
     */
    private static class QuestionStatisticData {
        private final Long questionId;
        private int correctAnswers = 0;
        private int totalAttempts = 0;
        private long totalTimeSeconds = 0;

        public QuestionStatisticData(Long questionId) {
            this.questionId = questionId;
        }

        public void addAttempt(boolean isCorrect, Integer timeTaken) {
            totalAttempts++;
            if (isCorrect) {
                correctAnswers++;
            }
            if (timeTaken != null && timeTaken > 0) {
                totalTimeSeconds += timeTaken;
            }
        }

        public Long getQuestionId() {
            return questionId;
        }

        public int getCorrectAnswers() {
            return correctAnswers;
        }

        public int getTotalAttempts() {
            return totalAttempts;
        }

        public double getCorrectRate() {
            return totalAttempts == 0 ? 0.0 : (100.0 * correctAnswers / totalAttempts);
        }

        public int getAverageTimeSeconds() {
            return totalAttempts == 0 ? 0 : (int) (totalTimeSeconds / totalAttempts);
        }
    }
}