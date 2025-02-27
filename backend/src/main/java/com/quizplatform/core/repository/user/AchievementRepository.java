package com.quizplatform.core.repository.user;


import com.quizplatform.core.domain.quiz.Achievement;
import com.quizplatform.core.domain.quiz.AchievementRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AchievementRepository {

    @Query(/* 업적 기록을 조회하는 쿼리 */)
    List<AchievementRecord> findRecentAchievementsByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    @Query(/* 특정 업적의 획득 기록을 조회하는 쿼리 */)
    AchievementRecord findByUserIdAndAchievement(@Param("userId") Long userId, @Param("achievement") Achievement achievement);
}
