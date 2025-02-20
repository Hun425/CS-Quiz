package com.quizplatform.core.domain.quiz;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuiz is a Querydsl query type for Quiz
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuiz extends EntityPathBase<Quiz> {

    private static final long serialVersionUID = 1974667290L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQuiz quiz = new QQuiz("quiz");

    public final NumberPath<Integer> attemptCount = createNumber("attemptCount", Integer.class);

    public final NumberPath<Double> avgScore = createNumber("avgScore", Double.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final com.quizplatform.core.domain.user.QUser creator;

    public final StringPath description = createString("description");

    public final EnumPath<DifficultyLevel> difficultyLevel = createEnum("difficultyLevel", DifficultyLevel.class);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final BooleanPath isPublic = createBoolean("isPublic");

    public final NumberPath<Integer> questionCount = createNumber("questionCount", Integer.class);

    public final ListPath<com.quizplatform.core.domain.question.Question, com.quizplatform.core.domain.question.QQuestion> questions = this.<com.quizplatform.core.domain.question.Question, com.quizplatform.core.domain.question.QQuestion>createList("questions", com.quizplatform.core.domain.question.Question.class, com.quizplatform.core.domain.question.QQuestion.class, PathInits.DIRECT2);

    public final EnumPath<QuizType> quizType = createEnum("quizType", QuizType.class);

    public final SetPath<com.quizplatform.core.domain.tag.Tag, com.quizplatform.core.domain.tag.QTag> tags = this.<com.quizplatform.core.domain.tag.Tag, com.quizplatform.core.domain.tag.QTag>createSet("tags", com.quizplatform.core.domain.tag.Tag.class, com.quizplatform.core.domain.tag.QTag.class, PathInits.DIRECT2);

    public final NumberPath<Integer> timeLimit = createNumber("timeLimit", Integer.class);

    public final StringPath title = createString("title");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> validUntil = createDateTime("validUntil", java.time.LocalDateTime.class);

    public final NumberPath<Integer> viewCount = createNumber("viewCount", Integer.class);

    public QQuiz(String variable) {
        this(Quiz.class, forVariable(variable), INITS);
    }

    public QQuiz(Path<? extends Quiz> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQuiz(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQuiz(PathMetadata metadata, PathInits inits) {
        this(Quiz.class, metadata, inits);
    }

    public QQuiz(Class<? extends Quiz> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.creator = inits.isInitialized("creator") ? new com.quizplatform.core.domain.user.QUser(forProperty("creator")) : null;
    }

}

