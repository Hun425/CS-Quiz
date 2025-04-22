package application.port.in;

import application.port.in.command.SearchQuizCommand;
import domain.model.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 퀴즈 검색 및 추천 기능을 정의하는 유스케이스 인터페이스
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
 */
public interface SearchQuizUseCase {
    /**
     * 퀴즈를 검색합니다.
     *
     * @param command 검색 명령
     * @param pageable 페이징 정보
     * @return 검색된 퀴즈 페이지
     */
    Page<Quiz> searchQuizzes(SearchQuizCommand command, Pageable pageable);
    
    /**
     * 추천 퀴즈를 조회합니다.
     *
     * @param userId 사용자 ID
     * @param limit 조회할 최대 개수
     * @return 추천 퀴즈 목록
     */
    List<Quiz> getRecommendedQuizzes(Long userId, int limit);
}