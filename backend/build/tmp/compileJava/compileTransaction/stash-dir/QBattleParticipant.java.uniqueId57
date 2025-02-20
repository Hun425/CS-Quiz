package com.quizplatform.core.domain.battle;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBattleParticipant is a Querydsl query type for BattleParticipant
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBattleParticipant extends EntityPathBase<BattleParticipant> {

    private static final long serialVersionUID = 272292953L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBattleParticipant battleParticipant = new QBattleParticipant("battleParticipant");

    public final ListPath<BattleAnswer, QBattleAnswer> answers = this.<BattleAnswer, QBattleAnswer>createList("answers", BattleAnswer.class, QBattleAnswer.class, PathInits.DIRECT2);

    public final QBattleRoom battleRoom;

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> currentScore = createNumber("currentScore", Integer.class);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final BooleanPath isReady = createBoolean("isReady");

    public final DateTimePath<java.time.LocalDateTime> lastActivity = createDateTime("lastActivity", java.time.LocalDateTime.class);

    public final com.quizplatform.core.domain.user.QUser user;

    public QBattleParticipant(String variable) {
        this(BattleParticipant.class, forVariable(variable), INITS);
    }

    public QBattleParticipant(Path<? extends BattleParticipant> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBattleParticipant(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBattleParticipant(PathMetadata metadata, PathInits inits) {
        this(BattleParticipant.class, metadata, inits);
    }

    public QBattleParticipant(Class<? extends BattleParticipant> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.battleRoom = inits.isInitialized("battleRoom") ? new QBattleRoom(forProperty("battleRoom"), inits.get("battleRoom")) : null;
        this.user = inits.isInitialized("user") ? new com.quizplatform.core.domain.user.QUser(forProperty("user")) : null;
    }

}

