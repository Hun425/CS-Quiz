package com.quizplatform.quiz.adapter.out.persistence.repository;

import com.quizplatform.quiz.domain.model.QuizAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 퀴즈 답변 리포지토리
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Repository
public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {
    
    /**
     * 문제 ID로 답변 목록 조회
     * 
     * @param questionId 문제 ID
     * @return 답변 목록
     */
    List<QuizAnswer> findByQuestionId(Long questionId);
    
    /**
     * 문제 ID와 정답 여부로 답변 목록 조회
     * 
     * @param questionId 문제 ID
     * @param isCorrect 정답 여부
     * @return 답변 목록
     */
    List<QuizAnswer> findByQuestionIdAndIsCorrect(Long questionId, boolean isCorrect);
    
    /**
     * 문제 ID로 정답 목록 조회
     * 
     * @param questionId 문제 ID
     * @return 정답 목록
     */
    default List<QuizAnswer> findCorrectAnswersByQuestionId(Long questionId) {
        return findByQuestionIdAndIsCorrect(questionId, true);
    }
} 