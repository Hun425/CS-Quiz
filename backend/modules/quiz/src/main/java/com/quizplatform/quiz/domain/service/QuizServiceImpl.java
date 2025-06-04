package com.quizplatform.quiz.domain.service;

import com.quizplatform.common.exception.BusinessException;
import com.quizplatform.common.exception.ErrorCode;
import com.quizplatform.quiz.domain.model.Quiz;
import com.quizplatform.quiz.domain.model.QuestionAttempt;
import com.quizplatform.quiz.domain.model.QuizAttempt;
import com.quizplatform.quiz.adapter.out.persistence.repository.QuizAttemptRepository;
import com.quizplatform.quiz.adapter.out.persistence.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 퀴즈 서비스 구현체
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizEventPublisher eventPublisher;
    
    // 사용자 정보 캐시 (실제 구현에서는 Redis 등 분산 캐시 사용 권장)
    private final Map<String, UserInfo> userCache = new ConcurrentHashMap<>();
    
    // 사용자 퀴즈 추천 설정 캐시
    private final Map<String, QuizRecommendationSettings> userRecommendations = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public Quiz createQuiz(Quiz quiz) {
        return quizRepository.save(quiz);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Quiz> findById(Long id) {
        return quizRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Quiz> findByCategory(String category) {
        return quizRepository.findByCategory(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Quiz> findByDifficulty(int difficulty) {
        return quizRepository.findByDifficulty(difficulty);
    }

    @Override
    @Transactional
    public QuizAttempt startQuizAttempt(Long quizId, Long userId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));
        
        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .userId(userId)
                .build();
        
        return quizAttemptRepository.save(attempt);
    }

    @Override
    @Transactional
    public QuizAttempt submitQuizAttempt(Long attemptId) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        
        // 퀴즈 시도 완료 처리
        boolean passed = attempt.complete();
        
        // 퀴즈 완료 이벤트 발행
        int experienceGained = calculateExperience(attempt);
        int pointsGained = calculatePoints(attempt);
        
        eventPublisher.publishQuizCompleted(
                attempt.getQuiz().getId().toString(),
                attempt.getUserId().toString(),
                attempt.getScore(),
                attempt.getTotalQuestions(),
                experienceGained,
                pointsGained,
                passed
        );
        
        // 업적 달성 여부 체크
        checkAndPublishAchievements(attempt);
        
        return quizAttemptRepository.save(attempt);
    }

    @Override
    public void handleNewUser(String userId, String username, String email) {
        // 사용자 정보 캐싱
        userCache.put(userId, new UserInfo(userId, username, email));
        
        // 초기 추천 설정 생성
        userRecommendations.put(userId, new QuizRecommendationSettings(1, 3));
        
        log.info("New user cached in Quiz module: {}", userId);
    }

    @Override
    public void adjustQuizRecommendationByLevel(String userId, int level) {
        // 레벨에 따른 퀴즈 추천 난이도 조정
        int recommendedDifficulty = Math.min(5, (level + 2) / 3);
        
        QuizRecommendationSettings settings = userRecommendations.getOrDefault(
                userId, new QuizRecommendationSettings(1, 3));
        
        settings.setDifficulty(recommendedDifficulty);
        userRecommendations.put(userId, settings);
        
        log.info("Adjusted quiz recommendation difficulty to {} for user {}", 
                recommendedDifficulty, userId);
    }
    
    /**
     * 획득 경험치 계산
     */
    private int calculateExperience(QuizAttempt attempt) {
        // 기본 경험치 = 문제당 10점 * 정답 개수
        int correctAnswers = (int) attempt.getQuestionAttempts().stream()
                .filter(QuestionAttempt::isCorrect)
                .count();
        
        int baseExp = correctAnswers * 10;
        
        // 난이도 보너스
        int difficultyBonus = attempt.getQuiz().getDifficulty() * 5;
        
        // 완료 보너스 (통과 시 추가 경험치)
        int completionBonus = attempt.isPassed() ? 50 : 0;
        
        return baseExp + difficultyBonus + completionBonus;
    }
    
    /**
     * 획득 포인트 계산
     */
    private int calculatePoints(QuizAttempt attempt) {
        // 기본 점수 = 경험치의 2배
        int basePoints = calculateExperience(attempt) * 2;
        
        // 정확도 보너스
        double accuracy = (double) attempt.getScore() / attempt.getTotalQuestions();
        int accuracyBonus = (int) (accuracy * 100);
        
        return basePoints + accuracyBonus;
    }
    
    /**
     * 업적 달성 체크 및 이벤트 발행
     */
    private void checkAndPublishAchievements(QuizAttempt attempt) {
        // 첫 퀴즈 완료 업적
        Map<String, Boolean> achievements = new HashMap<>();
        achievements.put("FIRST_QUIZ_COMPLETED", isFirstQuizCompleted(attempt.getUserId()));
        
        // 높은 점수 업적
        if (attempt.isPassed() && attempt.getScore() >= 90) {
            achievements.put("HIGH_SCORE", true);
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
            default -> 50;
        };
    }
    
    /**
     * 사용자 정보 내부 클래스
     */
    @lombok.Data
    private static class UserInfo {
        private final String userId;
        private final String username;
        private final String email;
        
        public UserInfo(String userId, String username, String email) {
            this.userId = userId;
            this.username = username;
            this.email = email;
        }
    }
    
    /**
     * 퀴즈 추천 설정 내부 클래스
     */
    @lombok.Data
    private static class QuizRecommendationSettings {
        private int difficulty;
        private int count;
        
        public QuizRecommendationSettings(int difficulty, int count) {
            this.difficulty = difficulty;
            this.count = count;
        }
    }
} 