package com.quizplatform.modules.quiz.application.port.in;

import com.quizplatform.modules.quiz.presentation.dto.QuizResponse; // Use existing DTO
import java.util.Optional;

/**
 * 외부 모듈에서 퀴즈 정보를 동기적으로 조회하기 위한 Inbound Port 인터페이스
 */
public interface QuizQueryPort {

    /**
     * 퀴즈 ID로 문제를 포함한 퀴즈 정보를 조회합니다.
     * @param quizId 조회할 퀴즈 ID
     * @return 퀴즈 정보 DTO (QuizResponse), 퀴즈가 없거나 접근 불가 시 Optional.empty()
     */
    Optional<QuizResponse> getQuizWithQuestionsById(Long quizId);

    // 필요에 따라 다른 조회 메소드 추가 가능
} 