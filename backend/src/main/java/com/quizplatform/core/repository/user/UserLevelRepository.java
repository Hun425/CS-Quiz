package com.quizplatform.core.repository.user;

import com.quizplatform.core.domain.user.LevelUpRecord;
import com.quizplatform.core.domain.user.UserLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserLevelRepository extends JpaRepository<UserLevel, Long> {

    Optional<UserLevel> findByUserId(Long userId);

    @Query(/* 레벨업 기록을 조회하는 쿼리 */)
    List<LevelUpRecord> findRecentLevelUpsByUserId(@Param("userId") Long userId, @Param("limit") int limit);
}
