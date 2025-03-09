package com.quizplatform.core.repository.quiz;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.quizplatform.core.domain.quiz.DifficultyLevel;
import com.quizplatform.core.domain.quiz.QQuiz;
import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.tag.QTag;
import com.quizplatform.core.domain.tag.Tag;
import com.quizplatform.core.repository.tag.TagRepository;
import com.quizplatform.core.service.quiz.QuizSearchCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// 커스텀 레포지토리 구현
@RequiredArgsConstructor
@Slf4j
public class CustomQuizRepositoryImpl implements CustomQuizRepository {
    private final JPAQueryFactory queryFactory;
    private final TagRepository tagRepository;

    @Override
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

        // 태그 필터 (계층 구조 고려하여 수정)
        if (condition.getTagIds() != null && !condition.getTagIds().isEmpty()) {
            // 복수 태그 검색 개선
            handleMultipleTagSearch(builder, quiz, condition.getTagIds());
        }

        // 문제 수 범위 필터
        if (condition.getMinQuestions() != null) {
            builder.and(quiz.questionCount.goe(condition.getMinQuestions()));
        }
        if (condition.getMaxQuestions() != null) {
            builder.and(quiz.questionCount.loe(condition.getMaxQuestions()));
        }

        // 디버깅 로깅 추가
        log.debug("검색 조건: {}", condition.toString());
        log.debug("생성된 쿼리 조건: {}", builder.toString());

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

        log.debug("검색 결과 수: {}", content.size());

        return new PageImpl<>(content, pageable, total);
    }

    // 복수 태그 검색을 처리하는 새로운 메서드
    private void handleMultipleTagSearch(BooleanBuilder builder, QQuiz quiz, List<Long> tagIds) {
        QTag tag = QTag.tag;

        if (tagIds.size() == 1) {
            // 단일 태그 검색: 기존 방식 유지 (태그와 그 하위 태그 포함)
            Set<Long> expandedIds = expandTagIds(tagIds);
            builder.and(quiz.tags.any().id.in(expandedIds));
        } else {
            // 복수 태그 검색: 모든 태그를 포함하는 퀴즈만 선택
            for (Long tagId : tagIds) {
                // 각 태그 ID와 하위 태그 ID 확장
                Set<Long> expandedIds = expandSingleTagId(tagId);

                // 이 태그(또는 하위 태그) 중 하나를 포함해야 함
                BooleanExpression tagCondition = quiz.tags.any().id.in(expandedIds);

                // 각 태그에 대한 조건을 AND로 연결
                builder.and(tagCondition);
            }
        }
    }

    private Set<Long> expandTagIds(List<Long> tagIds) {
        Set<Long> expandedIds = new HashSet<>(tagIds);

        // 각 태그 ID에 대해 하위 태그 추가
        for (Long tagId : tagIds) {
            expandedIds.addAll(expandSingleTagId(tagId));
        }

        log.debug("태그 ID 확장: {} -> {}", tagIds, expandedIds);
        return expandedIds;
    }

    // 단일 태그 ID에 대한 확장 (이 태그와 모든 하위 태그)
    private Set<Long> expandSingleTagId(Long parentId) {
        Set<Long> expandedIds = new HashSet<>();
        expandedIds.add(parentId); // 현재 태그 ID 추가

        // 직접 쿼리로 하위 태그 조회
        List<Tag> childTags = tagRepository.findByParentId(parentId);
        for (Tag childTag : childTags) {
            expandedIds.add(childTag.getId());
            // 재귀적으로 더 깊은 수준의 하위 태그도 추가
            expandedIds.addAll(findAllChildTagIds(childTag.getId()));
        }

        return expandedIds;
    }

    // 재귀적으로 모든 하위 태그 ID 조회 메서드
    private Set<Long> findAllChildTagIds(Long parentId) {
        Set<Long> childIds = new HashSet<>();
        List<Tag> childTags = tagRepository.findByParentId(parentId);

        for (Tag childTag : childTags) {
            childIds.add(childTag.getId());
            // 재귀 호출: 더 깊은 레벨의 하위 태그도 포함
            childIds.addAll(findAllChildTagIds(childTag.getId()));
        }

        return childIds;
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