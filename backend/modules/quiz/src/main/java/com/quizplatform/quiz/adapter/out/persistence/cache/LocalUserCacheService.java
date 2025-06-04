package com.quizplatform.quiz.adapter.out.persistence.cache;

import com.quizplatform.quiz.domain.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 로컬 메모리 기반 사용자 캐시 서비스 구현
 * 
 * <p>분산 환경이 아닌 단일 인스턴스 환경에서 사용 가능한
 * 메모리 기반 캐시 서비스입니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Slf4j
@Service
public class LocalUserCacheService implements UserCacheService {
    
    private final Map<String, UserDTO> userCache = new ConcurrentHashMap<>();
    
    @Override
    public void cacheUserInfo(UserDTO userDTO) {
        if (userDTO == null || userDTO.getId() == null) {
            log.warn("Attempted to cache invalid user data");
            return;
        }
        
        userCache.put(userDTO.getId(), userDTO);
        log.debug("User {} cached successfully", userDTO.getId());
    }
    
    @Override
    public UserDTO getUserInfo(String userId) {
        if (userId == null) {
            log.warn("Attempted to retrieve user with null ID");
            return null;
        }
        
        UserDTO userDTO = userCache.get(userId);
        if (userDTO == null) {
            log.debug("User {} not found in cache", userId);
        }
        
        return userDTO;
    }
    
    @Override
    public boolean removeUserInfo(String userId) {
        if (userId == null) {
            log.warn("Attempted to remove user with null ID");
            return false;
        }
        
        UserDTO removed = userCache.remove(userId);
        boolean success = removed != null;
        
        if (success) {
            log.debug("User {} removed from cache", userId);
        } else {
            log.debug("User {} not found in cache for removal", userId);
        }
        
        return success;
    }
    
    @Override
    public Collection<UserDTO> getAllUsers() {
        return userCache.values();
    }
} 