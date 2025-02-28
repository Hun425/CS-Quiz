package com.quizplatform.core.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmitRequest {
    // 퀴즈 시도 ID (startQuiz에서 생성된 ID)
    private Long quizAttemptId;

    // 각 문제 ID에 대응하는 답변 맵
    private Map<Long, String> answers;

    // 총 소요 시간 (초)
    private Integer timeTaken;
}