package com.quizplatform.core.domain.quiz;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuizAttempt is a Querydsl query type for QuizAttempt
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuizAttempt extends EntityPathBase<QuizAttempt> {

    private static final long serialVersionUID = 472641875L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQuizAttempt quizAttempt = new QQuizAttempt("quizAttempt");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> endTime = createDateTime("endTime", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isCompleted = createBoolean("isCompleted");

    public final ListPath<com.quizplatform.core.domain.question.QuestionAttempt, com.quizplatform.core.domain.question.QQuestionAttempt> questionAttempts = this.<com.quizplatform.core.domain.question.QuestionAttempt, com.quizplatform.core.domain.question.QQuestionAttempt>createList("questionAttempts", com.quizplatform.core.domain.question.QuestionAttempt.class, com.quizplatform.core.domain.question.QQuestionAttempt.class, PathInits.DIRECT2);

    public final QQuiz quiz;

    public final EnumPath<QuizType> quizType = createEnum("quizType", QuizType.class);

    public final NumberPath<Integer> score = createNumber("score", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> startTime = createDateTime("startTime", java.time.LocalDateTime.class);

    public final NumberPath<Integer> timeTaken = createNumber("timeTaken", Integer.class);

    public final com.quizplatform.core.domain.user.QUser user;

    public QQuizAttempt(String variable) {
        this(QuizAttempt.class, forVariable(variable), INITS);
    }

    public QQuizAttempt(Path<? extends QuizAttempt> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQuizAttempt(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQuizAttempt(PathMetadata metadata, PathInits inits) {
        this(QuizAttempt.class, metadata, inits);
    }

    public QQuizAttempt(Class<? extends QuizAttempt> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.quiz = inits.isInitialized("quiz") ? new QQuiz(forProperty("quiz"), inits.get("quiz")) : null;
        this.user = inits.isInitialized("user") ? new com.quizplatform.core.domain.user.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

