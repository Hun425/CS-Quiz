package application.port.in;

import domain.model.QuizStatistics;

import java.util.Optional;

/**
 * 퀴즈 및 사용자별 퀴즈 통계 조회 기능을 정의하는 유스케이스 인터페이스
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
 */
public interface GetQuizStatisticsUseCase {
    /**
     * 퀴즈의 통계 정보를 조회합니다.
     *
     * @param quizId 퀴즈 ID
     * @return 퀴즈 통계 (Optional)
     */
    Optional<QuizStatistics> getQuizStatistics(Long quizId);
    
    /**
     * 사용자의 퀴즈 시도 통계를 조회합니다.
     *
     * @param userId 사용자 ID
     * @param quizId 퀴즈 ID
     * @return 사용자별 퀴즈 통계 (Optional)
     */
    Optional<QuizStatistics> getUserQuizStatistics(Long userId, Long quizId);
}