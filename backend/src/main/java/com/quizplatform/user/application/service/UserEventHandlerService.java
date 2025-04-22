package com.quizplatform.user.application.service;

import com.quizplatform.user.application.port.out.DomainEventPublisherPort;
import com.quizplatform.user.application.port.out.LoadUserPort;
import com.quizplatform.user.application.port.out.LoadUserLevelPort;
import com.quizplatform.user.application.port.out.SaveUserAchievementHistoryPort;
import com.quizplatform.user.application.port.out.SaveUserPort;
import com.quizplatform.user.domain.event.UserAchievementEarnedEvent;
import com.quizplatform.user.domain.event.UserLevelUpEvent;
import com.quizplatform.user.domain.model.User;
import com.quizplatform.user.domain.model.UserAchievementHistory;
import com.quizplatform.user.domain.model.UserLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * 외부 이벤트 처리를 위한 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventHandlerService {

    private final LoadUserPort loadUserPort;
    private final SaveUserPort saveUserPort;
    private final LoadUserLevelPort loadUserLevelPort;
    private final SaveUserAchievementHistoryPort saveUserAchievementHistoryPort;
    private final DomainEventPublisherPort eventPublisher;

    /**
     * 퀴즈 완료 이벤트 처리 (경험치 획득 및 레벨업)
     */
    @Transactional
    public void handleQuizCompleted(Long userId, int score, int experienceGained) {
        try {
            // 사용자 조회
            User user = loadUserPort.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));

            // 경험치 획득 및 레벨업 처리
            int previousLevel = user.getLevel();
            boolean leveledUp = user.gainExperience(experienceGained);
            
            // 사용자 정보 업데이트
            User updatedUser = saveUserPort.updateUser(user);
            
            // 레벨업 되었으면 이벤트 발행
            if (leveledUp) {
                log.info("User {} leveled up from {} to {}", userId, previousLevel, user.getLevel());
                eventPublisher.publish(new UserLevelUpEvent(updatedUser, previousLevel, user.getLevel()));
            }
        } catch (Exception e) {
            log.error("Error handling quiz completed event for user {}", userId, e);
            throw e;
        }
    }

    /**
     * 업적 획득 이벤트 처리 (업적 이력 저장 및 포인트 부여)
     */
    @Transactional
    public void handleAchievementUnlocked(Long userId, String achievementId, String achievementName, int pointsAwarded) {
        try {
            // 사용자 조회
            User user = loadUserPort.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));

            // 업적 이력 저장
            UserAchievementHistory achievementHistory = UserAchievementHistory.builder()
                    .userId(userId)
                    .achievementId(achievementId)
                    .achievementName(achievementName)
                    .pointsAwarded(pointsAwarded)
                    .earnedAt(ZonedDateTime.now())
                    .build();
            
            saveUserAchievementHistoryPort.save(achievementHistory);
            
            // 포인트 추가
            int oldPoints = user.getTotalPoints();
            user.addPoints(pointsAwarded);
            log.info("User {} points updated from {} to {}", userId, oldPoints, user.getTotalPoints());
            
            // 사용자 업데이트 및 이벤트 발행
            User updatedUser = saveUserPort.updateUser(user);
            eventPublisher.publish(new UserAchievementEarnedEvent(updatedUser, achievementId, achievementName, pointsAwarded));
            
            log.info("User {} earned achievement {} with {} points", userId, achievementName, pointsAwarded);
        } catch (Exception e) {
            log.error("Error handling achievement unlocked event for user {}", userId, e);
            throw e;
        }
    }

    /**
     * 사용자 레벨에 필요한 경험치 계산
     */
    public int calculateRequiredExperience(int level) {
        // 레벨 정의에서 필요 경험치 조회
        Optional<UserLevel> userLevel = loadUserLevelPort.findByLevel(level);
        
        // 레벨 정의가 있으면 해당 값 사용, 없으면 기본 공식 적용
        return userLevel
                .map(UserLevel::getRequiredExperience)
                .orElseGet(() -> level * 100 + (int) (Math.pow(level, 1.5) * 10));
    }
}