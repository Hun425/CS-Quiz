package com.quizplatform.core.domain.quiz;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuizReview is a Querydsl query type for QuizReview
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuizReview extends EntityPathBase<QuizReview> {

    private static final long serialVersionUID = 1180889106L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQuizReview quizReview = new QQuizReview("quizReview");

    public final ListPath<QuizReviewComment, QQuizReviewComment> comments = this.<QuizReviewComment, QQuizReviewComment>createList("comments", QuizReviewComment.class, QQuizReviewComment.class, PathInits.DIRECT2);

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QQuiz quiz;

    public final NumberPath<Integer> rating = createNumber("rating", Integer.class);

    public final com.quizplatform.modules.user.domain.entity.QUser reviewer;

    public QQuizReview(String variable) {
        this(QuizReview.class, forVariable(variable), INITS);
    }

    public QQuizReview(Path<? extends QuizReview> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQuizReview(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQuizReview(PathMetadata metadata, PathInits inits) {
        this(QuizReview.class, metadata, inits);
    }

    public QQuizReview(Class<? extends QuizReview> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.quiz = inits.isInitialized("quiz") ? new QQuiz(forProperty("quiz"), inits.get("quiz")) : null;
        this.reviewer = inits.isInitialized("reviewer") ? new com.quizplatform.modules.user.domain.entity.QUser(forProperty("reviewer"), inits.get("reviewer")) : null;
    }

}

