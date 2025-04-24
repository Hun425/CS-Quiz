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

import java.util.*;
import java.util.stream.Collectors;

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
        condition.validate();

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
            handleTagSearch(builder, quiz, condition.getTagIds());
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
        Long total = queryFactory
                .select(quiz.countDistinct())
                .from(quiz)
                .leftJoin(quiz.tags, tag)
                .where(builder)
                .fetchOne();

        // 결과 추출
        List<Quiz> content = query.fetch();

        log.debug("검색 결과 수: {}", content.size());

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    // 태그 검색 처리를 위한 개선된 메서드 (OR 조건으로 변경)
    private void handleTagSearch(BooleanBuilder builder, QQuiz quiz, List<Long> tagIds) {
        QTag tag = QTag.tag;

        if (tagIds.size() == 1) {
            // 단일 태그 검색: 기존 방식 (태그 ID와 그 하위 태그 포함)
            Set<Long> expandedIds = expandTagId(tagIds.get(0));
            builder.and(quiz.tags.any().id.in(expandedIds));
            log.debug("단일 태그 검색: 태그 ID {} 및 하위 태그 검색", tagIds.get(0));
        } else {
            // 복수 태그 검색: 태그 중 하나라도 있는 퀴즈 선택 (OR 조건으로 변경)
            BooleanBuilder tagCondition = new BooleanBuilder();
            
            for (Long tagId : tagIds) {
                // 각 태그 ID와 하위 태그를 확장
                Set<Long> expandedIds = expandTagId(tagId);
                
                // OR 조건으로 추가
                tagCondition.or(quiz.tags.any().id.in(expandedIds));
                log.debug("다중 태그 검색(OR): 태그 ID {} 및 하위 태그 조건 추가", tagId);
            }
            
            // 최종 태그 조건을 쿼리에 AND로 추가
            builder.and(tagCondition);
            log.debug("다중 태그 검색: OR 조건으로 검색 (적어도 하나의 태그 포함)");
        }
    }

    // 단일 태그 ID에 대한 확장 (이 태그와 모든 하위 태그)
    private Set<Long> expandTagId(Long tagId) {
        log.debug("태그 ID 확장 시작: {}", tagId);
        Set<Long> expandedIds = new HashSet<>();
        expandedIds.add(tagId); // 현재 태그 ID 추가

        try {
            // 해당 태그의 모든 하위 태그를 가져옴
            List<Tag> childTags = tagRepository.findTagAndAllDescendants(tagId);
            for (Tag childTag : childTags) {
                if (!childTag.getId().equals(tagId)) { // 현재 태그가 아닌 경우만 추가
                    expandedIds.add(childTag.getId());
                }
            }
        } catch (Exception e) {
            log.error("태그 ID 확장 중 오류 발생: {}", e.getMessage());
        }

        log.debug("태그 ID {} 확장 결과: {}", tagId, expandedIds);
        return expandedIds;
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