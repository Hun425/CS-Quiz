package application.port.in;

import domain.model.QuizAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 퀴즈 시도 정보 조회 기능을 정의하는 유스케이스 인터페이스
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
 */
public interface GetQuizAttemptUseCase {
    /**
     * ID로 퀴즈 시도를 조회합니다.
     *
     * @param quizAttemptId 퀴즈 시도 ID
     * @return 퀴즈 시도 (Optional)
     */
    Optional<QuizAttempt> getQuizAttemptById(Long quizAttemptId);
    
    /**
     * 사용자의 특정 퀴즈 시도 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @param quizId 퀴즈 ID
     * @param pageable 페이징 정보
     * @return 퀴즈 시도 페이지
     */
    Page<QuizAttempt> getUserQuizAttempts(Long userId, Long quizId, Pageable pageable);
    
    /**
     * 사용자의 모든 퀴즈 시도 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 퀴즈 시도 페이지
     */
    Page<QuizAttempt> getAllUserQuizAttempts(Long userId, Pageable pageable);

    /**
     * 사용자의 퀴즈 시도 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 퀴즈 시도 페이지
     */
    Page<QuizAttempt> getQuizAttemptsByUserId(Long userId, Pageable pageable);
    
    /**
     * 사용자의 최근 퀴즈 시도 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @param limit 조회할 최대 개수
     * @return 퀴즈 시도 목록
     */
    List<QuizAttempt> getRecentQuizAttemptsByUserId(Long userId, int limit);
    
    /**
     * 사용자의 특정 퀴즈의 최고 점수 시도 조회합니다.
     *
     * @param userId 사용자 ID
     * @param quizId 퀴즈 ID
     * @return 최고 점수의 퀴즈 시도
     */
    QuizAttempt getBestQuizAttempt(Long userId, Long quizId);
}