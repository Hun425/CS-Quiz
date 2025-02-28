package com.quizplatform.core.repository.quiz;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.quizplatform.core.domain.quiz.DifficultyLevel;
import com.quizplatform.core.domain.quiz.QQuiz;
import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.tag.QTag;
import com.quizplatform.core.domain.tag.Tag;
import com.quizplatform.core.service.quiz.QuizSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

// 커스텀 레포지토리 구현
@RequiredArgsConstructor
public class CustomQuizRepositoryImpl implements CustomQuizRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Quiz> search(QuizSearchCondition condition, Pageable pageable) {
        QQuiz quiz = QQuiz.quiz;
        QTag tag = QTag.tag;

        // 동적 쿼리 생성
        BooleanBuilder builder = new BooleanBuilder();

        // 기본 조건: 공개된 퀴즈만
        builder.and(quiz.isPublic.isTrue());

        // 제목 검색
        if (condition.getTitle() != null) {
            builder.and(quiz.title.containsIgnoreCase(condition.getTitle()));
        }

        // 난이도 필터
        if (condition.getDifficultyLevel() != null) {
            builder.and(quiz.difficultyLevel.eq(condition.getDifficultyLevel()));
        }

        // 퀴즈 타입 필터
        if (condition.getQuizType() != null) {
            builder.and(quiz.quizType.eq(condition.getQuizType()));
        }

        // 태그 필터
        if (!condition.getTagIds().isEmpty()) {
            builder.and(quiz.tags.any().id.in(condition.getTagIds()));
        }

        // 문제 수 범위 필터
        if (condition.getMinQuestions() != null) {
            builder.and(quiz.questionCount.goe(condition.getMinQuestions()));
        }
        if (condition.getMaxQuestions() != null) {
            builder.and(quiz.questionCount.loe(condition.getMaxQuestions()));
        }

        // 쿼리 실행
        QueryResults<Quiz> results = queryFactory
                .selectFrom(quiz)
                .leftJoin(quiz.tags, tag)
                .where(builder)
                .orderBy(getOrderSpecifier(condition.getOrderBy()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        return new PageImpl<>(results.getResults(), pageable, results.getTotal());
    }


    @Override
    public List<Quiz> findRecommendedQuizzes(Set<Tag> tags, DifficultyLevel difficulty, int limit) {
        QQuiz quiz = QQuiz.quiz;
        QTag tag = QTag.tag;

        // 추천 퀴즈 쿼리 생성
        return queryFactory
                .selectFrom(quiz)
                .leftJoin(quiz.tags, tag)
                .where(
                        quiz.isPublic.isTrue()
                                .and(quiz.difficultyLevel.eq(difficulty))
                                .and(quiz.tags.any().in(tags))
                )
                .orderBy(
                        Expressions.numberTemplate(Double.class, "random()").asc()
                )
                .limit(limit)
                .fetch();
    }

    // 정렬 조건 생성
    private OrderSpecifier<?> getOrderSpecifier(String orderBy) {
        QQuiz quiz = QQuiz.quiz;

        if (orderBy == null) {
            return quiz.createdAt.desc();
        }

        switch (orderBy) {
            case "avgScore":
                return quiz.avgScore.desc();
            case "attemptCount":
                return quiz.attemptCount.desc();
            case "difficulty":
                return quiz.difficultyLevel.asc();
            default:
                return quiz.createdAt.desc();
        }
    }
}