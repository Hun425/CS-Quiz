package adapter.in.web.dto;


import adapter.out.persistence.entity.QuizAttemptJpaEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 퀴즈 시도 결과 응답 DTO 클래스
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttemptResponse {

    private Long id;
    private Long userId;
    private QuizResponse quiz;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer score;
    private Boolean passed;
    private QuizAttemptJpaEntity.AttemptStatus status;
    @Builder.Default
    private List<QuestionAttemptResponse> questionAttempts = new ArrayList<>();
    
    // 추가 통계 정보
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer maxPossibleScore;
    private Double percentageScore;
    private Long timeSpentSeconds;
}
