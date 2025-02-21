package com.quizplatform.core.domain.battle;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBattleRoom is a Querydsl query type for BattleRoom
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBattleRoom extends EntityPathBase<BattleRoom> {

    private static final long serialVersionUID = -1515425163L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBattleRoom battleRoom = new QBattleRoom("battleRoom");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> currentQuestionIndex = createNumber("currentQuestionIndex", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> currentQuestionStartTime = createDateTime("currentQuestionStartTime", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> endTime = createDateTime("endTime", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> maxParticipants = createNumber("maxParticipants", Integer.class);

    public final SetPath<BattleParticipant, QBattleParticipant> participants = this.<BattleParticipant, QBattleParticipant>createSet("participants", BattleParticipant.class, QBattleParticipant.class, PathInits.DIRECT2);

    public final com.quizplatform.core.domain.quiz.QQuiz quiz;

    public final StringPath roomCode = createString("roomCode");

    public final DateTimePath<java.time.LocalDateTime> startTime = createDateTime("startTime", java.time.LocalDateTime.class);

    public final EnumPath<BattleRoomStatus> status = createEnum("status", BattleRoomStatus.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public final QBattleParticipant winner;

    public QBattleRoom(String variable) {
        this(BattleRoom.class, forVariable(variable), INITS);
    }

    public QBattleRoom(Path<? extends BattleRoom> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBattleRoom(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBattleRoom(PathMetadata metadata, PathInits inits) {
        this(BattleRoom.class, metadata, inits);
    }

    public QBattleRoom(Class<? extends BattleRoom> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.quiz = inits.isInitialized("quiz") ? new com.quizplatform.core.domain.quiz.QQuiz(forProperty("quiz"), inits.get("quiz")) : null;
        this.winner = inits.isInitialized("winner") ? new QBattleParticipant(forProperty("winner"), inits.get("winner")) : null;
    }

}

