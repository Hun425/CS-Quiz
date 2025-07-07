package com.quizplatform.core.service.quiz;

import com.quizplatform.core.domain.question.Question;
import com.quizplatform.core.domain.question.QuestionAttempt;
import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.quiz.QuizAttempt;
import com.quizplatform.core.domain.quiz.QuizType;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.dto.question.QuestionAttemptDto;
import com.quizplatform.core.dto.quiz.QuizResultResponse;
import com.quizplatform.core.dto.quiz.QuizSubmitRequest;
import com.quizplatform.core.exception.BusinessException;
import com.quizplatform.core.exception.ErrorCode;
import com.quizplatform.core.repository.question.QuestionAttemptRepository;
import com.quizplatform.core.repository.quiz.QuizAttemptRepository;
import com.quizplatform.core.repository.quiz.QuizRepository;
import com.quizplatform.core.service.common.EntityMapperService;
import com.quizplatform.core.service.level.LevelingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 퀴즈 시도(QuizAttempt)와 관련된 비즈니스 로직을 정의하는 인터페이스
 * 퀴즈 시도 시작, 개별 문제 답변 제출, 퀴즈 완료, 결과 조회 및 최종 제출 처리 등을 담당합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
public interface QuizAttemptService {
    
    /**
     * 사용자가 특정 퀴즈에 대한 시도를 시작합니다.
     * 새로운 QuizAttempt 레코드를 생성하고 저장합니다.
     * DAILY 타입 퀴즈의 경우, 이미 완료했는지 확인합니다.
     *
     * @param quizId 시작할 퀴즈의 ID
     * @param user   퀴즈를 시도하는 사용자
     * @return 생성된 QuizAttempt 객체
     * @throws BusinessException 퀴즈를 찾을 수 없거나 (QUIZ_NOT_FOUND),
     * 데일리 퀴즈를 이미 완료했을 경우 (QUIZ_ALREADY_COMPLETED)
     */
    QuizAttempt startQuiz(Long quizId, User user);

    /**
     * 진행 중인 퀴즈 시도 내에서 특정 문제에 대한 사용자의 답변을 제출(저장)합니다.
     * 퀴즈 시도가 완료되었거나 시간 제한이 초과된 경우 답변을 제출할 수 없습니다.
     *
     * @param quizAttemptId 진행 중인 퀴즈 시도의 ID
     * @param questionId    답변을 제출할 문제의 ID
     * @param answer        사용자가 제출한 답변 내용
     * @return 생성된 QuestionAttempt 객체 (제출된 답변 정보 포함)
     * @throws BusinessException 퀴즈 시도 또는 문제를 찾을 수 없거나 (ENTITY_NOT_FOUND),
     * 퀴즈가 이미 완료되었거나 (QUIZ_ALREADY_COMPLETED),
     * 시간이 만료된 경우 (QUIZ_TIME_EXPIRED)
     */
    QuestionAttempt submitAnswer(Long quizAttemptId, Long questionId, String answer);

    /**
     * 특정 퀴즈 시도를 완료 상태로 변경합니다.
     * 이미 완료된 퀴즈 시도에 대해서는 오류를 발생시킵니다.
     *
     * @param quizAttemptId 완료 처리할 퀴즈 시도의 ID
     * @return 완료 처리된 QuizAttempt 객체
     * @throws BusinessException 퀴즈 시도를 찾을 수 없거나 (ENTITY_NOT_FOUND),
     * 이미 완료된 퀴즈일 경우 (QUIZ_ALREADY_COMPLETED)
     */
    QuizAttempt completeQuiz(Long quizAttemptId);

    /**
     * 완료된 퀴즈 시도의 개별 문제 결과 목록을 조회합니다.
     * 각 문제에 대한 사용자의 답변, 정답 여부 등을 포함합니다.
     * 퀴즈가 아직 완료되지 않은 경우 오류를 발생시킵니다.
     *
     * @param quizAttemptId 결과를 조회할 퀴즈 시도의 ID
     * @return 문제 시도 결과(QuestionAttemptDto) 리스트
     * @throws BusinessException 퀴즈 시도를 찾을 수 없거나 (ENTITY_NOT_FOUND),
     * 퀴즈가 아직 완료되지 않았을 경우 (BATTLE_NOT_STARTED - ErrorCode 개선 필요)
     */
    List<QuestionAttemptDto> getQuizResults(Long quizAttemptId);

    /**
     * 사용자가 퀴즈 전체를 제출하고 최종 결과를 계산 및 반환합니다.
     * 요청에 포함된 모든 답변을 처리하고, 퀴즈 시도를 완료 상태로 변경합니다.
     * 점수 계산, 퀴즈 통계 업데이트, 경험치 부여 등의 후처리 작업을 수행합니다.
     *
     * @param quizId  제출 대상 퀴즈의 ID
     * @param userId  퀴즈를 제출하는 사용자의 ID
     * @param request 사용자의 답변, 소요 시간 등이 포함된 제출 요청 객체
     * @return 퀴즈 최종 결과 정보를 담은 QuizResultResponse DTO
     * @throws BusinessException 퀴즈 시도 조회 실패, 권한 없음, ID 불일치, 이미 완료, 시간 초과 등 다양한 오류 발생 가능
     */
    QuizResultResponse submitQuiz(Long quizId, Long userId, QuizSubmitRequest request);

    /**
     * 특정 퀴즈 시도의 최종 결과 요약을 조회합니다.
     * 이미 완료된 퀴즈 시도에 대해서만 조회가 가능합니다.
     * 이 메서드는 경험치를 다시 계산하지 않으며, 저장된 결과만 반환합니다.
     *
     * @param quizId    조회 대상 퀴즈의 ID
     * @param attemptId 조회할 퀴즈 시도의 ID
     * @param userId    결과를 조회하는 사용자의 ID (권한 확인용)
     * @return 퀴즈 최종 결과 정보를 담은 QuizResultResponse DTO (experienceGained는 0)
     * @throws BusinessException 퀴즈 시도 조회 실패, 권한 없음, ID 불일치, 미완료 상태 등 오류 발생 가능
     */
    QuizResultResponse getQuizResult(Long quizId, Long attemptId, Long userId);
}