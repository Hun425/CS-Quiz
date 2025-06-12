package com.quizplatform.common.auth;

import java.util.List;

public record CurrentUserInfo(Long id, String email, List<String> roles) {
    
    /**
     * 사용자 ID만으로 생성하는 생성자 (하위 호환성)
     */
    public static CurrentUserInfo of(Long id) {
        return new CurrentUserInfo(id, null, List.of());
    }
    
    /**
     * 사용자가 특정 권한을 가지고 있는지 확인
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
    
    /**
     * 사용자가 관리자 권한을 가지고 있는지 확인
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }
}
