package com.quizplatform.core.domain.user;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = 365199834L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUser user = new QUser("user");

    public final StringPath accessToken = createString("accessToken");

    public final QUserBattleStats battleStats;

    public final DateTimePath<java.time.ZonedDateTime> createdAt = createDateTime("createdAt", java.time.ZonedDateTime.class);

    public final StringPath email = createString("email");

    public final NumberPath<Integer> experience = createNumber("experience", Integer.class);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final BooleanPath isActive = createBoolean("isActive");

    public final DateTimePath<java.time.ZonedDateTime> lastLogin = createDateTime("lastLogin", java.time.ZonedDateTime.class);

    public final NumberPath<Integer> level = createNumber("level", Integer.class);

    public final StringPath profileImage = createString("profileImage");

    public final EnumPath<AuthProvider> provider = createEnum("provider", AuthProvider.class);

    public final StringPath providerId = createString("providerId");

    public final ListPath<com.quizplatform.core.domain.quiz.QuizAttempt, com.quizplatform.core.domain.quiz.QQuizAttempt> quizAttempts = this.<com.quizplatform.core.domain.quiz.QuizAttempt, com.quizplatform.core.domain.quiz.QQuizAttempt>createList("quizAttempts", com.quizplatform.core.domain.quiz.QuizAttempt.class, com.quizplatform.core.domain.quiz.QQuizAttempt.class, PathInits.DIRECT2);

    public final StringPath refreshToken = createString("refreshToken");

    public final NumberPath<Integer> requiredExperience = createNumber("requiredExperience", Integer.class);

    public final EnumPath<UserRole> role = createEnum("role", UserRole.class);

    public final DateTimePath<java.time.ZonedDateTime> tokenExpiresAt = createDateTime("tokenExpiresAt", java.time.ZonedDateTime.class);

    public final NumberPath<Integer> totalPoints = createNumber("totalPoints", Integer.class);

    public final DateTimePath<java.time.ZonedDateTime> updatedAt = createDateTime("updatedAt", java.time.ZonedDateTime.class);

    public final StringPath username = createString("username");

    public QUser(String variable) {
        this(User.class, forVariable(variable), INITS);
    }

    public QUser(Path<? extends User> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUser(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUser(PathMetadata metadata, PathInits inits) {
        this(User.class, metadata, inits);
    }

    public QUser(Class<? extends User> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.battleStats = inits.isInitialized("battleStats") ? new QUserBattleStats(forProperty("battleStats"), inits.get("battleStats")) : null;
    }

}

