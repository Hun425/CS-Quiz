package application.service;

import application.port.in.GetQuizUseCase;
import application.port.in.SearchQuizUseCase;
import application.port.in.command.SearchQuizCommand;
import application.port.out.LoadQuizAttemptPort;
import application.port.out.LoadQuizPort;
import domain.model.DifficultyLevel;
import domain.model.Quiz;
import domain.model.QuizType;
import domain.model.QuizAttempt;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 퀴즈 조회 및 검색 관련 비즈니스 로직을 처리하는 서비스 클래스
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizQueryService implements GetQuizUseCase, SearchQuizUseCase {
    private final LoadQuizPort loadQuizPort;
    private final LoadQuizAttemptPort loadQuizAttemptPort;

    @Override
    public Quiz getQuiz(Long quizId) {
        return loadQuizPort.findById(quizId)
                .orElseThrow(() -> new NoSuchElementException("퀴즈를 찾을 수 없습니다: " + quizId));
    }
    
    @Override
    public Page<Quiz> getQuizzes(Pageable pageable) {
        return loadQuizPort.findByIsPublic(true, pageable);
    }
    
    @Override
    public Quiz getDailyQuiz() {
        return loadQuizPort.findCurrentDailyQuiz(LocalDateTime.now().minusDays(1))
                .orElseThrow(() -> new NoSuchElementException("현재 유효한 데일리 퀴즈가 없습니다."));
    }
    
    @Override
    public Page<Quiz> getQuizzesByTag(Long tagId, Pageable pageable) {
        return loadQuizPort.findByTagId(tagId, pageable);
    }
    
    @Override
    public Page<Quiz> getQuizzesByUser(Long creatorId, Pageable pageable) {
        return loadQuizPort.findByCreatorId(creatorId, pageable);
    }

    // 추가적인 구현 메소드
    public Optional<Quiz> getQuizById(Long quizId) {
        return loadQuizPort.findById(quizId);
    }

    public Optional<Quiz> getQuizWithQuestions(Long quizId) {
        return loadQuizPort.findByIdWithQuestions(quizId);
    }

    public Optional<Quiz> getCurrentDailyQuiz() {
        return loadQuizPort.findCurrentDailyQuiz(LocalDateTime.now().minusDays(1));
    }
    
    public Page<Quiz> getQuizzesByCreator(Long creatorId, Pageable pageable) {
        return loadQuizPort.findByCreatorId(creatorId, pageable);
    }

    @Override
    public Page<Quiz> searchQuizzes(SearchQuizCommand command, Pageable pageable) {
        Set<DifficultyLevel> difficultyLevels = command.getDifficultyLevels() != null ? 
                command.getDifficultyLevels() : Set.of();
        
        Set<QuizType> quizTypes = command.getQuizTypes() != null ? 
                command.getQuizTypes() : Set.of();
        
        return loadQuizPort.search(
                command.getKeyword(),
                command.getTagIds(),
                difficultyLevels,
                quizTypes,
                command.getCreatorId(),
                command.isPublicOnly(),
                pageable
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
        DifficultyLevel recommendedDifficulty = analyzeRecommendedDifficulty(recentAttempts);
        
        // 추천 퀴즈 조회
        return loadQuizPort.findRecommendedQuizzes(preferredTagIds, recommendedDifficulty, Pageable.ofSize(limit));
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
    private DifficultyLevel analyzeRecommendedDifficulty(List<QuizAttempt> recentAttempts) {
        // 평균 점수를 기준으로 적절한 난이도 추천
        double avgScore = recentAttempts.stream()
                .filter(attempt -> attempt.getScore() != null)
                .mapToInt(QuizAttempt::getScore)
                .average()
                .orElse(0);
        
        if (avgScore >= 85) {
            return DifficultyLevel.ADVANCED;
        } else if (avgScore >= 65) {
            return DifficultyLevel.INTERMEDIATE;
        } else {
            return DifficultyLevel.BEGINNER;
        }
    }
}