package com.quizplatform.core.repository.quiz;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.quizplatform.core.domain.quiz.DifficultyLevel;
import com.quizplatform.core.domain.quiz.QQuiz; // QueryDSL 생성 Q클래스
import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.tag.QTag; // QueryDSL 생성 Q클래스
import com.quizplatform.core.domain.tag.Tag;
import com.quizplatform.core.domain.user.QUser;
import com.quizplatform.core.dto.quiz.QuizSubmitRequest; // 위치 확인 필요
import com.quizplatform.core.dto.quiz.QuizSummaryResponse;

import com.quizplatform.core.dto.tag.TagDto;
import com.quizplatform.core.dto.tag.TagResponse;
import com.quizplatform.core.repository.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * CustomQuizRepository 인터페이스의 QueryDSL 기반 구현체입니다.
 * 동적 쿼리 생성을 통해 복잡한 퀴즈 검색 및 추천 로직을 처리합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@RequiredArgsConstructor
@Slf4j
public class CustomQuizRepositoryImpl implements CustomQuizRepository {
    private final JPAQueryFactory queryFactory; // QueryDSL 팩토리
    private final TagRepository tagRepository; // 태그 계층 구조 조회를 위해 사용

    /**
     * 다양한 검색 조건(제목, 난이도, 타입, 태그, 문제 수 등)을 조합하여 퀴즈를 검색하고
     * 페이징 처리된 결과를 반환합니다. QueryDSL을 사용하여 동적 쿼리를 생성합니다.
     * 성능 최적화를 위해 연관된 엔티티(태그, 생성자)를 fetch join 합니다.
     *
     * @param condition 검색 조건을 담고 있는 객체
     * @param pageable  페이징 정보 (페이지 번호, 크기, 정렬)
     * @return 검색 조건에 맞는 Quiz 엔티티 페이지 객체
     */
    @Override
    public Page<Quiz> search(QuizSubmitRequest.QuizSearchCondition condition, Pageable pageable) {
        QQuiz quiz = QQuiz.quiz; // Quiz 엔티티에 대한 Q클래스
        QTag tag = QTag.tag;     // Tag 엔티티에 대한 Q클래스

        // 검색 조건 유효성 검사 (QuizSearchCondition 내부에 validate() 메서드 필요)
        condition.validate();

        // QueryDSL의 BooleanBuilder를 사용하여 동적 WHERE 절 구성
        BooleanBuilder builder = createSearchCondition(condition);

        // 디버깅을 위한 로그 출력
        log.debug("Quiz search condition: {}", condition.toString());
        log.debug("Generated query condition: {}", builder.toString());

        // 메인 쿼리 생성 (fetchJoin으로 N+1 문제 방지)
        JPAQuery<Quiz> query = queryFactory
                .selectFrom(quiz)
                .leftJoin(quiz.tags, tag).fetchJoin() // 퀴즈와 태그 조인 (N+1 방지)
                .leftJoin(quiz.creator).fetchJoin()   // 퀴즈와 생성자 조인 (N+1 방지)
                .where(builder) // 동적으로 생성된 WHERE 조건 적용
                .orderBy(getOrderSpecifier(condition.getOrderBy())) // 정렬 조건 적용
                .offset(pageable.getOffset()) // 페이징 offset
                .limit(pageable.getPageSize()) // 페이징 limit
                .distinct(); // fetch join으로 인해 발생할 수 있는 중복 제거

        // 전체 결과 수 계산 쿼리 (페이징 처리를 위해 필요)
        Long total = queryFactory
                .select(quiz.countDistinct()) // 중복 제거된 카운트
                .from(quiz)
                .leftJoin(quiz.tags, tag) // 조건에 태그가 포함될 수 있으므로 조인 필요
                .where(builder) // 동일한 WHERE 조건 적용
                .fetchOne(); // 결과가 하나 또는 null

        // 메인 쿼리 실행 및 결과 가져오기
        List<Quiz> content = query.fetch();

        log.debug("Quiz search result count: {}", content.size());

        // Page 객체 생성하여 반환 (결과 목록, 페이징 정보, 전체 개수)
        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    /**
     * 다양한 검색 조건을 이용하여 퀴즈를 검색하고 페이징 처리된 결과를 DTO로 직접 반환합니다.
     * N+1 문제를 방지하기 위해 DTO 직접 조회 방식을 사용합니다.
     *
     * @param condition 검색 조건을 담고 있는 객체
     * @param pageable  페이징 정보 (페이지 번호, 크기, 정렬 등)
     * @return 검색 조건에 맞는 QuizSummaryResponse DTO 페이지 객체
     */
    @Override
    public Page<QuizSummaryResponse> searchQuizSummaryResponse(QuizSubmitRequest.QuizSearchCondition condition, Pageable pageable) {
        QQuiz quiz = QQuiz.quiz;
        QTag tag = QTag.tag;
        QUser creator = QUser.user;

        // 검색 조건 유효성 검사
        condition.validate();

        // 검색 조건 생성
        BooleanBuilder builder = createSearchCondition(condition);

        // 카운트 쿼리 먼저 실행
        Long total = queryFactory
                .select(quiz.countDistinct())
                .from(quiz)
                .leftJoin(quiz.tags, tag) // Count 쿼리에도 조건 조인이 필요할 수 있음
                .where(builder)
                .fetchOne();

        if (total == null || total == 0) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 1단계: 태그를 제외한 기본 정보 DTO로 직접 조회
        List<QuizSummaryResponse> baseContent = queryFactory
                .select(Projections.constructor(QuizSummaryResponse.class,
                        quiz.id,                  // Long
                        quiz.title,               // String
                        quiz.quizType,            // QuizType
                        quiz.difficultyLevel,     // DifficultyLevel
                        quiz.questionCount,       // int
                        quiz.attemptCount,        // int
                        quiz.avgScore,            // double
                        Expressions.constant(Collections.emptyList()), // List<TagResponse>
                        quiz.createdAt            // LocalDateTime
                ))
                .from(quiz)
                .leftJoin(quiz.creator, creator) // 조인은 필요
                .where(builder)
                .orderBy(getOrderSpecifier(condition.getOrderBy()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2단계: 별도 쿼리로 태그 정보 로드 (Map<QuizId, List<TagResponse>>)
        List<QuizSummaryResponse> finalContent; // 최종 결과를 담을 리스트
        if (!baseContent.isEmpty()) {
            Set<Long> quizIds = baseContent.stream()
                    .map(QuizSummaryResponse::getId)
                    .collect(Collectors.toSet());

            // 수정된 loadQuizTags 호출 (TagResponse 반환)
            Map<Long, List<TagResponse>> quizTagsMap = loadQuizTagsAsResponse(quizIds); // 새 메서드 사용

            // 3단계: 기본 DTO와 태그 정보를 합쳐 최종 DTO 생성
            finalContent = baseContent.stream()
                    .map(dto -> dto.toBuilder() // 기존 DTO 기반으로 빌더 생성
                            .tags(quizTagsMap.getOrDefault(dto.getId(), Collections.emptyList())) // 태그 설정
                            .build()) // 새로운 DTO 객체 생성
                    .collect(Collectors.toList());
        } else {
            finalContent = Collections.emptyList();
        }

        return new PageImpl<>(finalContent, pageable, total);
    }

    /**
     * 퀴즈 ID 목록에 대한 태그 정보를 TagResponse DTO로 조회하여 맵 형태로 반환합니다.
     *
     * @param quizIds 태그 정보를 조회할 퀴즈 ID 목록
     * @return 퀴즈 ID를 키로, TagResponse DTO 목록을 값으로 하는 맵
     */
    private Map<Long, List<TagResponse>> loadQuizTagsAsResponse(Set<Long> quizIds) {
        QQuiz quiz = QQuiz.quiz;
        QTag tag = QTag.tag;

        // Quiz ID와 연관된 Tag 정보 (ID, Name, Description 등 TagResponse 생성에 필요한 필드) 조회
        List<Tuple> results = queryFactory
                .select(quiz.id,
                        tag.id,
                        tag.name,
                        tag.description
                        // TagResponse.from(Tag) 를 사용하려면 Tag 엔티티 전체 또는 필요한 필드 조회
                        // 또는 TagResponse 생성자에 필요한 필드를 직접 select
                )
                .from(quiz)
                .join(quiz.tags, tag)
                .where(quiz.id.in(quizIds))
                .fetch();

        // 결과를 퀴즈 ID 기준으로 매핑 (TagResponse 생성)
        Map<Long, List<TagResponse>> quizTagsMap = new HashMap<>();
        results.forEach(tuple -> {
            Long quizId = tuple.get(quiz.id);
            // TagResponse 생성 (팩토리 메서드나 빌더 사용 가정)
            // 예시: TagResponse.from(Tag) 를 사용 못하므로 필드를 직접 사용
            TagResponse tagResponse = TagResponse.builder()
                    .id(tuple.get(tag.id))
                    .name(tuple.get(tag.name))
                    .description(tuple.get(tag.description))
                    // quizCount는 여기서 알 수 없으므로 0 또는 다른 값으로 설정하거나,
                    // TagResponse 구조 변경 고려
                    .quizCount(0)
                    // parentId, synonyms 등 다른 필드도 필요시 조회 및 설정
                    .parentId(null) // 필요시 부모 태그 ID 조회 로직 추가
                    .synonyms(Collections.emptySet()) // 필요시 동의어 조회 로직 추가
                    .build();

            quizTagsMap.computeIfAbsent(quizId, k -> new ArrayList<>())
                    .add(tagResponse);
        });

        return quizTagsMap;
    }

    /**
     * 주어진 태그 목록과 난이도를 기반으로 추천 퀴즈 목록을 조회합니다.
     * 공개된 퀴즈 중에서 해당 난이도이고 주어진 태그 중 하나라도 포함하는 퀴즈를
     * 랜덤하게 정렬하여 제한된 개수만큼 반환합니다.
     *
     * @param tags       추천 기준이 되는 태그(Tag) 객체 Set
     * @param difficulty 추천 기준이 되는 난이도(DifficultyLevel)
     * @param limit      조회할 최대 퀴즈 개수
     * @return 추천된 Quiz 엔티티 리스트
     */
    @Override
    public List<Quiz> findRecommendedQuizzes(Set<Tag> tags, DifficultyLevel difficulty, int limit) {
        // 입력 검증: limit 값이 유효한 범위인지 확인
        if (limit < 1 || limit > 1000) {
            throw new IllegalArgumentException("Limit must be between 1 and 1000, but was: " + limit);
        }
        
        // 입력 검증: tags가 null이거나 비어있는지 확인
        if (tags == null || tags.isEmpty()) {
            throw new IllegalArgumentException("Tags cannot be null or empty");
        }
        
        // 입력 검증: difficulty가 null인지 확인
        if (difficulty == null) {
            throw new IllegalArgumentException("Difficulty level cannot be null");
        }
        
        QQuiz quiz = QQuiz.quiz;
        QTag tag = QTag.tag; // 사용되지는 않지만 조인을 위해 선언

        // 추천 퀴즈 조회 쿼리
        List<Quiz> quizzes = queryFactory
                .selectFrom(quiz)
                .leftJoin(quiz.tags, tag) // 태그 정보 조인을 위해 필요
                .where(
                        quiz.isPublic.isTrue() // 공개된 퀴즈
                                .and(quiz.difficultyLevel.eq(difficulty)) // 지정된 난이도
                                .and(quiz.tags.any().in(tags)) // 주어진 태그 중 하나라도 포함
                )
                .distinct() // 태그 중복으로 인한 퀴즈 중복 제거
                .fetch();
        
        // 애플리케이션 레벨에서 랜덤하게 섞기 (DB의 ORDER BY random() 대신)
        Collections.shuffle(quizzes);
        
        // 제한된 개수만큼 반환
        return quizzes.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 주어진 태그 목록과 난이도를 기반으로 추천 퀴즈 목록을 DTO로 직접 조회합니다.
     *
     * @param tags       추천 기준이 되는 태그(Tag) 객체 Set
     * @param difficulty 추천 기준이 되는 난이도(DifficultyLevel)
     * @param limit      조회할 최대 퀴즈 개수
     * @return 추천된 QuizSummaryResponse DTO 리스트
     */
    @Override
    public List<QuizSummaryResponse> findRecommendedQuizSummaryResponses(Set<Tag> tags, DifficultyLevel difficulty, int limit) {
        // 입력 검증: limit 값이 유효한 범위인지 확인
        if (limit < 1 || limit > 1000) {
            throw new IllegalArgumentException("Limit must be between 1 and 1000, but was: " + limit);
        }
        
        // 입력 검증: tags가 null이거나 비어있는지 확인
        if (tags == null || tags.isEmpty()) {
            throw new IllegalArgumentException("Tags cannot be null or empty");
        }
        
        // 입력 검증: difficulty가 null인지 확인
        if (difficulty == null) {
            throw new IllegalArgumentException("Difficulty level cannot be null");
        }
        
        QQuiz quiz = QQuiz.quiz;
        QTag tag = QTag.tag;
        QUser creator = QUser.user;

        // 태그 ID 목록 추출
        Set<Long> tagIds = tags.stream()
                .map(Tag::getId)
                .collect(Collectors.toSet());

        // 1단계: 태그를 제외한 기본 정보 DTO로 직접 조회
        // QuizSummaryResponse 생성자에 맞게 필드 순서/타입 조정 및 임시 값 추가
        List<QuizSummaryResponse> baseResult = queryFactory
                .select(Projections.constructor(QuizSummaryResponse.class,
                        quiz.id,
                        quiz.title,
                        quiz.quizType,
                        quiz.difficultyLevel,
                        quiz.questionCount,
                        quiz.attemptCount,
                        quiz.avgScore,
                        Expressions.constant(Collections.emptyList()), // tags 임시 빈 리스트
                        quiz.createdAt
                ))
                .from(quiz)
                // .leftJoin(quiz.creator, creator) // 생성자에 creator 정보 없으므로 조인 불필요할 수 있음 (쿼리 조건에 따라 판단)
                .where(
                        quiz.isPublic.isTrue()
                                .and(quiz.difficultyLevel.eq(difficulty))
                                .and(quiz.id.in(
                                        // 서브쿼리: 태그 중 하나라도 포함하는 퀴즈 ID 목록
                                        queryFactory.select(quiz.id)
                                                .from(quiz)
                                                .join(quiz.tags, tag)
                                                .where(tag.id.in(tagIds))
                                                .groupBy(quiz.id)
                                ))
                )
                .fetch();

        // 애플리케이션 레벨에서 랜덤하게 섞기
        Collections.shuffle(baseResult);

        // 결과 크기 제한
        List<QuizSummaryResponse> limitedResult;
        if (baseResult.size() > limit) {
            limitedResult = baseResult.subList(0, limit);
        } else {
            limitedResult = baseResult;
        }

        // 2단계: 별도 쿼리로 태그 정보 로드 (TagResponse 사용)
        List<QuizSummaryResponse> finalResult; // 최종 결과를 담을 리스트
        if (!limitedResult.isEmpty()) {
            Set<Long> quizIds = limitedResult.stream()
                    .map(QuizSummaryResponse::getId)
                    .collect(Collectors.toSet());

            // 수정된 loadQuizTagsAsResponse 호출
            Map<Long, List<TagResponse>> quizTagsMap = loadQuizTagsAsResponse(quizIds);

            // 3단계: 기본 DTO와 태그 정보를 합쳐 최종 DTO 생성 (toBuilder 사용)
            finalResult = limitedResult.stream()
                    .map(dto -> dto.toBuilder() // 기존 DTO 기반으로 빌더 생성
                            .tags(quizTagsMap.getOrDefault(dto.getId(), Collections.emptyList())) // 태그 설정
                            .build()) // 새로운 DTO 객체 생성
                    .collect(Collectors.toList());
        } else {
            finalResult = Collections.emptyList();
        }

        return finalResult;
    }

// loadQuizTagsAsResponse 메서드는 이전에 정의된 것을 사용합니다.
// private Map<Long, List<TagResponse>> loadQuizTagsAsResponse(Set<Long> quizIds) { ... }
    
    /**
     * 퀴즈 ID 목록에 대한 태그 정보를 조회하여 맵 형태로 반환합니다. (내부 헬퍼 메서드)
     *
     * @param quizIds 태그 정보를 조회할 퀴즈 ID 목록
     * @return 퀴즈 ID를 키로, 태그 DTO 목록을 값으로 하는 맵
     */
    private Map<Long, List<TagDto>> loadQuizTags(Set<Long> quizIds) {
        QQuiz quiz = QQuiz.quiz;
        QTag tag = QTag.tag;
        
        // IN 쿼리 하나로 모든 퀴즈의 태그 정보 로드
        List<Tuple> results = queryFactory
                .select(quiz.id, tag.id, tag.name)
                .from(quiz)
                .join(quiz.tags, tag)
                .where(quiz.id.in(quizIds))
                .fetch();
                
        // 결과를 퀴즈 ID 기준으로 매핑
        Map<Long, List<TagDto>> quizTagsMap = new HashMap<>();
        results.forEach(tuple -> {
            Long quizId = tuple.get(quiz.id);
            Long tagId = tuple.get(tag.id);
            String tagName = tuple.get(tag.name);
            
            quizTagsMap.computeIfAbsent(quizId, k -> new ArrayList<>())
                   .add(new TagDto(tagId, tagName));
        });
        
        return quizTagsMap;
    }

    /**
     * 검색 조건을 BooleanBuilder로 생성합니다. (내부 헬퍼 메서드)
     *
     * @param condition 검색 조건 객체
     * @return 생성된 BooleanBuilder 객체
     */
    private BooleanBuilder createSearchCondition(QuizSubmitRequest.QuizSearchCondition condition) {
        QQuiz quiz = QQuiz.quiz;
        
        // QueryDSL의 BooleanBuilder를 사용하여 동적 WHERE 절 구성
        BooleanBuilder builder = new BooleanBuilder();

        // 기본 조건: 공개된(isPublic=true) 퀴즈만 검색
        builder.and(quiz.isPublic.isTrue());

        // 제목 검색 조건 (null 또는 빈 문자열이 아닐 경우)
        if (StringUtils.hasText(condition.getTitle())) {
            builder.and(quiz.title.containsIgnoreCase(condition.getTitle())); // 대소문자 무시 포함 검색
        }

        // 난이도 필터 조건
        if (condition.getDifficultyLevel() != null) {
            builder.and(quiz.difficultyLevel.eq(condition.getDifficultyLevel()));
        }

        // 퀴즈 타입 필터 조건
        if (condition.getQuizType() != null) {
            builder.and(quiz.quizType.eq(condition.getQuizType()));
        }

        // 태그 필터 조건 (태그 ID 목록이 null이 아니고 비어있지 않을 경우)
        if (condition.getTagIds() != null && !condition.getTagIds().isEmpty()) {
            // 태그 검색 로직 처리 (하위 태그 포함 및 다중 태그 OR 조건)
            handleTagSearch(builder, quiz, condition.getTagIds());
        }

        // 최소 문제 수 필터 조건
        if (condition.getMinQuestions() != null) {
            builder.and(quiz.questionCount.goe(condition.getMinQuestions())); // >=
        }
        // 최대 문제 수 필터 조건
        if (condition.getMaxQuestions() != null) {
            builder.and(quiz.questionCount.loe(condition.getMaxQuestions())); // <=
        }
        
        return builder;
    }

    /**
     * 태그 기반 검색 조건을 QueryDSL BooleanBuilder에 추가합니다. (내부 헬퍼 메서드)
     * 단일 태그 검색 시 해당 태그 및 모든 하위 태그를 포함하여 검색합니다.
     * 복수 태그 검색 시 각 태그(및 하위 태그) 중 하나라도 포함하는 퀴즈를 검색합니다 (OR 조건).
     *
     * @param builder QueryDSL BooleanBuilder 객체
     * @param quiz    QQuiz 객체 (퀴즈 Q클래스)
     * @param tagIds  검색할 태그 ID 목록
     */
    private void handleTagSearch(BooleanBuilder builder, QQuiz quiz, List<Long> tagIds) {
        if (tagIds.size() == 1) {
            // --- 단일 태그 검색 ---
            Long singleTagId = tagIds.get(0);
            // 해당 태그 ID와 그 하위 태그 ID들을 모두 가져옴
            Set<Long> expandedIds = expandTagId(singleTagId);
            // 퀴즈가 가진 태그 중 하나라도 expandedIds에 포함되는지 확인 (AND 조건)
            builder.and(quiz.tags.any().id.in(expandedIds));
            log.debug("Single tag search applied: tagId={} (including descendants)", singleTagId);
        } else {
            // --- 복수 태그 검색 (OR 조건) ---
            BooleanBuilder tagCondition = new BooleanBuilder(); // 태그 조건만을 위한 별도 Builder
            for (Long tagId : tagIds) {
                // 각 태그 ID와 그 하위 태그 ID들을 모두 가져옴
                Set<Long> expandedIds = expandTagId(tagId);
                // 해당 태그(및 하위 태그)를 포함하는 조건을 OR로 추가
                tagCondition.or(quiz.tags.any().id.in(expandedIds));
                log.debug("Multi-tag search condition added (OR): tagId={} (including descendants)", tagId);
            }
            // 생성된 태그 OR 조건을 메인 builder에 AND로 추가
            builder.and(tagCondition);
            log.debug("Multi-tag search applied: searching for quizzes containing at least one of the specified tags (OR condition)");
        }
    }

    /**
     * 주어진 태그 ID에 해당하는 태그와 그 모든 하위 태그들의 ID를 Set으로 반환합니다. (내부 헬퍼 메서드)
     * TagRepository를 사용하여 하위 태그 목록을 조회합니다.
     *
     * @param tagId 확장할 기준 태그 ID
     * @return 기준 태그 ID와 모든 하위 태그 ID들을 포함하는 Set
     */
    private Set<Long> expandTagId(Long tagId) {
        log.debug("Expanding tag ID: {}", tagId);
        Set<Long> expandedIds = new HashSet<>();
        expandedIds.add(tagId); // 자기 자신 ID 추가

        try {
            // TagRepository를 통해 해당 태그와 모든 자식 태그 엔티티 조회
            List<Tag> childTags = tagRepository.findTagAndAllDescendants(tagId);
            // 조회된 태그들의 ID를 expandedIds Set에 추가
            for (Tag childTag : childTags) {
                expandedIds.add(childTag.getId());
            }
        } catch (Exception e) {
            // 하위 태그 조회 중 오류 발생 시 로그 기록 (예: DB 오류)
            log.error("Error expanding tag ID {}: {}", tagId, e.getMessage(), e);
        }

        log.debug("Tag ID {} expanded to: {}", tagId, expandedIds);
        return expandedIds;
    }

    /**
     * 정렬 기준 문자열(orderBy)에 따라 QueryDSL OrderSpecifier를 생성하여 반환합니다. (내부 헬퍼 메서드)
     * 유효하지 않은 정렬 기준이거나 null일 경우 기본값(최신순)으로 정렬합니다.
     *
     * @param orderBy 정렬 기준 문자열 ("avgScore", "attemptCount", "difficulty", 등)
     * @return 생성된 OrderSpecifier 객체
     */
    private OrderSpecifier<?> getOrderSpecifier(String orderBy) {
        QQuiz quiz = QQuiz.quiz;

        // orderBy 값이 null이면 기본 정렬(최신순) 적용
        if (orderBy == null) {
            return quiz.createdAt.desc();
        }

        // orderBy 문자열에 따라 적절한 OrderSpecifier 반환
        switch (orderBy.toLowerCase()) { // 대소문자 구분 없이 비교
            case "avgscore":
                return quiz.avgScore.desc(); // 평균 점수 내림차순
            case "attemptcount":
                return quiz.attemptCount.desc(); // 시도 횟수 내림차순
            case "difficulty":
                return quiz.difficultyLevel.asc(); // 난이도 오름차순 (쉬운 것부터)
            // 다른 정렬 기준 추가 가능 (예: "viewcount", "title")
            // case "viewcount":
            //     return quiz.viewCount.desc();
            default:
                // 정의되지 않은 값이면 기본 정렬(최신순) 적용
                log.warn("Unsupported order by value: '{}'. Defaulting to createdAt desc.", orderBy);
                return quiz.createdAt.desc();
        }
    }
}