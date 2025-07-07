package com.quizplatform.core.service.quiz;

import com.quizplatform.core.domain.question.Question;
import com.quizplatform.core.domain.quiz.*;
import com.quizplatform.core.domain.tag.Tag;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.dto.question.QuestionCreateRequest;
import com.quizplatform.core.dto.quiz.*;
import com.quizplatform.core.exception.BusinessException;
import com.quizplatform.core.exception.ErrorCode;
import com.quizplatform.core.repository.UserRepository;
import com.quizplatform.core.repository.quiz.QuizAttemptRepository;
import com.quizplatform.core.repository.quiz.QuizRepository;
import com.quizplatform.core.repository.tag.TagRepository;
import com.quizplatform.core.service.common.EntityMapperService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 퀴즈 관련 비즈니스 로직을 정의하는 인터페이스
 * 
 * <p>퀴즈 생성, 조회, 수정, 통계 분석 등 퀴즈 관련 핵심 기능을 제공합니다.</p>
 * 
 * @author 채기훈
 */
public interface QuizService {
    
    /**
     * 새로운 퀴즈를 생성합니다.
     * 
     * @param creatorId 퀴즈 생성자 ID
     * @param request 퀴즈 생성 요청 데이터
     * @return 생성된 퀴즈 정보
     */
    QuizResponse createQuiz(Long creatorId, QuizCreateRequest request);
    
    /**
     * 기존 퀴즈를 수정합니다.
     * 
     * @param quizId 수정할 퀴즈 ID
     * @param request 퀴즈 수정 요청 데이터
     * @return 수정된 퀴즈 정보
     */
    QuizResponse updateQuiz(Long quizId, QuizCreateRequest request);
    
    /**
     * 데일리 퀴즈를 생성합니다.
     * 
     * @return 생성된 데일리 퀴즈 정보
     */
    QuizResponse createDailyQuiz();
    
    /**
     * 주어진 조건에 맞는 퀴즈 목록을 검색합니다.
     * 
     * @param condition 검색 조건
     * @param pageable 페이징 정보
     * @return 검색된 퀴즈 목록 (페이징)
     */
    Page<QuizSummaryResponse> searchQuizzesDto(QuizSubmitRequest.QuizSearchCondition condition, Pageable pageable);
    
    /**
     * 문제 내용을 제외한 퀴즈 정보를 조회합니다.
     * 
     * @param quizId 조회할 퀴즈 ID
     * @return 퀴즈 상세 정보 (문제 제외)
     */
    QuizDetailResponse getQuizWithoutQuestions(Long quizId);
    
    /**
     * 문제를 포함한 퀴즈 정보를 조회합니다.
     * 
     * @param quizId 조회할 퀴즈 ID
     * @return 퀴즈 상세 정보 (문제 포함)
     */
    QuizResponse getQuizWithQuestions(Long quizId);
    
    /**
     * 현재 유효한 데일리 퀴즈를 조회합니다.
     * 
     * @return 현재 데일리 퀴즈 정보
     */
    QuizResponse getCurrentDailyQuiz();
    
    /**
     * 특정 태그에 속한 퀴즈 목록을 조회합니다.
     * 
     * @param tagId 태그 ID
     * @param pageable 페이징 정보
     * @return 해당 태그의 퀴즈 목록 (페이징)
     */
    Page<QuizSummaryResponse> getQuizzesByTag(Long tagId, Pageable pageable);
    
    /**
     * 퀴즈의 상세 통계 정보를 조회합니다.
     * 
     * @param quizId 퀴즈 ID
     * @return 퀴즈 통계 정보
     */
    QuizStatisticsResponse getQuizStatistics(Long quizId);
    
    /**
     * 사용자에게 추천할 퀴즈 목록을 조회합니다.
     * 
     * @param user 사용자
     * @param limit 최대 조회 개수
     * @return 추천 퀴즈 목록
     */
    List<QuizSummaryResponse> getRecommendedQuizzes(User user, int limit);
    
    /**
     * 플레이 가능한 퀴즈 정보를 조회합니다.
     * 
     * @param quizId 퀴즈 ID
     * @param userId 사용자 ID
     * @return 플레이 가능한 퀴즈 정보
     */
    QuizResponse getPlayableQuiz(Long quizId, Long userId);
    
    /**
     * 퀴즈를 상세 응답 객체로 변환합니다.
     * 
     * @param quiz 퀴즈 엔티티
     * @return 퀴즈 상세 응답 객체
     */
    QuizDetailResponse convertToDetailResponse(Quiz quiz);
}