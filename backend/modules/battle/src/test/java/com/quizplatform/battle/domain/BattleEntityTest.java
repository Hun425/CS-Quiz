package com.quizplatform.battle.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.quizplatform.battle.domain.model.Battle;
import com.quizplatform.battle.domain.model.BattleStatus;

class BattleEntityTest {

    @Test
    @DisplayName("대결 엔티티 생성 테스트")
    void createBattleTest() {
        // given
        Long hostUserId = 1L;
        Long guestUserId = 2L;
        List<Long> quizIds = Arrays.asList(1L, 2L, 3L, 4L, 5L);
        
        // when
        Battle battle = Battle.builder()
                .hostUserId(hostUserId)
                .guestUserId(guestUserId)
                .quizIds(quizIds)
                .status(BattleStatus.WAITING)
                .build();
        
        // then
        assertThat(battle).isNotNull();
        assertThat(battle.getHostUserId()).isEqualTo(hostUserId);
        assertThat(battle.getGuestUserId()).isEqualTo(guestUserId);
        assertThat(battle.getQuizIds()).containsExactlyElementsOf(quizIds);
        assertThat(battle.getStatus()).isEqualTo(BattleStatus.WAITING);
        assertThat(battle.getHostScore()).isEqualTo(0);
        assertThat(battle.getGuestScore()).isEqualTo(0);
    }
    
    @Test
    @DisplayName("대결 상태 변경 테스트")
    void changeBattleStatusTest() {
        // given
        Battle battle = Battle.builder()
                .hostUserId(1L)
                .guestUserId(2L)
                .quizIds(Arrays.asList(1L, 2L, 3L, 4L, 5L))
                .status(BattleStatus.WAITING)
                .build();
        
        // when
        battle.startBattle();
        
        // then
        assertThat(battle.getStatus()).isEqualTo(BattleStatus.IN_PROGRESS);
        
        // when
        battle.endBattle();
        
        // then
        assertThat(battle.getStatus()).isEqualTo(BattleStatus.COMPLETED);
    }
    
    @Test
    @DisplayName("점수 업데이트 테스트")
    void updateScoreTest() {
        // given
        Battle battle = Battle.builder()
                .hostUserId(1L)
                .guestUserId(2L)
                .quizIds(Arrays.asList(1L, 2L, 3L, 4L, 5L))
                .status(BattleStatus.IN_PROGRESS)
                .build();
        
        // when - 호스트 점수 증가
        battle.incrementHostScore();
        battle.incrementHostScore();
        
        // then
        assertThat(battle.getHostScore()).isEqualTo(2);
        assertThat(battle.getGuestScore()).isEqualTo(0);
        
        // when - 게스트 점수 증가
        battle.incrementGuestScore();
        
        // then
        assertThat(battle.getHostScore()).isEqualTo(2);
        assertThat(battle.getGuestScore()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("승자 결정 테스트")
    void determineWinnerTest() {
        // given
        Battle battle = Battle.builder()
                .hostUserId(1L)
                .guestUserId(2L)
                .quizIds(Arrays.asList(1L, 2L, 3L, 4L, 5L))
                .status(BattleStatus.IN_PROGRESS)
                .build();
        
        // when - 호스트가 더 많은 점수
        battle.incrementHostScore();
        battle.incrementHostScore();
        battle.incrementHostScore();
        battle.incrementGuestScore();
        battle.endBattle();
        
        // then
        assertThat(battle.getWinnerId()).isEqualTo(battle.getHostUserId());
        
        // given
        Battle battle2 = Battle.builder()
                .hostUserId(1L)
                .guestUserId(2L)
                .quizIds(Arrays.asList(1L, 2L, 3L, 4L, 5L))
                .status(BattleStatus.IN_PROGRESS)
                .build();
        
        // when - 게스트가 더 많은 점수
        battle2.incrementGuestScore();
        battle2.incrementGuestScore();
        battle2.incrementHostScore();
        battle2.endBattle();
        
        // then
        assertThat(battle2.getWinnerId()).isEqualTo(battle2.getGuestUserId());
    }
} 