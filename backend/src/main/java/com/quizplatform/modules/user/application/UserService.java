import com.quizplatform.core.config.security.oauth.OAuth2UserInfo;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.repository.UserRepository;
import com.quizplatform.modules.user.application.dto.AuthResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final OAuth2ClientFactory oAuth2ClientFactory;
    private final com.quizplatform.core.security.jwt.JwtTokenProvider tokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    public AuthResponse socialLogin(String provider, String code) {
        // 1. OAuth2 클라이언트를 통해 액세스 토큰 획득
        OAuth2Client client = oAuth2ClientFactory.getClient(provider);
        OAuth2Token token = client.getToken(code);

        // 2. 액세스 토큰으로 사용자 정보 조회
        OAuth2UserInfo userInfo = client.getUserInfo(token.getAccessToken());

        // 3. 사용자 정보로 회원가입 또는 로그인 처리
        User user = userRepository.findByProviderAndProviderId(provider, userInfo.getId())
                .orElseGet(() -> registerNewUser(provider, userInfo));

        // 4. JWT 토큰 발급
        String accessToken = tokenProvider.generateAccessToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);

        // 5. Redis에 Refresh 토큰 저장
        saveRefreshToken(user.getId(), refreshToken);

        return createAuthResponse(user, accessToken, refreshToken);
    }

    private User registerNewUser(String provider, OAuth2UserInfo userInfo) {
        // 이메일 중복 검증
        userRepository.findByEmail(userInfo.getEmail())
                .ifPresent(u -> {
                    throw new EmailAlreadyExistsException(userInfo.getEmail());
                });

        User user = User.builder()
                .provider(provider)
                .providerId(userInfo.getId())
                .email(userInfo.getEmail())
                .username(generateUniqueUsername(userInfo.getName()))
                .profileImage(userInfo.getImageUrl())
                .role("USER")
                .build();

        return userRepository.save(user);
    }


}