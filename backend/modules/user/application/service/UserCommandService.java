package com.quizplatform.user.application.service;

import com.quizplatform.user.application.port.in.RegisterUserUseCase;
import com.quizplatform.user.application.port.in.UpdateUserLevelUseCase;
import com.quizplatform.user.application.port.in.UpdateUserProfileUseCase;
import com.quizplatform.user.application.port.in.command.RegisterUserCommand;
import com.quizplatform.user.application.port.in.command.UpdateUserLevelCommand;
import com.quizplatform.user.application.port.in.command.UpdateUserProfileCommand;
import com.quizplatform.user.application.port.out.DomainEventPublisherPort;
import com.quizplatform.user.application.port.out.LoadUserPort;
import com.quizplatform.user.application.port.out.SaveUserLevelHistoryPort;
import com.quizplatform.user.application.port.out.SaveUserPort;
import com.quizplatform.user.domain.event.UserLevelUpEvent;
import com.quizplatform.user.domain.event.UserRegisteredEvent;
import com.quizplatform.user.domain.model.User;
import com.quizplatform.user.domain.model.UserLevelHistory;
import com.quizplatform.user.domain.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandService implements RegisterUserUseCase, UpdateUserLevelUseCase, UpdateUserProfileUseCase {

    private final SaveUserPort saveUserPort;
    private final LoadUserPort loadUserPort;
    private final SaveUserLevelHistoryPort saveUserLevelHistoryPort;
    private final PasswordEncoder passwordEncoder;
    private final DomainEventPublisherPort eventPublisher;

    @Override
    public User registerUser(RegisterUserCommand command) {
        // 이메일 또는 사용자 이름 중복 확인
        if (loadUserPort.findByUsername(command.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + command.getUsername());
        }
        if (loadUserPort.findByEmail(command.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + command.getEmail());
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(command.getPassword());

        // 새 사용자 생성 (빌더 사용, 초기 레벨 등 설정)
        User newUser = User.builder()
                .username(command.getUsername())
                .password(encodedPassword)
                .email(command.getEmail())
                .nickname(command.getNickname() != null ? command.getNickname() : command.getUsername())
                .provider(command.getProvider())
                .providerId(command.getProviderId())
                .profileImage(command.getProfileImage())
                .roles(Set.of(UserRole.ROLE_USER))
                .isActive(true)
                .level(1)
                .experience(0)
                .requiredExperience(100)
                .totalPoints(0)
                .build();

        // 사용자 저장
        User savedUser = saveUserPort.saveNewUser(newUser);
        
        // 사용자 등록 이벤트 발행
        eventPublisher.publish(new UserRegisteredEvent(savedUser));
        
        return savedUser;
    }

    @Override
    public void updateUserLevel(UpdateUserLevelCommand command) {
        // 사용자 조회
        User user = loadUserPort.findById(command.getUserId())
                .orElseThrow(() -> new NoSuchElementException("User not found: " + command.getUserId()));

        int previousLevel = user.getLevel();
        int newLevel = command.getNewLevel();

        // 레벨 업데이트
        if (previousLevel != newLevel) {
            user.updateLevelAndExp(newLevel, user.getExperience());
            saveUserPort.updateUser(user);

            // 레벨 변경 이력 저장
            UserLevelHistory history = UserLevelHistory.builder()
                    .userId(user.getId())
                    .previousLevel(previousLevel)
                    .level(newLevel)
                    .build();
            saveUserLevelHistoryPort.save(history);
            
            // 레벨업 이벤트 발행 (레벨이 높아진 경우에만)
            if (newLevel > previousLevel) {
                eventPublisher.publish(new UserLevelUpEvent(user, previousLevel, newLevel));
            }
        }
    }

    @Override
    public User updateUserProfile(UpdateUserProfileCommand command) {
        // 사용자 조회
        User user = loadUserPort.findById(command.getUserId())
                .orElseThrow(() -> new NoSuchElementException("User not found: " + command.getUserId()));

        // 프로필 정보 업데이트 (null이 아닌 값만 업데이트)
        boolean updated = false;
        
        if (command.getNickname() != null && !command.getNickname().equals(user.getNickname())) {
            user.updateNickname(command.getNickname());
            updated = true;
        }
        
        if (command.getEmail() != null && !command.getEmail().equals(user.getEmail())) {
            // 이메일 중복 검사
            if (loadUserPort.findByEmail(command.getEmail())
                    .filter(found -> !found.getId().equals(user.getId()))
                    .isPresent()) {
                throw new IllegalArgumentException("Email already exists: " + command.getEmail());
            }
            user.updateEmail(command.getEmail());
            updated = true;
        }
        
        if (command.getProfileImage() != null && !command.getProfileImage().equals(user.getProfileImage())) {
            user.updateProfile(user.getUsername(), command.getProfileImage());
            updated = true;
        }

        // 변경 사항이 있을 경우에만 저장
        if (updated) {
            return saveUserPort.updateUser(user);
        }
        return user;
    }
}