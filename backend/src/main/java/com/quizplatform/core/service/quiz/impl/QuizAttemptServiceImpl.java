package com.quizplatform.core.service.quiz.impl;

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
import com.quizplatform.core.service.quiz.QuizAttemptService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * QuizAttemptService 인터페이스의 구현체
 * 퀴즈 시도(QuizAttempt)와 관련된 비즈니스 로직을 처리합니다.
 *
 * @author 채기훈
 */
@Service
@RequiredArgsConstructor
@Transactional
public class QuizAttemptServiceImpl implements QuizAttemptService {
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuestionAttemptRepository questionAttemptRepository;
    private final QuizRepository quizRepository;
    private final LevelingService levelingService;
    private final EntityMapperService entityMapperService;

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

        // 퀴즈 자체의 통계 업데이트 (평균 점수, 시도 횟수 등) - 원자적 업데이트 사용
        int updatedRows = quizRepository.updateQuizStatsAtomic(quiz.getId(), quizAttempt.getScore());
        if (updatedRows == 0) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "퀴즈 통계 업데이트에 실패했습니다.");
        }

        // 레벨링 서비스 호출하여 경험치 계산 및 부여, 레벨/업적 처리
        User user = quizAttempt.getUser();
        int experienceBefore = user.getExperience(); // 경험치 부여 전 경험치 기록
        levelingService.calculateQuizExp(quizAttempt); // 경험치 계산 및 사용자 레벨 업데이트
        int experienceAfter = user.getExperience(); // 경험치 부여 후 경험치 기록
        int experienceGained = experienceAfter - experienceBefore; // 실제 획득 경험치 계산

        // 최종 결과 응답 생성 (EntityMapperService 사용)
        return entityMapperService.mapToQuizResultResponse(quizAttempt, experienceGained);
    }

    @Override
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