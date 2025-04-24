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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);
            log.debug("JWT from request: {}", jwt != null ? jwt.substring(0, Math.min(20, jwt.length())) + "..." : null);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                log.debug("JWT is valid, extracting user ID...");
                String userId = tokenProvider.getUserIdFromToken(jwt);
                log.debug("Extracted user ID: {}", userId);
                
                UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
                log.debug("Loaded UserDetails: {}", userDetails);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authentication set in SecurityContext: {}", authentication);
            } else if (StringUtils.hasText(jwt)) {
                log.warn("JWT found but invalid: {}", jwt.substring(0, Math.min(20, jwt.length())) + "...");
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        log.debug("Authorization header: {}", bearerToken);
        
        if (StringUtils.hasText(bearerToken)) {
            // 이중 Bearer 접두사 처리 (Bearer Bearer token -> token)
            if (bearerToken.startsWith("Bearer Bearer ")) {
                log.debug("Found double 'Bearer' prefix, removing both...");
                return bearerToken.substring(14);
            }
            // 일반 Bearer 접두사 처리 (Bearer token -> token)
            else if (bearerToken.startsWith("Bearer ")) {
                log.debug("Found standard 'Bearer' prefix, removing it...");
                return bearerToken.substring(7);
            }
            // 접두사 없는 경우 (token)
            else {
                log.debug("No 'Bearer' prefix found, using token as is");
                return bearerToken;
            }
        }
        return null;
    }
}