package com.quizplatform.quiz.domain.service;

import com.quizplatform.quiz.domain.model.Question;
import com.quizplatform.quiz.domain.model.QuestionAttempt;
import com.quizplatform.quiz.domain.model.Quiz;
import com.quizplatform.quiz.domain.model.QuizAttempt;
import com.quizplatform.quiz.domain.repository.QuizAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 퀴즈 결과 처리 서비스 구현
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuizResultProcessorImpl implements QuizResultProcessor {

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizEventPublisher eventPublisher;

    @Override
    @Transactional
    public QuizAttempt processQuizCompletion(QuizAttempt attempt) {
        log.info("Processing quiz completion for attempt ID: {}", attempt.getId());
        
        // 퀴즈 완료 처리
        boolean passed = attempt.complete();
        
        // 퀴즈 통계 업데이트
        updateQuizStatistics(attempt);
        
        // 완료 이벤트 발행
        publishQuizCompletionEvent(attempt);
        
        // 업적 확인 및 이벤트 발행
        checkAndPublishAchievements(attempt);
        
        return quizAttemptRepository.save(attempt);
    }

    @Override
    @Transactional
    public void publishQuizCompletionEvent(QuizAttempt attempt) {
        log.info("Publishing quiz completion event for attempt ID: {}", attempt.getId());
        
        // 획득 경험치와 포인트 계산
        int experienceGained = calculateExperienceGained(attempt);
        int pointsGained = calculatePointsGained(attempt);
        
        // 이벤트 발행
        eventPublisher.publishQuizCompleted(
                attempt.getQuiz().getId().toString(),
                attempt.getUserId().toString(),
                attempt.getScore(),
                attempt.getTotalQuestions(),
                experienceGained,
                pointsGained,
                attempt.isPassed()
        );
        
        log.info("Quiz completion event published for quiz: {}, user: {}, score: {}/{}",
                attempt.getQuiz().getId(), attempt.getUserId(), 
                attempt.getScore(), attempt.getTotalQuestions());
    }

    @Override
    @Transactional
    public void checkAndPublishAchievements(QuizAttempt attempt) {
        log.info("Checking achievements for user: {}", attempt.getUserId());
        
        // 업적 달성 여부 체크
        Map<String, Boolean> achievements = new HashMap<>();
        
        // 첫 퀴즈 완료 업적
        if (isFirstQuizCompleted(attempt.getUserId())) {
            achievements.put("FIRST_QUIZ_COMPLETED", true);
            log.info("User {} achieved: FIRST_QUIZ_COMPLETED", attempt.getUserId());
        }
        
        // 높은 점수 업적 (90점 이상)
        if (attempt.isPassed() && attempt.getScore() >= 90) {
            achievements.put("HIGH_SCORE", true);
            log.info("User {} achieved: HIGH_SCORE with score: {}", 
                    attempt.getUserId(), attempt.getScore());
        }
        
        // 완벽 퀴즈 업적 (100점)
        if (attempt.getScore() == 100) {
            achievements.put("PERFECT_SCORE", true);
            log.info("User {} achieved: PERFECT_SCORE", attempt.getUserId());
        }
        
        // 달성한 업적에 대해 이벤트 발행
        achievements.forEach((type, achieved) -> {
            if (achieved) {
                eventPublisher.publishUserAchievement(
                        attempt.getUserId().toString(),
                        type,
                        getAchievementDescription(type),
                        getAchievementExperience(type),
                        getAchievementPoints(type)
                );
            }
        });
    }

    @Override
    @Transactional
    public void updateQuizStatistics(QuizAttempt attempt) {
        log.info("Updating quiz statistics for quiz ID: {}", attempt.getQuiz().getId());
        
        Quiz quiz = attempt.getQuiz();
        quiz.recordAttempt(attempt.getScore());
        
        log.info("Quiz statistics updated: attempts={}, avg_score={}", 
                quiz.getAttemptCount(), quiz.getAvgScore());
    }

    @Override
    public int calculateExperienceGained(QuizAttempt attempt) {
        // 기본 경험치 = 문제당 10점 * 정답 개수
        int correctAnswers = (int) attempt.getQuestionAttempts().stream()
                .filter(QuestionAttempt::isCorrect)
                .count();
        
        int baseExp = correctAnswers * 10;
        
        // 난이도 보너스
        int difficultyBonus = attempt.getQuiz().getDifficulty() * 5;
        
        // 완료 보너스 (통과 시 추가 경험치)
        int completionBonus = attempt.isPassed() ? 50 : 0;
        
        // 속도 보너스 (시간 제한이 있고 빨리 완료한 경우)
        int speedBonus = 0;
        if (attempt.getQuiz().getTimeLimit() != null && attempt.getTimeTaken() != null) {
            int timeLimit = attempt.getQuiz().getTimeLimit() * 60; // 분 -> 초
            if (attempt.getTimeTaken() < timeLimit / 2) {
                speedBonus = 30; // 제한 시간의 절반 이내에 완료하면 보너스
            }
        }
        
        int totalExp = baseExp + difficultyBonus + completionBonus + speedBonus;
        log.debug("Experience calculation: base={}, difficulty={}, completion={}, speed={}, total={}",
                baseExp, difficultyBonus, completionBonus, speedBonus, totalExp);
        
        return totalExp;
    }

    @Override
    public int calculatePointsGained(QuizAttempt attempt) {
        // 기본 점수 = 경험치의 2배
        int basePoints = calculateExperienceGained(attempt) * 2;
        
        // 정확도 보너스
        double accuracy = (double) attempt.getScore() / 100;
        int accuracyBonus = (int) (accuracy * 100);
        
        // 난이도 레벨에 따른 추가 보너스
        int difficultyLevelBonus = 0;
        if (attempt.getQuiz().getDifficultyLevel() != null) {
            switch (attempt.getQuiz().getDifficultyLevel()) {
                case BEGINNER:
                    difficultyLevelBonus = 20;
                    break;
                case INTERMEDIATE:
                    difficultyLevelBonus = 50;
                    break;
                case ADVANCED:
                    difficultyLevelBonus = 100;
                    break;
                case EXPERT:
                    difficultyLevelBonus = 200;
                    break;
            }
        }
        
        int totalPoints = basePoints + accuracyBonus + difficultyLevelBonus;
        log.debug("Points calculation: base={}, accuracy={}, difficulty={}, total={}",
                basePoints, accuracyBonus, difficultyLevelBonus, totalPoints);
        
        return totalPoints;
    }
    
    /**
     * 첫 퀴즈 완료 여부 확인
     */
    private boolean isFirstQuizCompleted(Long userId) {
        long count = quizAttemptRepository.countByUserIdAndCompleted(userId, true);
        return count == 1;
    }
    
    /**
     * 업적 설명 반환
     */
    private String getAchievementDescription(String type) {
        return switch (type) {
            case "FIRST_QUIZ_COMPLETED" -> "첫 번째 퀴즈를 완료했습니다!";
            case "HIGH_SCORE" -> "90점 이상의 고득점을 달성했습니다!";
            case "PERFECT_SCORE" -> "100점 만점을 획득했습니다!";
            default -> "업적을 달성했습니다!";
        };
    }
    
    /**
     * 업적 보너스 경험치 반환
     */
    private int getAchievementExperience(String type) {
        return switch (type) {
            case "FIRST_QUIZ_COMPLETED" -> 50;
            case "HIGH_SCORE" -> 100;
            case "PERFECT_SCORE" -> 200;
            default -> 30;
        };
    }
    
    /**
     * 업적 보너스 포인트 반환
     */
    private int getAchievementPoints(String type) {
        return switch (type) {
            case "FIRST_QUIZ_COMPLETED" -> 100;
            case "HIGH_SCORE" -> 200;
            case "PERFECT_SCORE" -> 500;
            default -> 50;
        };
    }
} 