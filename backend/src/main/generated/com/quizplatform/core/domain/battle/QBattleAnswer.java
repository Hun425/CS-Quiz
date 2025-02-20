package com.quizplatform.core.domain.battle;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBattleAnswer is a Querydsl query type for BattleAnswer
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBattleAnswer extends EntityPathBase<BattleAnswer> {

    private static final long serialVersionUID = -817155368L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBattleAnswer battleAnswer = new QBattleAnswer("battleAnswer");

    public final StringPath answer = createString("answer");

    public final BooleanPath correct = createBoolean("correct");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> earnedPoints = createNumber("earnedPoints", Integer.class);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final QBattleParticipant participant;

    public final com.quizplatform.core.domain.question.QQuestion question;

    public final NumberPath<Integer> timeBonus = createNumber("timeBonus", Integer.class);

    public final NumberPath<Integer> timeTaken = createNumber("timeTaken", Integer.class);

    public QBattleAnswer(String variable) {
        this(BattleAnswer.class, forVariable(variable), INITS);
    }

    public QBattleAnswer(Path<? extends BattleAnswer> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBattleAnswer(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBattleAnswer(PathMetadata metadata, PathInits inits) {
        this(BattleAnswer.class, metadata, inits);
    }

    public QBattleAnswer(Class<? extends BattleAnswer> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.participant = inits.isInitialized("participant") ? new QBattleParticipant(forProperty("participant"), inits.get("participant")) : null;
        this.question = inits.isInitialized("question") ? new com.quizplatform.core.domain.question.QQuestion(forProperty("question"), inits.get("question")) : null;
    }

}

