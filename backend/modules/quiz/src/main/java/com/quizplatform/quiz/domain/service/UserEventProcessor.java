package com.quizplatform.quiz.domain.service;

import com.quizplatform.common.event.user.UserLevelUpEvent;
import com.quizplatform.common.event.user.UserRegisteredEvent;
import com.quizplatform.quiz.domain.dto.UserDTO;
import com.quizplatform.quiz.adapter.out.persistence.cache.UserCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 사용자 이벤트 처리 서비스
 * 
 * <p>User 모듈에서 발생한 이벤트를 처리하고
 * Quiz 모듈에 필요한 사용자 정보를 관리합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventProcessor {
    
    private final UserCacheService userCacheService;
    private final QuizRecommendationService recommendationService;
    
    /**
     * 사용자 등록 이벤트 처리
     * 
     * @param event 사용자 등록 이벤트
     */
    public void processUserRegisteredEvent(UserRegisteredEvent event) {
        log.info("Processing user registered event: {}", event);
        
        // DTO 생성하여 캐시에 저장
        UserDTO userDTO = new UserDTO(
            event.getUserId(),
            event.getUsername(),
            event.getEmail(),
            1, // 초기 레벨
            0, // 초기 경험치
            "USER" // 기본 역할
        );
        
        // 사용자 정보 캐싱
        userCacheService.cacheUserInfo(userDTO);
        
        // 초보자용 퀴즈 추천 설정
        recommendationService.initializeRecommendations(event.getUserId());
        
        log.info("User {} data cached in Quiz module", event.getUserId());
    }
    
    /**
     * 사용자 레벨업 이벤트 처리
     * 
     * @param event 사용자 레벨업 이벤트
     */
    public void processUserLevelUpEvent(UserLevelUpEvent event) {
        log.info("Processing user level up event: {}", event);
        
        // 캐시된 사용자 정보 조회
        UserDTO userDTO = userCacheService.getUserInfo(event.getUserId());
        
        if (userDTO != null) {
            // 사용자 레벨 업데이트
            userDTO.setLevel(event.getNewLevel());
            userCacheService.cacheUserInfo(userDTO);
            
            // 레벨에 맞는 퀴즈 추천 조정
            recommendationService.adjustRecommendationsByLevel(
                event.getUserId(), 
                event.getNewLevel()
            );
            
            log.info("User {} level updated to {} in Quiz module", 
                    event.getUserId(), event.getNewLevel());
        } else {
            // 캐시에 사용자 정보가 없는 경우 처리
            log.warn("User {} not found in cache during level up event", event.getUserId());
            UserDTO newUserDTO = new UserDTO(
                event.getUserId(),
                event.getUsername(),
                null, // 이메일 정보 없음
                event.getNewLevel(),
                0, // 경험치 정보 없음
                "USER" // 기본 역할
            );
            userCacheService.cacheUserInfo(newUserDTO);
            recommendationService.initializeRecommendations(event.getUserId());
        }
    }
} 