package com.quizplatform.core.repository.question;


import com.quizplatform.core.domain.question.Question;
import com.quizplatform.core.domain.question.QuestionAttempt;
import com.quizplatform.core.domain.quiz.QuizAttempt;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionAttemptRepository extends JpaRepository<QuestionAttempt, Long> {
    List<QuestionAttempt> findByQuizAttempt(QuizAttempt quizAttempt);

    @Query("SELECT qa FROM QuestionAttempt qa " +
            "WHERE qa.question = :question AND qa.isCorrect = false " +
            "GROUP BY qa.userAnswer " +
            "ORDER BY COUNT(qa.userAnswer) DESC")
    List<Object[]> findCommonWrongAnswers(
            @Param("question") Question question,
            Pageable pageable
    );

    @Query("SELECT COUNT(qa) FROM QuestionAttempt qa WHERE qa.quizAttempt.user.id = :userId AND qa.isCorrect = true")
    long countCorrectAnswersByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(qa) FROM QuestionAttempt qa WHERE qa.quizAttempt.user.id = :userId")
    long countTotalQuestionsByUserId(@Param("userId") Long userId);
}
