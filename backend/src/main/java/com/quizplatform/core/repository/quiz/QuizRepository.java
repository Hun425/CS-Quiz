package com.quizplatform.core.repository.quiz;

import com.quizplatform.core.domain.quiz.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuizRepository extends JpaRepository<Quiz, UUID> {
    Quiz findById()
}
