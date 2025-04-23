package com.quizplatform.quiz.domain.repository;

import com.quizplatform.quiz.domain.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 퀴즈 시도 리포지토리 인터페이스
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    
    /**
     * 사용자 ID로 퀴즈 시도 목록 조회
     * 
     * @param userId 사용자 ID
     * @return 퀴즈 시도 목록
     */
    List<QuizAttempt> findByUserId(Long userId);
    
    /**
     * 퀴즈 ID로 퀴즈 시도 목록 조회
     * 
     * @param quizId 퀴즈 ID
     * @return 퀴즈 시도 목록
     */
    List<QuizAttempt> findByQuizId(Long quizId);
    
    /**
     * 사용자 ID와 퀴즈 ID로 완료된 퀴즈 시도 조회
     * 
     * @param userId 사용자 ID
     * @param quizId 퀴즈 ID
     * @param completed 완료 여부
     * @return 퀴즈 시도
     */
    Optional<QuizAttempt> findByUserIdAndQuizIdAndCompleted(Long userId, Long quizId, boolean completed);
    
    /**
     * 사용자 ID와 퀴즈 ID로 가장 최근의 퀴즈 시도 조회
     * 
     * @param userId 사용자 ID
     * @param quizId 퀴즈 ID
     * @return 가장 최근의 퀴즈 시도
     */
    Optional<QuizAttempt> findTopByUserIdAndQuizIdOrderByStartedAtDesc(Long userId, Long quizId);
    
    /**
     * 사용자 ID와 완료 여부로 시도 수 조회
     * 
     * @param userId 사용자 ID
     * @param completed 완료 여부
     * @return 시도 수
     */
    long countByUserIdAndCompleted(Long userId, boolean completed);
} 