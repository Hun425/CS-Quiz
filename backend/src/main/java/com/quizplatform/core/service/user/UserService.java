package com.quizplatform.core.service.user;

import com.quizplatform.core.domain.quiz.Achievement;
import com.quizplatform.core.domain.quiz.QuizAttempt;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.domain.user.UserLevel;
import com.quizplatform.core.dto.user.*;
import com.quizplatform.core.exception.BusinessException;
import com.quizplatform.core.exception.ErrorCode;
import com.quizplatform.core.repository.UserRepository;
import com.quizplatform.core.repository.question.QuestionAttemptRepository;
import com.quizplatform.core.repository.quiz.QuizAttemptRepository;
import com.quizplatform.core.repository.tag.TagRepository;
import com.quizplatform.core.repository.user.AchievementRepository;
import com.quizplatform.core.repository.user.UserLevelHistoryRepository;
import com.quizplatform.core.repository.user.UserLevelRepository;
import com.quizplatform.core.service.common.EntityMapperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 사용자 관련 비즈니스 로직을 정의하는 인터페이스
 * 사용자 프로필 조회, 통계 계산, 최근 활동, 업적, 주제별 성과 조회 및 프로필 업데이트 기능을 제공합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
public interface UserService {

    /**
     * 사용자 ID를 이용하여 사용자 엔티티를 조회합니다.
     *
     * @param userId 조회할 사용자의 ID
     * @return 조회된 User 엔티티
     * @throws com.quizplatform.core.exception.BusinessException 사용자를 찾을 수 없을 경우 (USER_NOT_FOUND)
     */
    User getUserById(Long userId);

    /**
     * 사용자 ID를 이용하여 사용자의 프로필 정보를 조회합니다.
     * 사용자 기본 정보 및 레벨, 경험치, 포인트 등을 포함합니다.
     *
     * @param userId 조회할 사용자의 ID
     * @return 사용자 프로필 정보 DTO (UserProfileDto)
     * @throws com.quizplatform.core.exception.BusinessException 사용자를 찾을 수 없을 경우 (USER_NOT_FOUND)
     */
    UserProfileDto getUserProfile(Long userId);

    /**
     * 사용자의 퀴즈 관련 통계 정보를 조회합니다.
     * 총 퀴즈 시도/완료 횟수, 평균 점수, 정답률, 총 소요 시간, 최고/최저 점수 등을 계산합니다.
     * 조회된 결과는 캐싱될 수 있습니다.
     *
     * @param userId 통계를 조회할 사용자의 ID
     * @return 사용자 퀴즈 통계 정보 DTO (UserStatisticsDto)
     * @throws com.quizplatform.core.exception.BusinessException 사용자를 찾을 수 없을 경우 (USER_NOT_FOUND)
     */
    UserStatisticsDto getUserStatistics(Long userId);

    /**
     * 사용자의 최근 활동 내역 (퀴즈 시도, 업적 획득, 레벨업)을 조회합니다.
     * 여러 종류의 활동을 시간 순서대로 조합하여 반환합니다.
     *
     * @param userId  활동 내역을 조회할 사용자의 ID
     * @param limit   조회할 최대 활동 개수
     * @return 최근 활동 내역 DTO (RecentActivityDto) 리스트
     * @throws com.quizplatform.core.exception.BusinessException 사용자를 찾을 수 없을 경우 (USER_NOT_FOUND)
     */
    List<RecentActivityDto> getRecentActivities(Long userId, int limit);

    /**
     * 사용자의 업적 획득 현황 및 진행도를 조회합니다.
     * 모든 정의된 업적에 대해 사용자의 획득 여부, 획득 시각, 현재 진행률을 계산하여 반환합니다.
     * 사용자의 UserLevel 정보가 없으면 자동으로 생성합니다.
     *
     * @param userId 업적 현황을 조회할 사용자의 ID
     * @return 업적 정보 DTO (AchievementDto) 리스트
     * @throws com.quizplatform.core.exception.BusinessException 사용자를 찾을 수 없을 경우 (USER_NOT_FOUND)
     */
    List<AchievementDto> getAchievements(Long userId);

    /**
     * 사용자의 주제별 성과 데이터를 조회합니다.
     * 각 주제(태그)별 완료한 퀴즈 수, 평균 점수, 총 경험치를 계산합니다.
     *
     * @param userId 주제별 성과를 조회할 사용자의 ID
     * @return 주제별 성과 정보 DTO (TopicPerformanceDto) 리스트
     * @throws com.quizplatform.core.exception.BusinessException 사용자를 찾을 수 없을 경우 (USER_NOT_FOUND)
     */
    List<TopicPerformanceDto> getTopicPerformance(Long userId);

    /**
     * 사용자 프로필 정보를 업데이트합니다.
     * 변경 가능한 필드: 닉네임, 이메일, 소개, 프로필 이미지 URL 등
     * 업데이트 후 관련 캐시가 자동으로 제거됩니다.
     *
     * @param userId 업데이트할 사용자의 ID
     * @param request 업데이트할 프로필 정보가 담긴 요청 객체
     * @return 업데이트된 사용자 프로필 정보 DTO (UserProfileDto)
     * @throws com.quizplatform.core.exception.BusinessException 사용자를 찾을 수 없을 경우 (USER_NOT_FOUND) 또는
     *                                                          이메일/닉네임 중복 시 (EMAIL_ALREADY_EXISTS, NICKNAME_ALREADY_EXISTS)
     */
    UserProfileDto updateProfile(Long userId, UserProfileUpdateRequest request);
}