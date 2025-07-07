package com.quizplatform.core.repository.question;

import com.quizplatform.core.domain.question.Question;
import com.quizplatform.core.domain.question.QuestionAttempt;
import com.quizplatform.core.domain.quiz.QuizAttempt;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * QuestionAttempt 엔티티에 대한 데이터 접근을 처리하는 리포지토리 인터페이스입니다.
 * 사용자의 개별 문제 답변 정보를 관리합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
public interface QuestionAttemptRepository extends JpaRepository<QuestionAttempt, Long> {

    /**
     * 특정 퀴즈 시도(QuizAttempt)에 속하는 모든 문제 시도(QuestionAttempt) 목록을 조회합니다.
     *
     * @param quizAttempt 조회할 대상 퀴즈 시도 객체
     * @return 해당 퀴즈 시도에 포함된 QuestionAttempt 엔티티 리스트
     */
    List<QuestionAttempt> findByQuizAttempt(QuizAttempt quizAttempt);

    /**
     * 특정 문제(Question)에 대해 사용자들이 제출한 오답 중에서 가장 흔한 오답들을 빈도순으로 조회합니다.
     * 오답(isCorrect = false)만 대상으로 하며, 제출된 답변(userAnswer)으로 그룹화하여 개수를 셉니다.
     * 결과는 오답 빈도가 높은 순서대로 정렬됩니다.
     *
     * @param question 오답 통계를 조회할 대상 문제 객체
     * @param pageable 페이징 정보 (조회할 개수 제한 등)
     * @return 흔한 오답 정보 리스트. 각 요소는 Object 배열이며, [userAnswer(String), count(Long)] 형태입니다.
     */
    @Query("SELECT qa.userAnswer, COUNT(qa.userAnswer) FROM QuestionAttempt qa " +
            "WHERE qa.question = :question AND qa.isCorrect = false " +
            "GROUP BY qa.userAnswer " +
            "ORDER BY COUNT(qa.userAnswer) DESC")
    List<Object[]> findCommonWrongAnswers(
                                           @Param("question") Question question,
                                           Pageable pageable
    );

    /**
     * 특정 사용자가 모든 퀴즈 시도에서 맞힌 문제의 총 개수를 조회합니다.
     *
     * @param userId 조회할 사용자의 ID
     * @return 해당 사용자가 맞힌 총 문제 개수 (long)
     */
    @Query("SELECT COUNT(qa) FROM QuestionAttempt qa WHERE qa.quizAttempt.user.id = :userId AND qa.isCorrect = true")
    long countCorrectAnswersByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자가 모든 퀴즈 시도에서 답변한 문제의 총 개수를 조회합니다.
     * (맞힌 문제 + 틀린 문제 모두 포함)
     *
     * @param userId 조회할 사용자의 ID
     * @return 해당 사용자가 답변한 총 문제 개수 (long)
     */
    @Query("SELECT COUNT(qa) FROM QuestionAttempt qa WHERE qa.quizAttempt.user.id = :userId")
    long countTotalQuestionsByUserId(@Param("userId") Long userId);
}