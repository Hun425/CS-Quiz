package com.quizplatform.core.repository.quiz;

import com.quizplatform.core.domain.quiz.DifficultyLevel;
import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.tag.Tag;
import com.quizplatform.core.service.quiz.QuizSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

// 커스텀 레포지토리 인터페이스
public interface CustomQuizRepository {
    Page<Quiz> search(QuizSearchCondition condition, Pageable pageable);
    List<Quiz> findRecommendedQuizzes(Set<Tag> tags, DifficultyLevel difficulty, int limit);
}