package com.quizplatform.quiz.application.port.in;

/**
 * 퀴즈 답변 검증 유스케이스
 * 사용자가 제출한 답변이 정답인지 검증하는 인터페이스
 */
public interface ValidateQuizAnswerUseCase {

    /**
     * 사용자가 제출한 답변이 정답인지 검증
     * 
     * @param requestId 요청 ID (비동기 처리 추적용)
     * @param userId 사용자 ID
     * @param questionId 문제 ID
     * @param submittedAnswer 사용자가 제출한 답변
     */
    void validateAnswer(String requestId, String userId, String questionId, String submittedAnswer);
}
