package com.quizplatform.modules.user.domain.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserLevelHistory is a Querydsl query type for UserLevelHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserLevelHistory extends EntityPathBase<UserLevelHistory> {

    private static final long serialVersionUID = -527205441L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserLevelHistory userLevelHistory = new QUserLevelHistory("userLevelHistory");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> level = createNumber("level", Integer.class);

    public final NumberPath<Integer> previousLevel = createNumber("previousLevel", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final QUser user;

    public QUserLevelHistory(String variable) {
        this(UserLevelHistory.class, forVariable(variable), INITS);
    }

    public QUserLevelHistory(Path<? extends UserLevelHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserLevelHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserLevelHistory(PathMetadata metadata, PathInits inits) {
        this(UserLevelHistory.class, metadata, inits);
    }

    public QUserLevelHistory(Class<? extends UserLevelHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user"), inits.get("user")) : null;
    }

}

