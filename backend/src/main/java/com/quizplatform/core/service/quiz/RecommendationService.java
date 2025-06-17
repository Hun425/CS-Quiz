package com.quizplatform.core.service.quiz;

import com.quizplatform.core.domain.quiz.DifficultyLevel;
import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.quiz.QuizAttempt;
import com.quizplatform.core.domain.quiz.QuizType;
import com.quizplatform.core.domain.tag.Tag;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.dto.quiz.QuizSummaryResponse;
import com.quizplatform.core.exception.BusinessException;
import com.quizplatform.core.exception.ErrorCode;
import com.quizplatform.core.repository.quiz.QuizAttemptRepository;
import com.quizplatform.core.repository.quiz.QuizRepository;
import com.quizplatform.core.repository.tag.TagRepository;
import com.quizplatform.core.service.common.EntityMapperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 사용자에게 다양한 유형의 퀴즈를 추천하는 로직을 담당하는 서비스 인터페이스입니다.
 * 개인 맞춤, 인기, 카테고리, 난이도, 데일리 퀴즈 연관 등 다양한 기준의 추천 기능을 제공합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
public interface RecommendationService {

    /**
     * 사용자의 최근 퀴즈 시도 기록을 분석하여 맞춤형 퀴즈를 추천합니다.
     * 사용자가 선호하는 태그와 적정 난이도를 파악하여, 시도하지 않은 퀴즈 중 관련성이 높은 퀴즈를 추천합니다.
     * 사용자 데이터가 부족할 경우 인기 퀴즈를 대신 추천합니다.
     *
     * @param user  추천을 받을 사용자
     * @param limit 추천할 퀴즈의 최대 개수
     * @return 추천된 퀴즈 요약 정보 DTO 리스트
     */
    List<QuizSummaryResponse> getPersonalizedRecommendations(User user, int limit);

    /**
     * 조회수, 시도 횟수, 평균 점수 등을 종합적으로 고려하여 인기 있는 퀴즈를 추천합니다.
     *
     * @param limit 추천할 퀴즈의 최대 개수
     * @return 인기 퀴즈 요약 정보 DTO 리스트
     */
    List<QuizSummaryResponse> getPopularQuizzes(int limit);

    /**
     * 특정 카테고리(태그) 및 그 하위 카테고리에 속하는 퀴즈 중에서 인기 있는 퀴즈를 추천합니다.
     *
     * @param tagId 추천 기준이 되는 태그(카테고리)의 ID
     * @param limit 추천할 퀴즈의 최대 개수
     * @return 카테고리 기반 추천 퀴즈 요약 정보 DTO 리스트
     * @throws BusinessException 해당 tagId의 태그를 찾을 수 없을 경우 (TAG_NOT_FOUND)
     */
    List<QuizSummaryResponse> getCategoryRecommendations(Long tagId, int limit);

    /**
     * 특정 난이도에 해당하는 퀴즈 중에서 인기 있는 퀴즈를 추천합니다.
     *
     * @param difficultyStr 추천 기준이 되는 난이도 문자열 (예: "BEGINNER", "INTERMEDIATE", "ADVANCED")
     * @param limit         추천할 퀴즈의 최대 개수
     * @return 난이도 기반 추천 퀴즈 요약 정보 DTO 리스트
     * @throws BusinessException 유효하지 않은 난이도 문자열이 입력된 경우 (INVALID_INPUT_VALUE)
     */
    List<QuizSummaryResponse> getDifficultyBasedRecommendations(String difficultyStr, int limit);
    
    /**
     * 현재 데일리 퀴즈와 관련된 추천 퀴즈를 반환합니다.
     * 데일리 퀴즈와 동일한 태그를 가진 퀴즈, 비슷한 난이도의 퀴즈 등을 추천합니다.
     *
     * @param limit 추천할 퀴즈의 최대 개수
     * @return 데일리 퀴즈 연관 추천 퀴즈 요약 정보 DTO 리스트
     */
    List<QuizSummaryResponse> getDailyRelatedRecommendations(int limit);
}