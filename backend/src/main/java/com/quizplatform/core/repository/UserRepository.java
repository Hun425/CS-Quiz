package com.quizplatform.core.repository;

import com.quizplatform.core.domain.user.AuthProvider;
import com.quizplatform.core.domain.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Map;
import java.util.Optional;

/**
 * User 엔티티에 대한 데이터 접근을 처리하는 리포지토리 인터페이스입니다.
 * 사용자 정보의 조회, 저장, 존재 여부 확인 및 소셜 로그인 관련 조회를 담당합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일 주소를 이용하여 사용자를 조회합니다.
     * 조회 시 사용자의 배틀 통계 정보(battleStats)를 즉시 로딩합니다.
     * (@EntityGraph 사용)
     *
     * @param email 조회할 사용자의 이메일 주소
     * @return 해당 이메일의 사용자 정보를 담은 Optional 객체 (배틀 통계 포함)
     */
    @EntityGraph(attributePaths = {"battleStats"}) // battleStats 연관 관계 EAGER 로딩
    Optional<User> findByEmail(String email);

    /**
     * 주어진 이메일 주소를 가진 사용자가 존재하는지 확인합니다.
     *
     * @param email 확인할 이메일 주소
     * @return 해당 이메일의 사용자가 존재하면 true, 아니면 false
     */
    boolean existsByEmail(String email);

    /**
     * 주어진 사용자 이름(username)을 가진 사용자가 존재하는지 확인합니다.
     *
     * @param username 확인할 사용자 이름
     * @return 해당 사용자 이름의 사용자가 존재하면 true, 아니면 false
     */
    boolean existsByUsername(String username);

    /**
     * 소셜 로그인 제공자(AuthProvider)와 해당 제공자에서의 고유 ID(providerId)를 이용하여 사용자를 조회합니다.
     * 소셜 로그인 연동 및 사용자 식별에 사용됩니다.
     *
     * @param provider   소셜 로그인 제공자 (예: GOOGLE, KAKAO)
     * @param providerId 해당 제공자에서 발급된 사용자의 고유 ID
     * @return 해당 소셜 로그인 정보로 등록된 사용자 정보를 담은 Optional 객체
     */
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    /**
     * 사용자 ID를 이용하여 사용자를 조회하며, 배틀 통계 정보(battleStats)를 즉시 로딩합니다.
     * (JPQL 및 FETCH JOIN 사용)
     *
     * @param id 조회할 사용자의 ID
     * @return 해당 ID의 사용자 정보를 담은 Optional 객체 (배틀 통계 포함)
     */
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.battleStats " + // battleStats 즉시 로딩
            "WHERE u.id = :id")
    Optional<User> findByIdWithStats(@Param("id") Long id);

    /**
     * 사용자 ID를 이용하여 사용자를 조회하며, 배틀 통계 정보(battleStats)와
     * 퀴즈 시도 목록(quizAttempts)을 즉시 로딩합니다.
     * (JPQL 및 FETCH JOIN 사용)
     *
     * @param id 조회할 사용자의 ID
     * @return 해당 ID의 사용자 정보를 담은 Optional 객체 (배틀 통계 및 퀴즈 시도 목록 포함)
     */
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.battleStats " + // battleStats 즉시 로딩
            "LEFT JOIN FETCH u.quizAttempts " + // quizAttempts 즉시 로딩
            "WHERE u.id = :id")
    Optional<User> findByIdWithStatsAndAttempts(@Param("id") Long id);

    /**
     * 사용자 프로필 정보를 DTO로 직접 조회합니다. (N+1 문제 방지)
     * UserProfileDto 생성자에 맞게 필드를 선택하고, 날짜는 문자열로 변환합니다.
     *
     * @param id 조회할 사용자의 ID
     * @return 사용자 프로필 DTO 객체
     */
    @Query("SELECT new com.quizplatform.core.dto.user.UserProfileDto(" +
            "u.id, u.username, u.email, u.profileImage, " + // role, provider 제외
            "u.level, u.experience, u.requiredExperience, u.totalPoints, " + // 순서 수정
            "FUNCTION('TO_CHAR', u.createdAt, 'YYYY-MM-DD HH24:MI:SS'), " + // joinedAt (생성일) 추가 및 포맷팅
            "FUNCTION('TO_CHAR', u.lastLogin, 'YYYY-MM-DD HH24:MI:SS')) " + // lastLogin 추가 및 포맷팅
            // 퀴즈 시도/리뷰 카운트 서브쿼리 제외
            "FROM User u WHERE u.id = :id")
    Optional<com.quizplatform.core.dto.user.UserProfileDto> findUserProfileDtoById(@Param("id") Long id);

    /**
     * 사용자 통계 데이터를 직접 조회합니다. (Native Query)
     *
     * @param userId 조회할 사용자의 ID
     * @return 사용자 통계 데이터 맵
     */
    @Query(value = "SELECT " +
           "COUNT(qa.id) as total_quizzes, " +
           "SUM(CASE WHEN qa.is_completed = true THEN 1 ELSE 0 END) as completed_quizzes, " +
           "AVG(qa.score) as avg_score, " +
           "SUM(CASE WHEN qqa.is_correct = true THEN 1 ELSE 0 END) as correct_answers, " +
           "COUNT(qqa.id) as total_questions, " +
           "CASE WHEN COUNT(qqa.id) > 0 THEN (SUM(CASE WHEN qqa.is_correct = true THEN 1 ELSE 0 END) * 100.0 / COUNT(qqa.id)) ELSE 0 END as correct_rate, " +
           "SUM(qa.time_taken) as total_time, " +
           "MAX(qa.score) as best_score, " +
           "MIN(qa.score) as worst_score " +
           "FROM users u " +
           "LEFT JOIN quiz_attempts qa ON u.id = qa.user_id " +
           "LEFT JOIN question_attempts qqa ON qa.id = qqa.quiz_attempt_id " +
           "WHERE u.id = :userId " +
           "GROUP BY u.id", 
           nativeQuery = true)
    Map<String, Object> getUserStatisticsData(@Param("userId") Long userId);
}