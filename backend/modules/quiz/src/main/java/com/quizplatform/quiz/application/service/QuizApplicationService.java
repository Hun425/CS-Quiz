package com.quizplatform.quiz.application.service;

import com.quizplatform.quiz.application.dto.QuizCreateRequest;
import com.quizplatform.quiz.application.dto.QuizResponse;
import com.quizplatform.quiz.application.dto.QuizUpdateRequest;
import com.quizplatform.quiz.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 퀴즈 애플리케이션 서비스 인터페이스
 * 
 * <p>퀴즈 관련 비즈니스 로직을 처리하는 애플리케이션 서비스입니다.
 * 컨트롤러와 도메인 서비스 사이의 중간 계층으로 작동합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
public interface QuizApplicationService {
    
    /**
     * 퀴즈 생성
     * 
     * @param request 퀴즈 생성 요청 DTO
     * @return 생성된 퀴즈 정보
     */
    QuizResponse createQuiz(QuizCreateRequest request);
    
    /**
     * 퀴즈 ID로 조회
     * 
     * @param id 퀴즈 ID
     * @return 퀴즈 정보
     */
    QuizResponse getQuizById(Long id);
    
    /**
     * 모든 퀴즈 페이지별 조회
     * 
     * @param pageable 페이지 정보
     * @return 퀴즈 목록 페이지
     */
    Page<QuizResponse> getAllQuizzes(Pageable pageable);
    
    /**
     * 카테고리별 퀴즈 조회
     * 
     * @param category 카테고리
     * @param pageable 페이지 정보
     * @return 카테고리별 퀴즈 목록 페이지
     */
    Page<QuizResponse> getQuizzesByCategory(String category, Pageable pageable);
    
    /**
     * 난이도별 퀴즈 조회
     * 
     * @param difficulty 난이도
     * @param pageable 페이지 정보
     * @return 난이도별 퀴즈 목록 페이지
     */
    Page<QuizResponse> getQuizzesByDifficulty(int difficulty, Pageable pageable);
    
    /**
     * 생성자별 퀴즈 조회
     * 
     * @param creatorId 생성자 ID
     * @param pageable 페이지 정보
     * @return 생성자별 퀴즈 목록 페이지
     */
    Page<QuizResponse> getQuizzesByCreator(Long creatorId, Pageable pageable);
    
    /**
     * 퀴즈 업데이트
     * 
     * @param id 퀴즈 ID
     * @param request 업데이트 요청 DTO
     * @return 업데이트된 퀴즈 정보
     */
    QuizResponse updateQuiz(Long id, QuizUpdateRequest request);
    
    /**
     * 퀴즈 삭제
     * 
     * @param id 퀴즈 ID
     */
    void deleteQuiz(Long id);
    
    /**
     * 퀴즈 공개 상태 설정
     * 
     * @param id 퀴즈 ID
     * @param publish 공개 여부
     * @return 업데이트된 퀴즈 정보
     */
    QuizResponse setQuizPublishStatus(Long id, boolean publish);
    
    /**
     * 퀴즈 활성화 상태 설정
     * 
     * @param id 퀴즈 ID
     * @param active 활성화 여부
     * @return 업데이트된 퀴즈 정보
     */
    QuizResponse setQuizActiveStatus(Long id, boolean active);
    
    /**
     * 퀴즈 검색
     * 
     * @param keyword 검색 키워드
     * @param pageable 페이지 정보
     * @return 검색 결과 페이지
     */
    Page<QuizResponse> searchQuizzes(String keyword, Pageable pageable);
    
    /**
     * 사용자에게 추천되는 퀴즈 목록 조회
     * 
     * @param user 사용자 정보
     * @param limit 조회할 개수
     * @return 추천된 퀴즈 목록
     */
    List<QuizResponse> getRecommendedQuizzes(User user, int limit);
    
    // ===== Tag 기반 퀴즈 조회 =====
    
    /**
     * 특정 태그에 연결된 퀴즈 목록 조회
     * 
     * @param tagId 태그 ID
     * @param pageable 페이지 정보
     * @return 태그별 퀴즈 목록 페이지
     */
    Page<QuizResponse> getQuizzesByTag(Long tagId, Pageable pageable);
    
    /**
     * 여러 태그 조건으로 퀴즈 목록 조회
     * 
     * @param tagIds 태그 ID 목록
     * @param operator 논리 연산자 ("AND", "OR")
     * @param pageable 페이지 정보
     * @return 태그별 퀴즈 목록 페이지
     */
    Page<QuizResponse> getQuizzesByTags(List<Long> tagIds, String operator, Pageable pageable);
    
    /**
     * 고급 검색 (키워드 + 태그 조합)
     * 
     * @param keyword 검색 키워드 (선택적)
     * @param tagIds 태그 ID 목록 (선택적)
     * @param category 카테고리 (선택적)
     * @param difficulty 난이도 (선택적)
     * @param pageable 페이지 정보
     * @return 고급 검색 결과 페이지
     */
    Page<QuizResponse> advancedSearchQuizzes(String keyword, List<Long> tagIds, 
                                           String category, Integer difficulty, Pageable pageable);
    
    // ===== Quiz-Tag 관계 관리 =====
    
    /**
     * 퀴즈의 태그 목록 업데이트
     * 
     * @param quizId 퀴즈 ID
     * @param tagIds 새로운 태그 ID 목록
     * @return 업데이트된 퀴즈 정보
     */
    QuizResponse updateQuizTags(Long quizId, List<Long> tagIds);
    
    /**
     * 퀴즈에 태그 추가
     * 
     * @param quizId 퀴즈 ID
     * @param tagId 추가할 태그 ID
     * @return 업데이트된 퀴즈 정보
     */
    QuizResponse addTagToQuiz(Long quizId, Long tagId);
    
    /**
     * 퀴즈에서 태그 제거
     * 
     * @param quizId 퀴즈 ID
     * @param tagId 제거할 태그 ID
     * @return 업데이트된 퀴즈 정보
     */
    QuizResponse removeTagFromQuiz(Long quizId, Long tagId);
} 