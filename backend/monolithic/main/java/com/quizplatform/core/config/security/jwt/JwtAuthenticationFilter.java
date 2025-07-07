package com.quizplatform.core.config.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService; // CustomUserDetailsService 사용
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * HTTP 요청에서 JWT(Json Web Token)를 추출하고 유효성을 검사하여,
 * 유효한 토큰인 경우 Spring Security Context에 인증 정보를 설정하는 필터입니다.
 * 모든 요청에 대해 한 번만 실행되도록 {@link OncePerRequestFilter}를 상속받습니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider; // JWT 생성 및 검증 컴포넌트
    private final UserDetailsService userDetailsService; // 사용자 정보 로드 서비스 (CustomUserDetailsService 주입)

    /**
     * 실제 필터링 로직을 수행합니다. 요청 헤더에서 JWT를 추출하고, 유효성을 검증한 후,
     * 유효한 토큰이면 사용자 정보를 로드하여 Spring Security Context에 인증 객체를 설정합니다.
     *
     * @param request     HTTP 요청 객체
     * @param response    HTTP 응답 객체
     * @param filterChain 필터 체인 객체
     * @throws ServletException 서블릿 처리 중 예외 발생 시
     * @throws IOException      입출력 처리 중 예외 발생 시
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. 요청 헤더에서 JWT 추출
            String jwt = getJwtFromRequest(request);
            log.trace("JWT extracted from request: {}", jwt != null ? jwt.substring(0, Math.min(20, jwt.length())) + "..." : "null");

            // 2. JWT가 존재하고 유효한지 검증
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                log.debug("Valid JWT found, proceeding with authentication setup.");
                // 3. JWT에서 사용자 ID 추출 (Subject 클레임 등 활용)
                String userId = tokenProvider.getUserIdFromToken(jwt);
                log.info("User ID extracted from JWT: {}", userId);

                // 4. 사용자 ID를 이용하여 UserDetails 객체 로드
                // CustomUserDetailsService의 loadUserByUsername이 ID 문자열도 처리하도록 구현됨
                UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
                log.debug("UserDetails loaded for user ID {}: {}", userId, userDetails);

                // 5. UserDetails를 기반으로 Authentication 객체 생성 (인증 완료 상태)
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                // 요청 정보(IP 주소 등)를 Authentication 객체에 추가
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. SecurityContextHolder에 Authentication 객체 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authentication successfully set in SecurityContext for user: {}", userId);

            } else if (StringUtils.hasText(jwt)) {
                // JWT가 존재하지만 유효하지 않은 경우 경고 로그 출력
                log.warn("Invalid JWT received: {}", jwt.substring(0, Math.min(20, jwt.length())) + "...");
            }
        } catch (Exception ex) {
            // 인증 처리 중 예외 발생 시 에러 로그 출력
            log.error("Could not set user authentication in security context", ex);
        }

        // 7. 다음 필터 호출
        filterChain.doFilter(request, response);
    }

    /**
     * HttpServletRequest의 'Authorization' 헤더에서 'Bearer' 접두사를 제거하고
     * 순수 JWT 문자열을 추출합니다. 헤더가 없거나 형식이 잘못된 경우 null을 반환합니다.
     * "Bearer Bearer " 와 같은 이중 접두사도 처리합니다.
     *
     * @param request HTTP 요청 객체
     * @return 추출된 JWT 문자열 또는 null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // 'Authorization' 헤더 값 가져오기
        String bearerToken = request.getHeader("Authorization");
        log.trace("Authorization header value: {}", bearerToken);

        // 헤더 값이 존재하고 비어있지 않은지 확인
        if (StringUtils.hasText(bearerToken)) {
            // "Bearer Bearer " 접두사 확인 및 제거
            if (bearerToken.startsWith("Bearer Bearer ")) {
                log.debug("Double 'Bearer ' prefix found, removing both.");
                return bearerToken.substring(14).trim(); // "Bearer Bearer " 길이 = 14
            }
            // "Bearer " 접두사 확인 및 제거
            else if (bearerToken.startsWith("Bearer ")) {
                log.debug("Standard 'Bearer ' prefix found, removing it.");
                return bearerToken.substring(7).trim(); // "Bearer " 길이 = 7
            }
            // 접두사가 없는 경우, 토큰 자체로 간주 (일부 클라이언트 구현 고려)
            else {
                log.debug("No 'Bearer ' prefix found, considering the header value as token.");
                return bearerToken.trim();
            }
        }
        // 헤더가 없거나 비어있는 경우
        log.trace("Authorization header is missing or empty.");
        return null;
    }
}