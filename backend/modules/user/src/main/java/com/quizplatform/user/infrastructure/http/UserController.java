package com.quizplatform.user.infrastructure.http;

import com.quizplatform.user.domain.model.User;
import com.quizplatform.user.domain.model.UserRole;
import com.quizplatform.user.domain.service.UserService;
import com.quizplatform.user.infrastructure.http.dto.UserCreationRequest;
import com.quizplatform.user.infrastructure.http.dto.UserProfileUpdateRequest;
import com.quizplatform.user.infrastructure.http.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 사용자 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Controller", description = "사용자 관리 API를 제공합니다")
public class UserController {

    private final UserService userService;

    /**
     * 모든 사용자 조회
     * 
     * @return 사용자 목록
     */
    @Operation(summary = "모든 사용자 조회", description = "시스템의 모든 사용자 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 목록이 성공적으로 조회되었습니다.")
    })
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        List<UserResponse> responses = users.stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * ID로 사용자 조회
     * 
     * @param id 사용자 ID
     * @return 사용자 정보
     */
    @Operation(summary = "ID로 사용자 조회", description = "특정 ID의 사용자 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 정보가 성공적으로 조회되었습니다.",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "조회할 사용자의 ID", required = true)
            @PathVariable Long id) {
        return userService.findById(id)
                .map(UserResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 사용자 생성
     * 
     * @param request 사용자 생성 요청
     * @return 생성된 사용자 정보
     */
    @Operation(summary = "사용자 생성", description = "새로운 사용자를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "사용자가 성공적으로 생성되었습니다.",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터입니다.")
    })
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Parameter(description = "생성할 사용자 정보", required = true)
            @RequestBody UserCreationRequest request) {
        User user = request.toEntity();
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserResponse.fromEntity(createdUser));
    }

    /**
     * 사용자 프로필 업데이트
     * 
     * @param id 사용자 ID
     * @param request 프로필 업데이트 요청
     * @return 업데이트된 사용자 정보
     */
    @Operation(summary = "사용자 프로필 업데이트", description = "사용자의 프로필 정보를 업데이트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필이 성공적으로 업데이트되었습니다.",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.")
    })
    @PutMapping("/{id}/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @Parameter(description = "업데이트할 사용자의 ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "업데이트할 프로필 정보", required = true)
            @RequestBody UserProfileUpdateRequest request) {
        User updatedUser = userService.updateProfile(id, request.getUsername(), request.getProfileImage());
        return ResponseEntity.ok(UserResponse.fromEntity(updatedUser));
    }

    /**
     * 사용자 활성화/비활성화 토글
     * 
     * @param id 사용자 ID
     * @return 업데이트된 사용자 정보
     */
    @Operation(summary = "사용자 활성화 상태 토글", description = "사용자의 활성화 상태를 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "활성화 상태가 성공적으로 변경되었습니다.",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.")
    })
    @PutMapping("/{id}/toggle-active")
    public ResponseEntity<UserResponse> toggleActive(
            @Parameter(description = "상태를 변경할 사용자의 ID", required = true)
            @PathVariable Long id) {
        User updatedUser = userService.toggleActive(id);
        return ResponseEntity.ok(UserResponse.fromEntity(updatedUser));
    }

    /**
     * 사용자 권한 변경
     * 
     * @param id 사용자 ID
     * @param role 새 권한
     * @return 업데이트된 사용자 정보
     */
    @Operation(summary = "사용자 권한 변경", description = "사용자의 권한 레벨을 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "권한이 성공적으로 변경되었습니다.",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.")
    })
    @PutMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateRole(
            @Parameter(description = "권한을 변경할 사용자의 ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "변경할 권한 (USER, ADMIN 등)", required = true)
            @RequestParam UserRole role) {
        User updatedUser = userService.updateRole(id, role);
        return ResponseEntity.ok(UserResponse.fromEntity(updatedUser));
    }

    /**
     * 사용자에게 경험치 부여
     * 
     * @param id 사용자 ID
     * @param experience 경험치
     * @return 레벨업 여부
     */
    @Operation(summary = "사용자 경험치 부여", description = "사용자에게 경험치를 부여하고 레벨업 여부를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "경험치가 성공적으로 부여되었습니다."),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.")
    })
    @PostMapping("/{id}/experience")
    public ResponseEntity<Boolean> giveExperience(
            @Parameter(description = "경험치를 부여할 사용자의 ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "부여할 경험치 양", required = true, example = "100")
            @RequestParam int experience) {
        boolean leveledUp = userService.giveExperience(id, experience);
        return ResponseEntity.ok(leveledUp);
    }

    /**
     * 사용자에게 포인트 부여
     * 
     * @param id 사용자 ID
     * @param points 포인트
     * @return 성공 응답
     */
    @Operation(summary = "사용자 포인트 부여", description = "사용자에게 포인트를 부여합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "포인트가 성공적으로 부여되었습니다."),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.")
    })
    @PostMapping("/{id}/points")
    public ResponseEntity<Void> givePoints(
            @Parameter(description = "포인트를 부여할 사용자의 ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "부여할 포인트 양", required = true, example = "50")
            @RequestParam int points) {
        userService.givePoints(id, points);
        return ResponseEntity.ok().build();
    }
} 