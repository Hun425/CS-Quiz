package com.quizplatform.modules.quiz.infrastructure.adapter;

import com.quizplatform.modules.quiz.domain.entity.QuizAttempt; // Assuming QuizAttempt is in this module's domain
import com.quizplatform.modules.quiz.infrastructure.repository.QuizAttemptRepository;
import com.quizplatform.modules.user.application.port.out.QuizAttemptSummary;
import com.quizplatform.modules.user.application.port.out.UserRecentActivityPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserActivityAdapter implements UserRecentActivityPort {

    private final QuizAttemptRepository quizAttemptRepository;

    @Override
    public List<QuizAttemptSummary> findRecentQuizAttempts(Long userId, Pageable pageable) {
        // Fetch QuizAttempt entities using the repository
        List<QuizAttempt> attempts = quizAttemptRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        // Map QuizAttempt entities to QuizAttemptSummary DTOs
        return attempts.stream()
                .map(attempt -> new QuizAttemptSummary(
                        attempt.getId(),
                        attempt.getQuiz().getId(),       // Requires Quiz entity access
                        attempt.getQuiz().getTitle(),    // Requires Quiz entity access
                        attempt.getScore(),
                        attempt.getCreatedAt() != null ? attempt.getCreatedAt().toLocalDateTime() : null // Convert ZonedDateTime if needed
                 ))
                .collect(Collectors.toList());
    }
} 