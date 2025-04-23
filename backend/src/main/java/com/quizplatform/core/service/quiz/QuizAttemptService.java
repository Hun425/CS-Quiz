package com.quizplatform.core.service.quiz;

import com.quizplatform.core.domain.question.Question;
import com.quizplatform.core.domain.question.QuestionAttempt;
import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.quiz.QuizAttempt;
import com.quizplatform.core.domain.quiz.QuizType;
import com.quizplatform.modules.user.domain.entity.User;
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

import java.util.List;
import java.util.Map;

/**
 * 퀴즈 시도(QuizAttempt)와 관련된 비즈니스 로직을 처리하는 서비스입니다.
 * 퀴즈 시도 시작, 개별 문제 답변 제출, 퀴즈 완료, 결과 조회 및 최종 제출 처리 등을 담당합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Service
@RequiredArgsConstructor
@Transactional
public class QuizAttemptService {
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuestionAttemptRepository questionAttemptRepository;
    private final QuizRepository quizRepository;
    private final LevelingService levelingService;
    private final EntityMapperService entityMapperService;

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
    public QuizAttempt startQuiz(Long quizId, User user) {
        Quiz quiz = quizRepository.findByIdWithAllDetails(quizId) // 퀴즈 정보와 연관된 모든 상세 정보 로드
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        // 데일리 퀴즈의 경우, 해당 사용자가 이 퀴즈를 이미 완료했는지 확인
        if (quiz.getQuizType() == QuizType.DAILY &&
                quizAttemptRepository.hasCompletedQuiz(user, quiz)) {
            throw new BusinessException(ErrorCode.QUIZ_ALREADY_COMPLETED, "이미 완료한 데일리 퀴즈입니다.");
        }

        // 새로운 퀴즈 시도 객체 생성 및 저장
        return quizAttemptRepository.save(QuizAttempt.builder()
                .user(user)
                .quiz(quiz)
                .build());
    }

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
    public QuestionAttempt submitAnswer(Long quizAttemptId, Long questionId, String answer) {
        // 퀴즈 시도 조회
        QuizAttempt quizAttempt = quizAttemptRepository.findById(quizAttemptId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "퀴즈 시도를 찾을 수 없습니다."));

        // 이미 완료된 퀴즈인지 확인
        if (quizAttempt.isCompleted()) {
            throw new BusinessException(ErrorCode.QUIZ_ALREADY_COMPLETED, "이미 완료된 퀴즈입니다.");
        }

        // 시간 제한 초과 여부 확인
        if (quizAttempt.isTimeExpired()) {
            throw new BusinessException(ErrorCode.QUIZ_TIME_EXPIRED, "퀴즈 시간이 만료되었습니다.");
        }

        // 퀴즈 내에서 해당 문제 ID를 가진 Question 객체 찾기
        Question question = quizAttempt.getQuiz().getQuestions().stream()
                .filter(q -> q.getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "해당 문제를 찾을 수 없습니다."));

        // QuizAttempt에 문제 답변 정보(QuestionAttempt) 추가 및 저장
        QuestionAttempt questionAttempt = quizAttempt.addQuestionAttempt(question, answer);
        return questionAttemptRepository.save(questionAttempt);
    }

    /**
     * 특정 퀴즈 시도를 완료 상태로 변경합니다.
     * 이미 완료된 퀴즈 시도에 대해서는 오류를 발생시킵니다.
     *
     * @param quizAttemptId 완료 처리할 퀴즈 시도의 ID
     * @return 완료 처리된 QuizAttempt 객체
     * @throws BusinessException 퀴즈 시도를 찾을 수 없거나 (ENTITY_NOT_FOUND),
     * 이미 완료된 퀴즈일 경우 (QUIZ_ALREADY_COMPLETED)
     */
    public QuizAttempt completeQuiz(Long quizAttemptId) {
        QuizAttempt quizAttempt = quizAttemptRepository.findById(quizAttemptId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "퀴즈 시도를 찾을 수 없습니다."));

        // 이미 완료된 퀴즈인지 확인
        if (quizAttempt.isCompleted()) {
            throw new BusinessException(ErrorCode.QUIZ_ALREADY_COMPLETED, "이미 완료된 퀴즈입니다.");
        }

        // 퀴즈 시도 완료 처리 (내부적으로 상태 변경 및 완료 시간 설정)
        quizAttempt.complete();
        return quizAttemptRepository.save(quizAttempt);
    }

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
    public List<QuestionAttemptDto> getQuizResults(Long quizAttemptId) {
        QuizAttempt quizAttempt = quizAttemptRepository.findById(quizAttemptId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "퀴즈 시도를 찾을 수 없습니다."));

        // 퀴즈 완료 여부 확인
        if (!quizAttempt.isCompleted()) {
            // TODO: ErrorCode.QUIZ_NOT_COMPLETED 와 같은 더 적절한 에러 코드로 변경 고려
            throw new BusinessException(ErrorCode.BATTLE_NOT_STARTED, "퀴즈가 완료되지 않았습니다.");
        }

        // DTO 변환 전, QuestionAttempt와 연관된 Question 엔티티의 필요 정보 초기화 (Lazy Loading)
        quizAttempt.getQuestionAttempts().forEach(attempt -> {
            attempt.getQuestion().getQuestionText(); // 질문 텍스트 로딩
            attempt.getQuestion().getExplanation(); // 해설 로딩
        });

        // EntityMapperService를 통해 DTO 리스트로 변환
        return entityMapperService.mapToQuestionAttemptDtoList(quizAttempt.getQuestionAttempts());
    }

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
    public QuizResultResponse submitQuiz(Long quizId, Long userId, QuizSubmitRequest request) {
        // 요청된 quizAttemptId로 퀴즈 시도 조회
        QuizAttempt quizAttempt = quizAttemptRepository.findById(request.getQuizAttemptId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "퀴즈 시도를 찾을 수 없습니다."));

        // 퀴즈 시도 소유자(사용자)가 요청한 사용자와 일치하는지 확인
        if (!quizAttempt.getUser().getId().equals(userId)) {
            // TODO: ErrorCode.FORBIDDEN 또는 ACCESS_DENIED 와 같은 권한 관련 에러 코드로 변경 고려
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "접근 권한이 없습니다.");
        }

        // 요청된 quizId와 실제 퀴즈 시도의 퀴즈 ID가 일치하는지 확인
        if (!quizAttempt.getQuiz().getId().equals(quizId)) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "퀴즈 ID가 일치하지 않습니다.");
        }

        // 이미 완료된 시도인지 확인
        if (quizAttempt.isCompleted()) {
            throw new BusinessException(ErrorCode.QUIZ_ALREADY_COMPLETED, "이미 완료된 퀴즈입니다.");
        }

        // 시간 제한 초과 확인
        if (quizAttempt.isTimeExpired()) {
            throw new BusinessException(ErrorCode.QUIZ_TIME_EXPIRED, "퀴즈 시간이 만료되었습니다.");
        }

        // 요청에 포함된 답변들을 처리 (Map<QuestionID, Answer>)
        Map<Long, String> answers = request.getAnswers();
        Quiz quiz = quizAttempt.getQuiz(); // 연관된 퀴즈 정보 로드

        // 퀴즈의 모든 문제에 대해
        quiz.getQuestions().forEach(question -> {
            // 해당 문제에 대한 답변을 요청에서 가져옴 (없으면 빈 문자열)
            String answer = answers.getOrDefault(question.getId(), "");
            // 해당 문제에 대한 답변이 이미 기록되었는지 확인
            boolean alreadySubmitted = quizAttempt.getQuestionAttempts().stream()
                    .anyMatch(qa -> qa.getQuestion().getId().equals(question.getId()));

            // 아직 기록되지 않은 답변만 추가
            if (!alreadySubmitted) {
                quizAttempt.addQuestionAttempt(question, answer);
            }
        });

        // 요청에 소요 시간이 포함되어 있으면 설정
        if (request.getTimeTaken() != null) {
            quizAttempt.setTimeTaken(request.getTimeTaken());
        }

        // 퀴즈 시도 완료 처리 (점수 계산 및 상태 변경)
        quizAttempt.complete();
        quizAttemptRepository.save(quizAttempt); // 변경된 QuizAttempt 저장

        // 퀴즈 자체의 통계 업데이트 (평균 점수, 시도 횟수 등)
        quiz.recordAttempt(quizAttempt.getScore());
        quizRepository.save(quiz); // 변경된 Quiz 정보 저장

        // 레벨링 서비스 호출하여 경험치 계산 및 부여, 레벨/업적 처리
        User user = quizAttempt.getUser();
        int experienceBefore = user.getExperience(); // 경험치 부여 전 경험치 기록
        levelingService.calculateQuizExp(quizAttempt); // 경험치 계산 및 사용자 레벨 업데이트
        int experienceAfter = user.getExperience(); // 경험치 부여 후 경험치 기록
        int experienceGained = experienceAfter - experienceBefore; // 실제 획득 경험치 계산

        // 최종 결과 응답 생성 (EntityMapperService 사용)
        return entityMapperService.mapToQuizResultResponse(quizAttempt, experienceGained);
    }

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
    public QuizResultResponse getQuizResult(Long quizId, Long attemptId, Long userId) {
        // attemptId로 퀴즈 시도 조회
        QuizAttempt quizAttempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "퀴즈 시도를 찾을 수 없습니다."));

        // 퀴즈 시도 소유자 확인
        if (!quizAttempt.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "접근 권한이 없습니다.");
        }

        // 퀴즈 ID 일치 확인
        if (!quizAttempt.getQuiz().getId().equals(quizId)) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "퀴즈 ID가 일치하지 않습니다.");
        }

        // 완료된 시도인지 확인
        if (!quizAttempt.isCompleted()) {
            throw new BusinessException(ErrorCode.BATTLE_NOT_STARTED, "아직 완료되지 않은 퀴즈입니다.");
        }

        // EntityMapperService를 통해 결과 응답 생성 (획득 경험치는 0으로 전달)
        return entityMapperService.mapToQuizResultResponse(quizAttempt, 0);
    }
}