package com.quizplatform.quiz.application.service;

import com.quizplatform.quiz.application.port.in.GetQuizUseCase;
import com.quizplatform.quiz.application.port.in.SearchQuizUseCase;
import com.quizplatform.quiz.application.port.in.command.SearchQuizCommand;
import com.quizplatform.quiz.application.port.out.LoadQuizAttemptPort;
import com.quizplatform.quiz.application.port.out.LoadQuizPort;
import com.quizplatform.quiz.domain.model.DifficultyLevel;
import com.quizplatform.quiz.domain.model.Quiz;
import com.quizplatform.quiz.domain.model.QuizType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 퀴즈 조회 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizQueryService implements GetQuizUseCase, SearchQuizUseCase {
    private final LoadQuizPort loadQuizPort;
    private final LoadQuizAttemptPort loadQuizAttemptPort;

    @Override
    public Optional<Quiz> getQuizById(Long quizId) {
        return loadQuizPort.findById(quizId);
    }

    @Override
    public Optional<Quiz> getQuizWithQuestions(Long quizId) {
        return loadQuizPort.findByIdWithQuestions(quizId);
    }

    @Override
    public Optional<Quiz> getCurrentDailyQuiz() {
        return loadQuizPort.findCurrentDailyQuiz(LocalDateTime.now());
    }

    @Override
    public List<Quiz> getQuizzesByTag(Long tagId, int limit, int offset) {
        return loadQuizPort.findByTagId(tagId, limit, offset);
    }

    @Override
    public List<Quiz> getQuizzesByCreator(Long creatorId, int limit, int offset) {
        return loadQuizPort.findByCreatorId(creatorId, limit, offset);
    }

    @Override
    public List<Quiz> searchQuizzes(SearchQuizCommand command) {
        Set<String> difficultyLevelStrings = command.getDifficultyLevels().stream()
                .map(DifficultyLevel::name)
                .collect(Collectors.toSet());
        
        Set<String> quizTypeStrings = command.getQuizTypes().stream()
                .map(QuizType::name)
                .collect(Collectors.toSet());
        
        return loadQuizPort.search(
                command.getKeyword(),
                command.getTagIds(),
                difficultyLevelStrings,
                quizTypeStrings,
                command.getCreatorId(),
                command.isPublicOnly(),
                command.getLimit(),
                command.getOffset()
        );
    }

    @Override
    public List<Quiz> getRecommendedQuizzes(Long userId, int limit) {
        // 최근 30일 내 사용자의 퀴즈 시도를 분석
        List<QuizAttempt> recentAttempts = loadQuizAttemptPort.findRecentAttempts(
                userId,
                LocalDateTime.now().minusDays(30),
                20 // 최근 시도 20개만 분석
        );
        
        // 사용자가 성과가 좋았던 태그와 적절한 난이도 분석
        Set<Long> preferredTagIds = analyzePreferredTags(recentAttempts);
        String recommendedDifficulty = analyzeRecommendedDifficulty(recentAttempts);
        
        // 추천 퀴즈 조회
        return loadQuizPort.findRecommendedQuizzes(preferredTagIds, recommendedDifficulty, limit);
    }
    
    /**
     * 사용자가 선호하는 태그를 분석합니다.
     */
    private Set<Long> analyzePreferredTags(List<QuizAttempt> recentAttempts) {
        // 실제 구현에서는 시도 내역에서 퀴즈 정보를 통해 태그를 추출하고
        // 높은 점수를 받은 태그들을 선별하는 로직이 필요합니다.
        
        // 간단한 예시 구현으로 퀴즈 ID별로 태그 정보를 조회하여 반환
        return recentAttempts.stream()
                .filter(attempt -> attempt.getScore() != null && attempt.getScore() >= 70) // 70점 이상 퀴즈만 선택
                .map(QuizAttempt::getQuizId)
                .distinct()
                .map(this::getTagsByQuizId)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }
    
    /**
     * 퀴즈 ID로 태그 ID 목록을 조회합니다.
     */
    private Set<Long> getTagsByQuizId(Long quizId) {
        return loadQuizPort.findById(quizId)
                .map(Quiz::getTagIds)
                .orElse(Set.of());
    }
    
    /**
     * 사용자에게 적합한 난이도를 분석합니다.
     */
    private String analyzeRecommendedDifficulty(List<QuizAttempt> recentAttempts) {
        // 평균 점수를 기준으로 적절한 난이도 추천
        double avgScore = recentAttempts.stream()
                .filter(attempt -> attempt.getScore() != null)
                .mapToInt(QuizAttempt::getScore)
                .average()
                .orElse(0);
        
        if (avgScore >= 85) {
            return DifficultyLevel.ADVANCED.name();
        } else if (avgScore >= 65) {
            return DifficultyLevel.INTERMEDIATE.name();
        } else {
            return DifficultyLevel.BEGINNER.name();
        }
    }
}