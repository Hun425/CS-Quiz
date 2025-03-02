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
     * 퀴즈 시도 시작
     */
    public QuizAttempt startQuiz(Long quizId, User user) {
        Quiz quiz = quizRepository.findByIdWithAllDetails(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        // 이미 완료한 퀴즈인지 확인 (DAILY 퀴즈의 경우)
        if (quiz.getQuizType() == QuizType.DAILY &&
                quizAttemptRepository.hasCompletedQuiz(user, quiz)) {
            throw new BusinessException(ErrorCode.QUIZ_ALREADY_COMPLETED);
        }

        return quizAttemptRepository.save(QuizAttempt.builder()
                .user(user)
                .quiz(quiz)
                .build());
    }

    /**
     * 개별 문제 답변 제출
     */
    public QuestionAttempt submitAnswer(Long quizAttemptId, Long questionId, String answer) {
        QuizAttempt quizAttempt = quizAttemptRepository.findById(quizAttemptId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        if (quizAttempt.isCompleted()) {
            throw new BusinessException(ErrorCode.QUIZ_ALREADY_COMPLETED);
        }

        if (quizAttempt.isTimeExpired()) {
            throw new BusinessException(ErrorCode.QUIZ_TIME_EXPIRED);
        }

        Question question = quizAttempt.getQuiz().getQuestions().stream()
                .filter(q -> q.getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        QuestionAttempt questionAttempt = quizAttempt.addQuestionAttempt(question, answer);
        return questionAttemptRepository.save(questionAttempt);
    }

    /**
     * 퀴즈 완료 처리
     */
    public QuizAttempt completeQuiz(Long quizAttemptId) {
        QuizAttempt quizAttempt = quizAttemptRepository.findById(quizAttemptId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        if (quizAttempt.isCompleted()) {
            throw new BusinessException(ErrorCode.QUIZ_ALREADY_COMPLETED);
        }

        quizAttempt.complete();
        return quizAttemptRepository.save(quizAttempt);
    }

    /**
     * 퀴즈 결과 조회
     */
    public List<QuestionAttemptDto> getQuizResults(Long quizAttemptId) {
        QuizAttempt quizAttempt = quizAttemptRepository.findById(quizAttemptId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        if (!quizAttempt.isCompleted()) {
            throw new BusinessException(ErrorCode.BATTLE_NOT_STARTED, "퀴즈가 완료되지 않았습니다.");
        }

        // 질문과 답변 관계 초기화
        quizAttempt.getQuestionAttempts().forEach(attempt -> {
            attempt.getQuestion().getQuestionText();
            attempt.getQuestion().getExplanation();
        });

        return entityMapperService.mapToQuestionAttemptDtoList(quizAttempt.getQuestionAttempts());
    }

    /**
     * 퀴즈 제출 및 결과 계산
     */
    public QuizResultResponse submitQuiz(Long quizId, Long userId, QuizSubmitRequest request) {
        // 퀴즈 시도 조회
        QuizAttempt quizAttempt = quizAttemptRepository.findById(request.getQuizAttemptId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "퀴즈 시도를 찾을 수 없습니다."));

        // 권한 확인
        if (!quizAttempt.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "접근 권한이 없습니다.");
        }

        // 퀴즈 ID 일치 확인
        if (!quizAttempt.getQuiz().getId().equals(quizId)) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "퀴즈 ID가 일치하지 않습니다.");
        }

        // 이미 완료된 시도인지 확인
        if (quizAttempt.isCompleted()) {
            throw new BusinessException(ErrorCode.QUIZ_ALREADY_COMPLETED, "이미 완료된 퀴즈입니다.");
        }

        // 제한 시간 초과 확인
        if (quizAttempt.isTimeExpired()) {
            throw new BusinessException(ErrorCode.QUIZ_TIME_EXPIRED, "퀴즈 시간이 만료되었습니다.");
        }

        // 각 문제별 답변 처리
        Map<Long, String> answers = request.getAnswers();
        Quiz quiz = quizAttempt.getQuiz();

        quiz.getQuestions().forEach(question -> {
            String answer = answers.getOrDefault(question.getId(), "");
            // 이미 제출된 답변이 있는지 확인
            boolean alreadySubmitted = quizAttempt.getQuestionAttempts().stream()
                    .anyMatch(qa -> qa.getQuestion().getId().equals(question.getId()));

            if (!alreadySubmitted) {
                quizAttempt.addQuestionAttempt(question, answer);
            }
        });

        // 소요 시간 설정
        if (request.getTimeTaken() != null) {
            quizAttempt.setTimeTaken(request.getTimeTaken());
        }

        // 퀴즈 시도 완료 처리
        quizAttempt.complete();
        quizAttemptRepository.save(quizAttempt);

        // 퀴즈 통계 업데이트
        quiz.recordAttempt(quizAttempt.getScore());
        quizRepository.save(quiz);

        // 경험치 처리
        User user = quizAttempt.getUser();
        int experienceBefore = user.getExperience();
        levelingService.calculateQuizExp(quizAttempt);
        int experienceAfter = user.getExperience();
        int experienceGained = experienceAfter - experienceBefore;

        // 결과 응답 생성
        return entityMapperService.mapToQuizResultResponse(quizAttempt, experienceGained);
    }

    /**
     * 퀴즈 결과 조회
     */
    public QuizResultResponse getQuizResult(Long quizId, Long attemptId, Long userId) {
        // 퀴즈 시도 조회
        QuizAttempt quizAttempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "퀴즈 시도를 찾을 수 없습니다."));

        // 권한 확인
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

        // 경험치 정보는 더 이상 계산할 수 없으므로 0으로 설정
        return entityMapperService.mapToQuizResultResponse(quizAttempt, 0);
    }
}