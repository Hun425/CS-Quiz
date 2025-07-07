package com.quizplatform.core.service.level;

import com.quizplatform.core.domain.quiz.Achievement;
import com.quizplatform.core.domain.quiz.QuizAttempt;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.domain.user.UserAchievementHistory;
import com.quizplatform.core.domain.user.UserLevel;
import com.quizplatform.core.domain.user.UserLevelHistory;
import com.quizplatform.core.dto.battle.BattleResult;
import com.quizplatform.core.repository.UserRepository;
import com.quizplatform.core.repository.quiz.QuizAttemptRepository;
import com.quizplatform.core.repository.user.AchievementRepository;
import com.quizplatform.core.repository.user.UserLevelHistoryRepository;
import com.quizplatform.core.repository.user.UserLevelRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 사용자 레벨, 경험치, 업적 관련 로직을 정의하는 인터페이스
 * 퀴즈 완료 또는 배틀 결과에 따라 경험치를 계산하고, 레벨을 갱신하며,
 * 관련 업적 달성 여부를 확인하고 부여합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
public interface LevelingService {

    /**
     * 퀴즈 완료 후 경험치를 계산하고, 사용자 레벨을 갱신하며 업적을 체크합니다.
     * 계산된 경험치는 사용자에게 부여되며, 레벨업 발생 시 이력이 기록됩니다.
     * 퀴즈 완료와 관련된 여러 업적 달성 여부를 확인합니다.
     *
     * @param attempt 완료된 QuizAttempt 객체
     * @return 획득한 총 경험치
     */
    int calculateQuizExp(QuizAttempt attempt);

    /**
     * 배틀 완료 후 경험치를 계산하고, 사용자 레벨을 갱신하며 업적을 체크합니다.
     * 배틀 결과(승리 여부, 정답률 등)를 기반으로 경험치를 계산합니다.
     * 레벨업 처리 및 배틀 관련 업적(연승 등)을 확인합니다.
     *
     * @param result 배틀 결과 정보를 담은 BattleResult 객체
     * @param user   경험치를 받을 대상 사용자
     * @return 획득한 총 경험치
     */
    int calculateBattleExp(BattleResult result, User user);
}