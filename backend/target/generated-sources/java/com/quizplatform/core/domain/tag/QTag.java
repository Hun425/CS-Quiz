package com.quizplatform.core.domain.tag;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTag is a Querydsl query type for Tag
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTag extends EntityPathBase<Tag> {

    private static final long serialVersionUID = -852870190L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTag tag = new QTag("tag");

    public final SetPath<Tag, QTag> children = this.<Tag, QTag>createSet("children", Tag.class, QTag.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath description = createString("description");

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final StringPath name = createString("name");

    public final QTag parent;

    public final SetPath<com.quizplatform.core.domain.quiz.Quiz, com.quizplatform.core.domain.quiz.QQuiz> quizzes = this.<com.quizplatform.core.domain.quiz.Quiz, com.quizplatform.core.domain.quiz.QQuiz>createSet("quizzes", com.quizplatform.core.domain.quiz.Quiz.class, com.quizplatform.core.domain.quiz.QQuiz.class, PathInits.DIRECT2);

    public final SetPath<String, StringPath> synonyms = this.<String, StringPath>createSet("synonyms", String.class, StringPath.class, PathInits.DIRECT2);

    public QTag(String variable) {
        this(Tag.class, forVariable(variable), INITS);
    }

    public QTag(Path<? extends Tag> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTag(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTag(PathMetadata metadata, PathInits inits) {
        this(Tag.class, metadata, inits);
    }

    public QTag(Class<? extends Tag> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.parent = inits.isInitialized("parent") ? new QTag(forProperty("parent"), inits.get("parent")) : null;
    }

}

