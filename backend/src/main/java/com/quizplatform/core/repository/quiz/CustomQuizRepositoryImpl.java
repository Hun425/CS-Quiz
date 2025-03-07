package com.quizplatform.core.repository.quiz;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
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
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

// 커스텀 레포지토리 구현
@RequiredArgsConstructor
public class CustomQuizRepositoryImpl implements CustomQuizRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    // search 메서드에서 태그 필터 처리 부분 수정
    public Page<Quiz> search(QuizSearchCondition condition, Pageable pageable) {
        QQuiz quiz = QQuiz.quiz;
        QTag tag = QTag.tag;

        // 조건 유효성 검사 추가
        condition.validate(); // 이 라인 추가

        // 동적 쿼리 생성
        BooleanBuilder builder = new BooleanBuilder();

        // 기본 조건: 공개된 퀴즈만
        builder.and(quiz.isPublic.isTrue());

        // 제목 검색
        if (StringUtils.hasText(condition.getTitle())) {
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
        if (condition.getTagIds() != null && !condition.getTagIds().isEmpty()) {
            builder.and(quiz.tags.any().id.in(condition.getTagIds()));
        }

        // 문제 수 범위 필터
        if (condition.getMinQuestions() != null) {
            builder.and(quiz.questionCount.goe(condition.getMinQuestions()));
        }
        if (condition.getMaxQuestions() != null) {
            builder.and(quiz.questionCount.loe(condition.getMaxQuestions()));
        }

        // 쿼리 실행 최적화: fetchJoin 추가
        JPAQuery<Quiz> query = queryFactory
                .selectFrom(quiz)
                .leftJoin(quiz.tags, tag).fetchJoin()
                .leftJoin(quiz.creator).fetchJoin()
                .where(builder)
                .orderBy(getOrderSpecifier(condition.getOrderBy()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .distinct();

        // 전체 카운트 쿼리
        long total = queryFactory
                .select(quiz.countDistinct())
                .from(quiz)
                .leftJoin(quiz.tags, tag)
                .where(builder)
                .fetchOne();

        // 결과 추출
        List<Quiz> content = query.fetch();

        return new PageImpl<>(content, pageable, total);
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