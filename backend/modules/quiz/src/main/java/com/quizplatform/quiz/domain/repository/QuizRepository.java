package com.quizplatform.quiz.domain.repository;

import com.quizplatform.quiz.domain.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 퀴즈 리포지토리 인터페이스
 */
@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    
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
     * 생성자 ID로 퀴즈 목록 조회
     * 
     * @param creatorId 생성자 ID
     * @return 퀴즈 목록
     */
    List<Quiz> findByCreatorId(Long creatorId);
    
    /**
     * 활성화된 퀴즈 중 카테고리로 목록 조회
     * 
     * @param category 카테고리
     * @param active 활성화 여부
     * @return 퀴즈 목록
     */
    List<Quiz> findByCategoryAndActive(String category, boolean active);
} 