package com.quizplatform.modules.user.domain.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserAchievementHistory is a Querydsl query type for UserAchievementHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserAchievementHistory extends EntityPathBase<UserAchievementHistory> {

    private static final long serialVersionUID = -1852102028L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserAchievementHistory userAchievementHistory = new QUserAchievementHistory("userAchievementHistory");

    public final EnumPath<Achievement> achievement = createEnum("achievement", Achievement.class);

    public final StringPath achievementName = createString("achievementName");

    public final DateTimePath<java.time.LocalDateTime> earnedAt = createDateTime("earnedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QUser user;

    public QUserAchievementHistory(String variable) {
        this(UserAchievementHistory.class, forVariable(variable), INITS);
    }

    public QUserAchievementHistory(Path<? extends UserAchievementHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserAchievementHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserAchievementHistory(PathMetadata metadata, PathInits inits) {
        this(UserAchievementHistory.class, metadata, inits);
    }

    public QUserAchievementHistory(Class<? extends UserAchievementHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user"), inits.get("user")) : null;
    }

}

