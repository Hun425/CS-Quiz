package com.quizplatform.core.domain.question;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuestion is a Querydsl query type for Question
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuestion extends EntityPathBase<Question> {

    private static final long serialVersionUID = -1556473158L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQuestion question = new QQuestion("question");

    public final StringPath codeSnippet = createString("codeSnippet");

    public final StringPath correctAnswer = createString("correctAnswer");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final EnumPath<com.quizplatform.core.domain.quiz.DifficultyLevel> difficultyLevel = createEnum("difficultyLevel", com.quizplatform.core.domain.quiz.DifficultyLevel.class);

    public final StringPath explanation = createString("explanation");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath options = createString("options");

    public final NumberPath<Integer> points = createNumber("points", Integer.class);

    public final StringPath questionText = createString("questionText");

    public final EnumPath<QuestionType> questionType = createEnum("questionType", QuestionType.class);

    public final com.quizplatform.core.domain.quiz.QQuiz quiz;

    public final NumberPath<Integer> timeLimitSeconds = createNumber("timeLimitSeconds", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QQuestion(String variable) {
        this(Question.class, forVariable(variable), INITS);
    }

    public QQuestion(Path<? extends Question> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQuestion(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQuestion(PathMetadata metadata, PathInits inits) {
        this(Question.class, metadata, inits);
    }

    public QQuestion(Class<? extends Question> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.quiz = inits.isInitialized("quiz") ? new com.quizplatform.core.domain.quiz.QQuiz(forProperty("quiz"), inits.get("quiz")) : null;
    }

}

