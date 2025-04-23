package com.quizplatform.quiz.domain.service;

import com.quizplatform.quiz.domain.model.DifficultyLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 퀴즈 추천 서비스 구현
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuizRecommendationServiceImpl implements QuizRecommendationService {
    
    // 사용자별 추천 설정 저장 (실제 구현에서는 Redis 등 분산 캐시 사용 권장)
    private final Map<String, RecommendationSettings> userRecommendations = new ConcurrentHashMap<>();
    
    @Override
    public void initializeRecommendations(String userId) {
        log.info("Initializing quiz recommendations for user: {}", userId);
        
        // 초보자 레벨에 맞는 추천 설정
        RecommendationSettings settings = new RecommendationSettings();
        settings.setDifficulty(1); // 초급 난이도
        settings.setDailyQuizCount(3); // 하루 3개 추천
        settings.setPreferredCategories(new String[]{"general", "beginner"});
        
        userRecommendations.put(userId, settings);
    }
    
    @Override
    public void adjustRecommendationsByLevel(String userId, int level) {
        log.info("Adjusting quiz recommendations for user: {} with level: {}", userId, level);
        
        // 기존 설정 가져오기 (없으면 새로 생성)
        RecommendationSettings settings = userRecommendations.getOrDefault(
                userId, new RecommendationSettings());
        
        // 레벨에 따른 난이도 조정
        int recommendedDifficulty = calculateDifficultyByLevel(level);
        settings.setDifficulty(recommendedDifficulty);
        
        // 레벨에 따른 추천 카테고리 조정
        String[] categories = determineCategories(level);
        settings.setPreferredCategories(categories);
        
        // 레벨에 따른 추천 개수 조정
        int dailyCount = Math.min(5, 2 + (level / 10));
        settings.setDailyQuizCount(dailyCount);
        
        userRecommendations.put(userId, settings);
        
        log.info("Updated recommendation settings for user {}: difficulty={}, dailyCount={}", 
                userId, recommendedDifficulty, dailyCount);
    }
    
    @Override
    public void adjustRecommendationsByActivity(String userId, String categoryId) {
        log.info("Adjusting quiz recommendations based on activity for user: {}, category: {}", 
                userId, categoryId);
        
        RecommendationSettings settings = userRecommendations.get(userId);
        if (settings == null) {
            log.warn("No recommendation settings found for user: {}", userId);
            return;
        }
        
        // 카테고리 관심도 증가 (실제 구현에서는 더 복잡한 로직 적용)
        String[] categories = settings.getPreferredCategories();
        if (categories != null && categories.length > 0 && !containsCategory(categories, categoryId)) {
            // 새 카테고리 배열 생성
            String[] newCategories = new String[categories.length + 1];
            System.arraycopy(categories, 0, newCategories, 0, categories.length);
            newCategories[categories.length] = categoryId;
            
            settings.setPreferredCategories(newCategories);
            userRecommendations.put(userId, settings);
            
            log.info("Added new category {} to user {}'s preferences", categoryId, userId);
        }
    }
    
    @Override
    public boolean removeRecommendationSettings(String userId) {
        RecommendationSettings removed = userRecommendations.remove(userId);
        if (removed != null) {
            log.info("Removed recommendation settings for user: {}", userId);
            return true;
        } else {
            log.warn("No recommendation settings found for user: {} during removal", userId);
            return false;
        }
    }
    
    /**
     * 사용자 레벨에 따른 퀴즈 난이도 계산
     * 
     * @param level 사용자 레벨
     * @return 추천 난이도 (1-5)
     */
    private int calculateDifficultyByLevel(int level) {
        if (level < 5) {
            return 1; // BEGINNER
        } else if (level < 15) {
            return 2; // INTERMEDIATE
        } else if (level < 30) {
            return 3; // ADVANCED
        } else {
            return 4; // EXPERT
        }
    }
    
    /**
     * 레벨에 따른 추천 카테고리 결정
     * 
     * @param level 사용자 레벨
     * @return 추천 카테고리 배열
     */
    private String[] determineCategories(int level) {
        if (level < 5) {
            return new String[]{"beginner", "general"};
        } else if (level < 15) {
            return new String[]{"intermediate", "general", "popular"};
        } else if (level < 30) {
            return new String[]{"advanced", "specialized", "popular"};
        } else {
            return new String[]{"expert", "specialized", "challenge"};
        }
    }
    
    /**
     * 카테고리 배열에 특정 카테고리 포함 여부 확인
     * 
     * @param categories 카테고리 배열
     * @param categoryId 확인할 카테고리 ID
     * @return 포함 여부
     */
    private boolean containsCategory(String[] categories, String categoryId) {
        for (String category : categories) {
            if (category.equals(categoryId)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 추천 설정 내부 클래스
     */
    private static class RecommendationSettings {
        private int difficulty = 1;
        private int dailyQuizCount = 3;
        private String[] preferredCategories = new String[0];
        
        public int getDifficulty() {
            return difficulty;
        }
        
        public void setDifficulty(int difficulty) {
            this.difficulty = difficulty;
        }
        
        public int getDailyQuizCount() {
            return dailyQuizCount;
        }
        
        public void setDailyQuizCount(int dailyQuizCount) {
            this.dailyQuizCount = dailyQuizCount;
        }
        
        public String[] getPreferredCategories() {
            return preferredCategories;
        }
        
        public void setPreferredCategories(String[] preferredCategories) {
            this.preferredCategories = preferredCategories;
        }
    }
} 