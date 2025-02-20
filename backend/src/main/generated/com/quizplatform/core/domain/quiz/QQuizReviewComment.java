package com.quizplatform.core.domain.quiz;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuizReviewComment is a Querydsl query type for QuizReviewComment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuizReviewComment extends EntityPathBase<QuizReviewComment> {

    private static final long serialVersionUID = 723697133L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQuizReviewComment quizReviewComment = new QQuizReviewComment("quizReviewComment");

    public final com.quizplatform.core.domain.user.QUser commenter;

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final QQuizReview parentReview;

    public QQuizReviewComment(String variable) {
        this(QuizReviewComment.class, forVariable(variable), INITS);
    }

    public QQuizReviewComment(Path<? extends QuizReviewComment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQuizReviewComment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQuizReviewComment(PathMetadata metadata, PathInits inits) {
        this(QuizReviewComment.class, metadata, inits);
    }

    public QQuizReviewComment(Class<? extends QuizReviewComment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.commenter = inits.isInitialized("commenter") ? new com.quizplatform.core.domain.user.QUser(forProperty("commenter"), inits.get("commenter")) : null;
        this.parentReview = inits.isInitialized("parentReview") ? new QQuizReview(forProperty("parentReview"), inits.get("parentReview")) : null;
    }

}

