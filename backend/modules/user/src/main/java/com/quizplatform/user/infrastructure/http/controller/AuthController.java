package com.quizplatform.user.infrastructure.http.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 인증된 사용자 정보를 다루는 컨트롤러 예제
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    // /**
    //  * 인증된 사용자 정보 조회 API
    //  * @param userDetails 인증된 사용자 정보
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
