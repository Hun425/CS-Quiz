package com.quizplatform.core.domain.question;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuestionAttempt is a Querydsl query type for QuestionAttempt
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuestionAttempt extends EntityPathBase<QuestionAttempt> {

    private static final long serialVersionUID = -1163351373L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQuestionAttempt questionAttempt = new QQuestionAttempt("questionAttempt");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final BooleanPath isCorrect = createBoolean("isCorrect");

    public final QQuestion question;

    public final com.quizplatform.core.domain.quiz.QQuizAttempt quizAttempt;

    public final NumberPath<Integer> timeTaken = createNumber("timeTaken", Integer.class);

    public final StringPath userAnswer = createString("userAnswer");

    public QQuestionAttempt(String variable) {
        this(QuestionAttempt.class, forVariable(variable), INITS);
    }

    public QQuestionAttempt(Path<? extends QuestionAttempt> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQuestionAttempt(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQuestionAttempt(PathMetadata metadata, PathInits inits) {
        this(QuestionAttempt.class, metadata, inits);
    }

    public QQuestionAttempt(Class<? extends QuestionAttempt> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.question = inits.isInitialized("question") ? new QQuestion(forProperty("question"), inits.get("question")) : null;
        this.quizAttempt = inits.isInitialized("quizAttempt") ? new com.quizplatform.core.domain.quiz.QQuizAttempt(forProperty("quizAttempt"), inits.get("quizAttempt")) : null;
    }

}

