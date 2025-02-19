package com.quizplatform.core.service.quiz;

import com.quizplatform.core.domain.question.Question;
import com.quizplatform.core.domain.question.QuestionAttempt;
import com.quizplatform.core.domain.quiz.*;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.dto.question.QuestionAttemptDto;
import com.quizplatform.core.exception.BusinessException;
import com.quizplatform.core.exception.ErrorCode;
import com.quizplatform.core.repository.question.QuestionAttemptRepository;
import com.quizplatform.core.repository.quiz.QuizAttemptRepository;
import com.quizplatform.core.repository.quiz.QuizRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// Service implementation
@Service
@RequiredArgsConstructor
@Transactional
public class QuizAttemptService {
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuestionAttemptRepository questionAttemptRepository;
    private final QuizRepository quizRepository;

    public QuizAttempt startQuiz(UUID quizId, User user) {
        Quiz quiz = quizRepository.findById(quizId)
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

    public QuestionAttempt submitAnswer(UUID quizAttemptId, UUID questionId, String answer) {
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

    public QuizAttempt completeQuiz(UUID quizAttemptId) {
        QuizAttempt quizAttempt = quizAttemptRepository.findById(quizAttemptId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        if (quizAttempt.isCompleted()) {
            throw new BusinessException(ErrorCode.QUIZ_ALREADY_COMPLETED);
        }

        quizAttempt.complete();
        return quizAttemptRepository.save(quizAttempt);
    }

    public List<QuestionAttemptDto> getQuizResults(UUID quizAttemptId) {
        QuizAttempt quizAttempt = quizAttemptRepository.findById(quizAttemptId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        if (!quizAttempt.isCompleted()) {
            throw new BusinessException(ErrorCode.BATTLE_NOT_STARTED, "퀴즈가 완료되지 않았습니다.");
        }

        return quizAttempt.getQuestionAttempts().stream()
                .map(QuestionAttemptDto::from)
                .collect(Collectors.toList());
    }
}
