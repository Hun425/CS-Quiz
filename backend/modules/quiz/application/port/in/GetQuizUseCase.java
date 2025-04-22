package application.port.in;

import domain.model.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 퀴즈 조회 관련 기능을 정의하는 유스케이스 인터페이스
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
 */
public interface GetQuizUseCase {

    /**
     * ID로 퀴즈를 조회합니다. (필요 시 문제를 함께 로드)
     *
     * @param quizId 퀴즈 ID
     * @return 퀴즈
     * @throws quiz.domain.exception.QuizNotFoundException 퀴즈를 찾을 수 없을 때
     */
    Quiz getQuiz(Long quizId);

    /**
     * 모든 공개된 퀴즈 목록을 페이징하여 조회합니다.
     *
     * @param pageable 페이징 정보
     * @return 퀴즈 페이지
     */
    Page<Quiz> getQuizzes(Pageable pageable);

    /**
     * 현재 유효한 데일리 퀴즈를 조회합니다.
     *
     * @return 데일리 퀴즈
     * @throws quiz.domain.exception.QuizNotFoundException 데일리 퀴즈를 찾을 수 없을 때
     */
    Quiz getDailyQuiz();

    /**
     * 특정 태그의 퀴즈 목록을 조회합니다.
     *
     * @param tagId 태그 ID
     * @param pageable 페이징 정보
     * @return 퀴즈 페이지
     */
    Page<Quiz> getQuizzesByTag(Long tagId, Pageable pageable);

    /**
     * 사용자가 만든 퀴즈 목록을 조회합니다.
     *
     * @param creatorId 생성자 ID
     * @param pageable 페이징 정보
     * @return 퀴즈 페이지
     */
    Page<Quiz> getQuizzesByUser(Long creatorId, Pageable pageable);
}