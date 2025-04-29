package com.quizplatform.battle.infrastructure.http.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



/**
 * 인증된 사용자 정보를 확인하는 컨트롤러 예제
 */
@RestController
@RequestMapping("/battles/auth")
@RequiredArgsConstructor
public class BattleAuthController {
    //
    // /**
    //  * 현재 인증된 사용자 정보 조회
    //  * @param userDetails 인증된 사용자 정보 (@AuthenticationPrincipal 예제)
    //  * @return 사용자 정보
    //  */
    // @GetMapping("/me")
    // public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal JwtUserDetails userDetails) {
    //     if (userDetails == null) {
    //         return ResponseEntity.status(401).build();
    //     }
    //
    //     Map<String, Object> userInfo = new HashMap<>();
    //     userInfo.put("userId", userDetails.getUserId());
    //     userInfo.put("name", userDetails.getName());
    //     userInfo.put("provider", userDetails.getProvider());
    //     userInfo.put("authorities", userDetails.getAuthorities());
    //
    //     return ResponseEntity.ok(userInfo);
    // }
}
