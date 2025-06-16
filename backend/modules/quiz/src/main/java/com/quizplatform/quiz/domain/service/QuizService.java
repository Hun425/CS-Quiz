package com.quizplatform.quiz.domain.service;

import com.quizplatform.quiz.domain.model.Quiz;
import com.quizplatform.quiz.domain.model.QuizAttempt;
import com.quizplatform.quiz.domain.model.Tag;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 퀴즈 서비스 인터페이스
 * 
 * <p>퀴즈 도메인의 핵심 비즈니스 로직을 정의한 인터페이스입니다.
 * 퀴즈 관리, 추천, 시도 등의 기능을 제공합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
public interface QuizService {
    
    /**
     * 퀴즈 생성
     * 
     * @param quiz 생성할 퀴즈 정보
     * @return 생성된 퀴즈
     */
    Quiz createQuiz(Quiz quiz);
    
    /**
     * ID로 퀴즈 조회
     * 
     * @param id 퀴즈 ID
     * @return 퀴즈 Optional
     */
    Optional<Quiz> findById(Long id);
    
    /**
     * 카테고리로 퀴즈 목록 조회
     * 
     * @param category 카테고리
     * @return 퀴즈 목록
     */
    List<Quiz> findByCategory(String category);
    
    /**
     * 난이도로 퀴즈 목록 조회
     * 
     * @param difficulty 난이도
     * @return 퀴즈 목록
     */
    List<Quiz> findByDifficulty(int difficulty);
    
    /**
     * 퀴즈 시도 생성
     * 
     * @param quizId 퀴즈 ID
     * @param userId 사용자 ID
     * @return 생성된 퀴즈 시도
     */
    QuizAttempt startQuizAttempt(Long quizId, Long userId);
    
    /**
     * 퀴즈 시도 제출 및 평가
     * 
     * @param attemptId 퀴즈 시도 ID
     * @return 평가 결과
     */
    QuizAttempt submitQuizAttempt(Long attemptId);
    
    /**
     * 새 사용자 정보 처리
     * 
     * <p>다른 모듈에서 사용자 등록 이벤트를 수신했을 때 호출됩니다.</p>
     * 
     * @param userId 사용자 ID
     * @param username 사용자명
     * @param email 이메일
     */
    void handleNewUser(String userId, String username, String email);
    
    /**
     * 사용자 레벨에 따른 퀴즈 추천 조정
     * 
     * <p>사용자가 레벨업했을 때 호출되어 추천 퀴즈의 난이도를 조정합니다.</p>
     * 
     * @param userId 사용자 ID
     * @param level 현재 레벨
     */
    void adjustQuizRecommendationByLevel(String userId, int level);
    
    // ===== 태그 관련 메서드 =====
    
    /**
     * 퀴즈에 태그 추가
     * 
     * @param quizId 퀴즈 ID
     * @param tagId 태그 ID
     * @param currentUserId 현재 사용자 ID
     * @return 업데이트된 퀴즈
     */
    Quiz addTagToQuiz(Long quizId, Long tagId, Long currentUserId);
    
    /**
     * 퀴즈에서 태그 제거
     * 
     * @param quizId 퀴즈 ID
     * @param tagId 태그 ID
     * @param currentUserId 현재 사용자 ID
     * @return 업데이트된 퀴즈
     */
    Quiz removeTagFromQuiz(Long quizId, Long tagId, Long currentUserId);
    
    /**
     * 퀴즈의 모든 태그를 새로운 태그 목록으로 대체
     * 
     * @param quizId 퀴즈 ID
     * @param tagIds 새로운 태그 ID 목록
     * @param currentUserId 현재 사용자 ID
     * @return 업데이트된 퀴즈
     */
    Quiz setQuizTags(Long quizId, Set<Long> tagIds, Long currentUserId);
    
    /**
     * 특정 태그가 포함된 퀴즈 목록 조회
     * 
     * @param tagId 태그 ID
     * @return 퀴즈 목록
     */
    List<Quiz> findQuizzesByTag(Long tagId);
    
    /**
     * 특정 태그가 포함된 퀴즈 목록 조회 (태그 이름으로)
     * 
     * @param tagName 태그 이름
     * @return 퀴즈 목록
     */
    List<Quiz> findQuizzesByTagName(String tagName);
    
    /**
     * 여러 태그가 모두 포함된 퀴즈 목록 조회 (AND 조건)
     * 
     * @param tagIds 태그 ID 목록
     * @return 퀴즈 목록
     */
    List<Quiz> findQuizzesWithAllTags(Set<Long> tagIds);
    
    /**
     * 여러 태그 중 하나라도 포함된 퀴즈 목록 조회 (OR 조건)
     * 
     * @param tagIds 태그 ID 목록
     * @return 퀴즈 목록
     */
    List<Quiz> findQuizzesWithAnyTags(Set<Long> tagIds);
    
    /**
     * 태그 기반 퀴즈 검색 (고급 필터링)
     * 
     * @param searchCriteria 검색 조건
     * @return 퀴즈 목록
     */
    List<Quiz> searchQuizzesWithTags(QuizTagSearchCriteria searchCriteria);
    
    /**
     * 사용자의 취약 태그 기반 추천 퀴즈 조회
     * 
     * @param userId 사용자 ID
     * @param limit 조회할 퀴즈 수
     * @return 추천 퀴즈 목록
     */
    List<Quiz> getRecommendedQuizzesForWeakTags(Long userId, int limit);
    
    // ===== QuizApplicationService에서 필요한 추가 메서드들 =====
    
    /**
     * 특정 태그에 연결된 퀴즈 페이지 조회
     * 
     * @param tagId 태그 ID
     * @param pageable 페이징 정보
     * @return 퀴즈 페이지
     */
    org.springframework.data.domain.Page<Quiz> getQuizzesByTag(Long tagId, org.springframework.data.domain.Pageable pageable);
    
    /**
     * 여러 태그 조건으로 퀴즈 페이지 조회
     * 
     * @param tagIds 태그 ID 목록
     * @param operator 논리 연산자 ("AND", "OR")
     * @param pageable 페이징 정보
     * @return 퀴즈 페이지
     */
    org.springframework.data.domain.Page<Quiz> getQuizzesByTags(List<Long> tagIds, String operator, org.springframework.data.domain.Pageable pageable);
    
    /**
     * 고급 검색 (키워드 + 태그 조합) 페이지 조회
     * 
     * @param keyword 검색 키워드
     * @param tagIds 태그 ID 목록
     * @param category 카테고리
     * @param difficulty 난이도
     * @param pageable 페이징 정보
     * @return 검색 결과 페이지
     */
    org.springframework.data.domain.Page<Quiz> advancedSearchQuizzes(String keyword, List<Long> tagIds, 
                                                                    String category, Integer difficulty, org.springframework.data.domain.Pageable pageable);
    
    /**
     * 퀴즈의 태그 목록 설정 (List 버전)
     * 
     * @param quizId 퀴즈 ID
     * @param tagIds 새로운 태그 ID 목록
     * @return 업데이트된 퀴즈
     */
    Quiz setQuizTags(Long quizId, List<Long> tagIds);
    
    /**
     * 퀴즈에 태그 추가 (사용자 ID 없는 버전)
     * 
     * @param quizId 퀴즈 ID
     * @param tagId 태그 ID
     * @return 업데이트된 퀴즈
     */
    Quiz addTagToQuiz(Long quizId, Long tagId);
    
    /**
     * 퀴즈에서 태그 제거 (사용자 ID 없는 버전)
     * 
     * @param quizId 퀴즈 ID
     * @param tagId 태그 ID
     * @return 업데이트된 퀴즈
     */
    Quiz removeTagFromQuiz(Long quizId, Long tagId);

    /**
     * 퀴즈 태그 검색 조건을 나타내는 클래스
     */
    record QuizTagSearchCriteria(
        Set<Long> includeTagIds,     // 포함되어야 할 태그들 (AND 조건)
        Set<Long> excludeTagIds,     // 제외되어야 할 태그들
        Set<Long> anyTagIds,         // 하나라도 포함되면 되는 태그들 (OR 조건)
        Integer minTagLevel,         // 태그 최소 레벨
        Integer maxTagLevel,         // 태그 최대 레벨
        String difficulty,           // 퀴즈 난이도 필터
        String category,             // 퀴즈 카테고리 필터
        Boolean publishedOnly,       // 공개된 퀴즈만 조회할지 여부
        String sortBy,               // 정렬 기준 (created, popularity, difficulty 등)
        boolean ascending            // 오름차순 정렬 여부
    ) {
        
        /**
         * 기본값이 적용된 검색 조건 생성
         */
        public QuizTagSearchCriteria withDefaults() {
            return new QuizTagSearchCriteria(
                this.includeTagIds != null ? this.includeTagIds : Set.of(),
                this.excludeTagIds != null ? this.excludeTagIds : Set.of(),
                this.anyTagIds != null ? this.anyTagIds : Set.of(),
                this.minTagLevel,
                this.maxTagLevel,
                this.difficulty,
                this.category,
                this.publishedOnly != null ? this.publishedOnly : true,
                this.sortBy != null ? this.sortBy : "created",
                this.ascending
            );
        }
    }
} 