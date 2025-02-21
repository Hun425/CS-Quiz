package com.quizplatform.core.domain.user;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserLevel is a Querydsl query type for UserLevel
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserLevel extends EntityPathBase<UserLevel> {

    private static final long serialVersionUID = 2118336970L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserLevel userLevel = new QUserLevel("userLevel");

    public final SetPath<com.quizplatform.core.domain.quiz.Achievement, EnumPath<com.quizplatform.core.domain.quiz.Achievement>> achievements = this.<com.quizplatform.core.domain.quiz.Achievement, EnumPath<com.quizplatform.core.domain.quiz.Achievement>>createSet("achievements", com.quizplatform.core.domain.quiz.Achievement.class, EnumPath.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> currentExp = createNumber("currentExp", Integer.class);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final NumberPath<Integer> level = createNumber("level", Integer.class);

    public final NumberPath<Integer> requiredExp = createNumber("requiredExp", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final QUser user;

    public QUserLevel(String variable) {
        this(UserLevel.class, forVariable(variable), INITS);
    }

    public QUserLevel(Path<? extends UserLevel> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserLevel(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserLevel(PathMetadata metadata, PathInits inits) {
        this(UserLevel.class, metadata, inits);
    }

    public QUserLevel(Class<? extends UserLevel> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

