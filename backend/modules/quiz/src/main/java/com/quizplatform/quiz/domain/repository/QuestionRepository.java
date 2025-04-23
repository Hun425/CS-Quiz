package com.quizplatform.quiz.domain.repository;

import com.quizplatform.quiz.domain.model.Question;
import com.quizplatform.quiz.domain.model.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 문제 리포지토리
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    /**
     * 퀴즈 ID로 문제 목록 조회
     * 
     * @param quizId 퀴즈 ID
     * @return 문제 목록
     */
    List<Question> findByQuizId(Long quizId);
    
    /**
     * 퀴즈 ID로 문제 목록 조회 (순서 정렬)
     * 
     * @param quizId 퀴즈 ID
     * @return 순서대로 정렬된 문제 목록
     */
    List<Question> findByQuizIdOrderByOrderAsc(Long quizId);
    
    /**
     * 퀴즈 ID와 문제 유형으로 문제 목록 조회
     * 
     * @param quizId 퀴즈 ID
     * @param questionType 문제 유형
     * @return 문제 목록
     */
    List<Question> findByQuizIdAndQuestionType(Long quizId, QuestionType questionType);
    
    /**
     * 퀴즈의 평균 난이도 조회
     * 
     * @param quizId 퀴즈 ID
     * @return 평균 배점
     */
    @Query("SELECT AVG(q.points) FROM Question q WHERE q.quiz.id = :quizId")
    Double findAveragePointsByQuizId(Long quizId);
    
    /**
     * 퀴즈의 총 문제 수 조회
     * 
     * @param quizId 퀴즈 ID
     * @return 문제 수
     */
    long countByQuizId(Long quizId);
} 