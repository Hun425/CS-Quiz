package com.quizplatform.quiz.domain.service;

import com.quizplatform.common.exception.BusinessException;
import com.quizplatform.common.exception.ErrorCode;
import com.quizplatform.quiz.adapter.out.persistence.repository.QuizAttemptRepository;
import com.quizplatform.quiz.adapter.out.persistence.repository.QuizRepository;
import com.quizplatform.quiz.application.dto.*;
import com.quizplatform.quiz.domain.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 퀴즈 시도 서비스 구현
 * 
 * <p>퀴즈 시도 생성, 답변 제출, 완료 처리 등의 비즈니스 로직을 구현합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class QuizAttemptServiceImpl implements QuizAttemptService {
    
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizRepository quizRepository;
    private final QuizResultProcessor quizResultProcessor;
    
    @Override
    @Transactional
    public QuizAttemptResponse startQuizAttempt(QuizAttemptRequest request, Long userId) {
        log.info("Starting quiz attempt for quiz: {} by user: {}", request.quizId(), userId);
        
        // 퀴즈 존재 확인
        Quiz quiz = quizRepository.findById(request.quizId())
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND, "퀴즈를 찾을 수 없습니다."));
        
        // 퀴즈 활성화 및 공개 상태 확인
        if (!quiz.isActive() || !quiz.isPublished()) {
            throw new BusinessException(ErrorCode.QUIZ_NOT_AVAILABLE, "현재 이용할 수 없는 퀴즈입니다.");
        }
        
        // 퀴즈 만료 확인 (데일리 퀴즈 등)
        if (quiz.isExpired()) {
            throw new BusinessException(ErrorCode.QUIZ_EXPIRED, "만료된 퀴즈입니다.");
        }
        
        // 기존 진행 중인 시도가 있는지 확인
        Optional<QuizAttempt> existingAttempt = quizAttemptRepository
                .findByUserIdAndQuizIdAndCompleted(userId, request.quizId(), false);
        
        if (existingAttempt.isPresent()) {
            log.info("Returning existing quiz attempt: {}", existingAttempt.get().getId());
            return QuizAttemptResponse.from(existingAttempt.get());
        }
        
        // 새로운 퀴즈 시도 생성
        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .userId(userId)
                .startTime(LocalDateTime.now())
                .completed(false)
                .passed(false)
                .score(0)
                .totalQuestions(quiz.getQuestionCount())
                .build();
        
        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);
        
        log.info("Quiz attempt started successfully: {}", savedAttempt.getId());
        return QuizAttemptResponse.from(savedAttempt);
    }
    
    @Override
    @Transactional
    public QuizAttemptResponse submitQuizAttempt(QuizSubmitRequest request, Long userId) {
        log.info("Submitting quiz attempt: {} by user: {}", request.attemptId(), userId);
        
        // 퀴즈 시도 조회
        QuizAttempt attempt = quizAttemptRepository.findById(request.attemptId())
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_ATTEMPT_NOT_FOUND, "퀴즈 시도를 찾을 수 없습니다."));
        
        // 소유자 확인
        if (!attempt.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "접근 권한이 없습니다.");
        }
        
        // 이미 완료된 시도인지 확인
        if (attempt.isCompleted()) {
            throw new BusinessException(ErrorCode.QUIZ_ALREADY_COMPLETED, "이미 완료된 퀴즈입니다.");
        }
        
        // 답변들을 QuestionAttempt로 변환하여 추가
        for (QuestionAttemptRequest answerRequest : request.answers()) {
            // 문제 존재 확인
            Question question = attempt.getQuiz().getQuestions().stream()
                    .filter(q -> q.getId().equals(answerRequest.questionId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND, 
                            "문제를 찾을 수 없습니다: " + answerRequest.questionId()));
            
            // 문제 시도 생성
            QuestionAttempt questionAttempt = QuestionAttempt.builder()
                    .questionId(answerRequest.questionId())
                    .userAnswer(answerRequest.userAnswer())
                    .attemptTime(LocalDateTime.now())
                    .timeSpentSeconds(answerRequest.timeSpentSeconds())
                    .build();
            
            // 정답 확인
            questionAttempt.checkAnswer(question.getCorrectAnswer());
            
            // 퀴즈 시도에 추가
            attempt.addQuestionAttempt(questionAttempt);
        }
        
        // 퀴즈 완료 처리 (점수 계산, 통과 여부 결정)
        QuizAttempt completedAttempt = quizResultProcessor.processQuizCompletion(attempt);
        
        log.info("Quiz attempt completed successfully: {} with score: {}/{}", 
                completedAttempt.getId(), completedAttempt.getScore(), completedAttempt.getTotalQuestions());
        
        return QuizAttemptResponse.from(completedAttempt);
    }
    
    @Override
    public List<QuizAttemptResponse> getUserQuizAttempts(Long userId) {
        log.debug("Getting quiz attempts for user: {}", userId);
        
        List<QuizAttempt> attempts = quizAttemptRepository.findByUserId(userId);
        return attempts.stream()
                .map(QuizAttemptResponse::fromWithoutQuestions)
                .toList();
    }
    
    @Override
    public List<QuizAttemptResponse> getQuizAttempts(Long quizId) {
        log.debug("Getting attempts for quiz: {}", quizId);
        
        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizId(quizId);
        return attempts.stream()
                .map(QuizAttemptResponse::fromWithoutQuestions)
                .toList();
    }
    
    @Override
    public QuizAttemptResponse getQuizAttempt(Long attemptId, Long userId) {
        log.debug("Getting quiz attempt: {} for user: {}", attemptId, userId);
        
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_ATTEMPT_NOT_FOUND, "퀴즈 시도를 찾을 수 없습니다."));
        
        // 소유자 확인
        if (!attempt.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "접근 권한이 없습니다.");
        }
        
        return QuizAttemptResponse.from(attempt);
    }
    
    @Override
    public QuizAttemptResponse getLatestQuizAttempt(Long quizId, Long userId) {
        log.debug("Getting latest quiz attempt for quiz: {} by user: {}", quizId, userId);
        
        Optional<QuizAttempt> attempt = quizAttemptRepository
                .findTopByUserIdAndQuizIdOrderByStartTimeDesc(userId, quizId);
        
        if (attempt.isEmpty()) {
            throw new BusinessException(ErrorCode.QUIZ_ATTEMPT_NOT_FOUND, "퀴즈 시도를 찾을 수 없습니다.");
        }
        
        return QuizAttemptResponse.from(attempt.get());
    }
}