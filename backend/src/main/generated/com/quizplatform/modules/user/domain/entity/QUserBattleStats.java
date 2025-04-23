package com.quizplatform.modules.user.domain.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserBattleStats is a Querydsl query type for UserBattleStats
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserBattleStats extends EntityPathBase<UserBattleStats> {

    private static final long serialVersionUID = -1125664360L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserBattleStats userBattleStats = new QUserBattleStats("userBattleStats");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> currentStreak = createNumber("currentStreak", Integer.class);

    public final NumberPath<Integer> highestScore = createNumber("highestScore", Integer.class);

    public final NumberPath<Integer> highestStreak = createNumber("highestStreak", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> totalBattles = createNumber("totalBattles", Integer.class);

    public final NumberPath<Integer> totalCorrectAnswers = createNumber("totalCorrectAnswers", Integer.class);

    public final NumberPath<Integer> totalQuestions = createNumber("totalQuestions", Integer.class);

    public final NumberPath<Integer> totalScore = createNumber("totalScore", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final QUser user;

    public final NumberPath<Integer> wins = createNumber("wins", Integer.class);

    public QUserBattleStats(String variable) {
        this(UserBattleStats.class, forVariable(variable), INITS);
    }

    public QUserBattleStats(Path<? extends UserBattleStats> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserBattleStats(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserBattleStats(PathMetadata metadata, PathInits inits) {
        this(UserBattleStats.class, metadata, inits);
    }

    public QUserBattleStats(Class<? extends UserBattleStats> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user"), inits.get("user")) : null;
    }

}

