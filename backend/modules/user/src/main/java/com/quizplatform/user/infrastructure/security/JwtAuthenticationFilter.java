package com.quizplatform.user.infrastructure.security;

import com.quizplatform.common.security.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT 토큰을 검증하고 인증 정보를 설정하는 필터
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 헤더에서 JWT 토큰 추출
        String jwt = getJwtFromRequest(request);
        
        // 토큰 검증 및 인증 정보 설정
        if (StringUtils.hasText(jwt) && jwtTokenUtil.validateToken(jwt)) {
            try {
                // 토큰에서 클레임 정보 추출
                Claims claims = jwtTokenUtil.getClaims(jwt);
                String userId = claims.getSubject();
                
                // 역할 정보 추출 (String 또는 List 형태로 전달될 수 있음)
                List<SimpleGrantedAuthority> authorities = extractAuthorities(claims);
                
                // 사용자 세부 정보 생성
                JwtUserDetails userDetails = new JwtUserDetails(
                        userId,
                        claims.get("name", String.class),
                        claims.get("provider", String.class),
                        authorities
                );
                
                // 인증 객체 생성 및 SecurityContext에 설정
                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
            } catch (Exception e) {
                log.error("JWT 인증 처리 중 오류 발생: {}", e.getMessage());
            }
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * 요청에서 JWT 토큰 추출
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
    
    /**
     * 클레임에서 권한 정보 추출
     */
    @SuppressWarnings("unchecked")
    private List<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
        Object roles = claims.get("roles");
        
        if (roles instanceof List) {
            return ((List<String>) roles).stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
        
        // 역할 정보가 없거나 다른 형식인 경우 기본 역할 부여
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
