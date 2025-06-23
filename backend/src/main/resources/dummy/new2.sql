-- 새로운 더미 데이터 추가 (백엔드 고급, 네트워크 고급)

-- 0. 필수 의존성 확인 및 생성 (이미 new.sql에서 생성되었지만 안전하게 체크)
-- 관리자 사용자 생성 (없는 경우에만)
INSERT INTO public.users (
    email, username, experience, is_active, level, provider, provider_id,
    required_experience, role, total_points, created_at, updated_at, profile_image,
    last_login, access_token, refresh_token, token_expires_at
)
SELECT 
    'admin@example.com', 'admin', 800, true, 10, 'GITHUB', 'admin_id_123',
    1000, 'ADMIN', 5000, NOW(), NOW(),
    'https://robohash.org/admin?set=set4', NOW() - INTERVAL '2 days',
    NULL, NULL, NULL
WHERE NOT EXISTS (
    SELECT 1 FROM public.users WHERE role = 'ADMIN'
);

-- 필요한 태그 생성 (없는 경우에만)
INSERT INTO public.tags (created_at, name, description)
VALUES
    (NOW(), '백엔드', '백엔드 개발 관련 기술 및 개념 (서버, 데이터베이스, API, 프레임워크 등)'),
    (NOW(), '네트워크', '네트워킹 프로토콜, 모델 및 인프라')
ON CONFLICT (name) DO NOTHING;

-- 2. 새로운 퀴즈 추가 (백엔드 고급 1개, 네트워크 고급 1개 - 각 10문제)

-- 관리자 ID 및 필요한 태그 ID 가져오기
WITH admin_user AS (
    SELECT id FROM public.users WHERE role = 'ADMIN' LIMIT 1
), backend_tag AS (
    SELECT id FROM public.tags WHERE name = '백엔드'
), network_tag AS (
    SELECT id FROM public.tags WHERE name = '네트워크'
),
-- 고급 퀴즈 생성
     inserted_advanced_quizzes AS (
         INSERT INTO public.quizzes (
                                     created_at, updated_at, title, description, difficulty_level,
                                     is_public, question_count, quiz_type, time_limit,
                                     creator_id, attempt_count, avg_score, view_count, valid_until
             )
             VALUES
                 -- 백엔드 고급
                 (NOW() - INTERVAL '1 day', NOW(), '백엔드 전문가 도전 퀴즈', '데이터베이스 심층, 시스템 설계, 보안 등 고급 백엔드 주제를 다룹니다.', 'ADVANCED',
                  true, 10, 'REGULAR', 45, (SELECT id FROM admin_user), floor(random() * 30 + 5), random() * 15 + 75, floor(random() * 150 + 20), NULL),
                 -- 네트워크 고급
                 (NOW(), NOW(), '네트워크 심층 탐구 퀴즈', 'TCP/IP 고급, 라우팅 프로토콜, 네트워크 보안 등 심층적인 네트워크 지식을 묻습니다.', 'ADVANCED',
                  true, 10, 'REGULAR', 45, (SELECT id FROM admin_user), floor(random() * 25 + 5), random() * 15 + 70, floor(random() * 130 + 15), NULL)
             RETURNING id, title -- 생성된 퀴즈 ID와 제목 반환
     ),
-- 생성된 퀴즈와 태그 연결
     quiz_tag_linking_advanced AS (
         INSERT INTO public.quiz_tags (quiz_id, tag_id)
             SELECT iq.id, bt.id FROM inserted_advanced_quizzes iq, backend_tag bt WHERE iq.title LIKE '백엔드%'
             UNION ALL
             SELECT iq.id, nt.id FROM inserted_advanced_quizzes iq, network_tag nt WHERE iq.title LIKE '네트워크%'
     )
-- 최종 SELECT 문 (결과 표시용, 실제 작업에는 영향 없음)
SELECT 'Advanced Quizzes and Tags inserted/linked successfully';


-- 3. 새로운 질문 추가 (고급 퀴즈별 10개씩, 총 20개)

-- 백엔드 고급 질문 (10개)
WITH quiz_info AS (
    SELECT id FROM public.quizzes WHERE title = '백엔드 전문가 도전 퀴즈' LIMIT 1
)
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds, quiz_id
)
VALUES
    (NOW(), NOW(), '데이터베이스 트랜잭션 격리 수준(Isolation Level) 중 팬텀 리드(Phantom Read)가 발생할 수 있는 가장 낮은 수준은?', 'MULTIPLE_CHOICE', 'ADVANCED', 'Repeatable Read', '["Read Uncommitted", "Read Committed", "Repeatable Read", "Serializable"]'::jsonb, 'Repeatable Read 수준에서는 한 트랜잭션 내에서 같은 쿼리를 반복 실행했을 때 다른 트랜잭션의 커밋된 변경 사항으로 인해 새로운 로우가 나타나는 팬텀 리드 현상이 발생할 수 있습니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'CAP 이론에서 분산 시스템이 동시에 보장할 수 없는 세 가지 속성 중 두 가지를 선택해야 한다면, 일반적으로 어떤 두 가지를 우선시하는 시스템이 많은가?', 'MULTIPLE_CHOICE', 'ADVANCED', '일관성(Consistency)과 가용성(Availability)', '["일관성(Consistency)과 파티션 허용성(Partition Tolerance)", "가용성(Availability)과 파티션 허용성(Partition Tolerance)", "일관성(Consistency)과 가용성(Availability)", "세 가지 모두 보장 가능"]'::jsonb, 'CAP 이론에 따르면 일관성(C), 가용성(A), 파티션 허용성(P) 중 최대 두 가지만 동시에 만족시킬 수 있습니다. 네트워크 장애(Partition)는 불가피하므로 보통 P를 기본으로 두고 C 또는 A를 선택합니다. CP(일관성 우선) 또는 AP(가용성 우선) 시스템이 일반적입니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '서킷 브레이커(Circuit Breaker) 패턴의 주요 목적은?', 'MULTIPLE_CHOICE', 'ADVANCED', '장애가 발생한 서비스로의 반복적인 호출 방지', '["API 요청 속도 제한", "데이터베이스 연결 관리", "장애가 발생한 서비스로의 반복적인 호출 방지", "마이크로서비스 간 인증 처리"]'::jsonb, '서킷 브레이커는 특정 서비스 호출이 반복적으로 실패할 경우, 해당 서비스로의 호출을 일시적으로 차단(Open)하여 시스템 전체의 연쇄적인 장애를 방지하는 패턴입니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'GraphQL과 REST API의 주요 차이점 중 하나는?', 'MULTIPLE_CHOICE', 'ADVANCED', '클라이언트가 필요한 데이터 구조를 직접 정의할 수 있다', '["GraphQL은 항상 TCP를 사용한다", "REST는 상태를 유지한다", "GraphQL은 XML 형식만 지원한다", "클라이언트가 필요한 데이터 구조를 직접 정의할 수 있다"]'::jsonb, 'GraphQL은 클라이언트가 쿼리를 통해 필요한 데이터의 형태와 종류를 명시적으로 요청할 수 있어, Over-fetching이나 Under-fetching 문제를 해결할 수 있다는 장점이 있습니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'OAuth 2.0의 "Authorization Code Grant" 흐름에서 "Authorization Code"의 역할은?', 'MULTIPLE_CHOICE', 'ADVANCED', '클라이언트가 Access Token을 얻기 위해 사용하는 임시 코드', '["사용자 인증 정보 자체", "클라이언트가 Access Token을 얻기 위해 사용하는 임시 코드", "영구적인 리소스 접근 권한", "Refresh Token과 동일"]'::jsonb, 'Authorization Code는 리소스 소유자(사용자)가 클라이언트에게 권한 부여를 승인했음을 나타내는 임시 코드입니다. 클라이언트는 이 코드를 인증 서버에 제시하여 Access Token과 교환합니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '데이터베이스 샤딩(Sharding)을 적용하는 주된 이유는?', 'MULTIPLE_CHOICE', 'ADVANCED', '대규모 데이터셋의 수평적 확장성 확보', '["데이터 백업 속도 향상", "트랜잭션 격리 수준 강화", "대규모 데이터셋의 수평적 확장성 확보", "SQL 쿼리 문법 단순화"]'::jsonb, '샤딩은 대용량 데이터를 여러 개의 작은 데이터베이스(샤드)에 분산 저장하여, 단일 데이터베이스의 성능 및 용량 한계를 극복하고 수평적 확장성을 확보하는 기법입니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '쿠버네티스(Kubernetes)에서 파드(Pod)에 대한 설명으로 올바른 것은?', 'MULTIPLE_CHOICE', 'ADVANCED', '하나 이상의 컨테이너와 공유 스토리지를 포함하는 배포 단위', '["단일 컨테이너만을 의미", "물리적인 서버 노드", "네트워크 라우팅 규칙", "하나 이상의 컨테이너와 공유 스토리지를 포함하는 배포 단위"]'::jsonb, '파드는 쿠버네티스에서 생성하고 관리할 수 있는 가장 작은 배포 단위이며, 하나 이상의 컨테이너 그룹과 이들이 공유하는 스토리지/네트워크 리소스를 포함합니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'CI/CD 파이프라인에서 CD(Continuous Deployment) 단계의 주요 목표는?', 'MULTIPLE_CHOICE', 'ADVANCED', '빌드 및 테스트가 성공한 코드를 자동으로 프로덕션 환경에 배포', '["코드 변경 사항을 자동으로 빌드", "자동화된 테스트 실행", "빌드 및 테스트가 성공한 코드를 자동으로 프로덕션 환경에 배포", "코드 정적 분석 수행"]'::jsonb, 'CD는 CI(Continuous Integration)를 통과한 코드 변경 사항을 자동으로 프로덕션 환경까지 배포하는 것을 목표로 하여, 배포 주기를 단축하고 안정성을 높입니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'Idempotent(멱등성) API 설계가 중요한 이유는?', 'MULTIPLE_CHOICE', 'ADVANCED', '네트워크 오류 등으로 동일한 요청이 여러 번 전송되어도 결과가 동일하게 보장', '["API 응답 속도 향상", "서버 리소스 사용량 감소", "네트워크 오류 등으로 동일한 요청이 여러 번 전송되어도 결과가 동일하게 보장", "데이터베이스 스키마 자동 변경"]'::jsonb, '멱등성은 동일한 요청을 한 번 보내든 여러 번 보내든 결과가 동일함을 의미합니다. 네트워크 불안정 등으로 요청이 중복될 수 있는 분산 환경에서 시스템 상태의 일관성을 유지하는 데 중요합니다 (예: PUT, DELETE).', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'OWASP Top 10 중 "Injection" 취약점에 해당하지 않는 공격 유형은?', 'MULTIPLE_CHOICE', 'ADVANCED', 'Cross-Site Request Forgery (CSRF)', '["SQL Injection", "OS Command Injection", "LDAP Injection", "Cross-Site Request Forgery (CSRF)"]'::jsonb, 'Injection 취약점은 신뢰할 수 없는 데이터가 명령어나 쿼리의 일부로 인터프리터에 전달될 때 발생합니다. SQL, OS Command, LDAP Injection 등이 대표적입니다. CSRF는 사용자가 자신의 의지와 무관하게 공격자가 의도한 행위(수정, 삭제 등)를 특정 웹사이트에 요청하게 하는 공격입니다.', 15, 60, (SELECT id FROM quiz_info));


-- 네트워크 고급 질문 (10개)
WITH quiz_info AS (
    SELECT id FROM public.quizzes WHERE title = '네트워크 심층 탐구 퀴즈' LIMIT 1
)
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds, quiz_id
)
VALUES
    (NOW(), NOW(), 'TCP 흐름 제어(Flow Control)에서 사용되는 슬라이딩 윈도우(Sliding Window) 메커니즘의 목적은?', 'MULTIPLE_CHOICE', 'ADVANCED', '수신 측의 처리 능력을 초과하지 않도록 송신 데이터 양 조절', '["네트워크 혼잡 방지", "데이터 패킷의 순서 보장", "수신 측의 처리 능력을 초과하지 않도록 송신 데이터 양 조절", "데이터 전송 오류 감지 및 재전송"]'::jsonb, '슬라이딩 윈도우는 수신 측이 현재 처리할 수 있는 데이터의 양(윈도우 크기)을 송신 측에 알려주어, 송신 측이 이 크기를 넘지 않도록 전송 속도를 조절하는 메커니즘입니다. 이는 수신 버퍼 오버플로우를 방지합니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'BGP(Border Gateway Protocol) 라우팅 프로토콜의 주요 역할은?', 'MULTIPLE_CHOICE', 'ADVANCED', '인터넷 상의 자율 시스템(AS) 간의 라우팅 정보 교환', '["동일 네트워크 내 장치 간 라우팅", "최단 경로 계산", "IP 주소 자동 할당", "인터넷 상의 자율 시스템(AS) 간의 라우팅 정보 교환"]'::jsonb, 'BGP는 인터넷의 핵심 라우팅 프로토콜로, 서로 다른 관리 도메인(자율 시스템, AS) 간에 라우팅 경로 정보를 교환하여 전 세계적인 인터넷 통신을 가능하게 합니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'VPN(Virtual Private Network)에서 사용되는 터널링 프로토콜이 아닌 것은?', 'MULTIPLE_CHOICE', 'ADVANCED', 'BGP (Border Gateway Protocol)', '["IPSec", "SSL/TLS (OpenVPN 등)", "PPTP", "BGP (Border Gateway Protocol)"]'::jsonb, 'IPSec, SSL/TLS, PPTP 등은 데이터를 캡슐화하고 암호화하여 인터넷과 같은 공용 네트워크 상에 가상의 사설 통신 채널(터널)을 만드는 데 사용됩니다. BGP는 라우팅 프로토콜입니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'QoS(Quality of Service)를 보장하기 위한 메커니즘이 아닌 것은?', 'MULTIPLE_CHOICE', 'ADVANCED', 'NAT (Network Address Translation)', '["트래픽 쉐이핑 (Traffic Shaping)", "우선순위 큐잉 (Priority Queuing)", "대역폭 예약 (Bandwidth Reservation)", "NAT (Network Address Translation)"]'::jsonb, 'QoS는 특정 애플리케이션이나 서비스에 대해 네트워크 성능(지연, 대역폭, 손실률 등)을 보장하는 기술입니다. 트래픽 쉐이핑, 우선순위 큐잉, 대역폭 예약 등이 관련 메커니즘이며, NAT는 주소 변환 기술입니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'SDN(Software-Defined Networking)의 핵심 개념은?', 'MULTIPLE_CHOICE', 'ADVANCED', '네트워크 제어 평면과 데이터 평면의 분리', '["모든 네트워크 장비의 가상화", "네트워크 제어 평면과 데이터 평면의 분리", "무선 통신 속도 향상", "IPv6 주소 체계 도입"]'::jsonb, 'SDN은 라우터나 스위치 같은 네트워크 장비의 데이터 전달 기능(데이터 평면)과 경로 설정 및 관리 기능(제어 평면)을 분리하고, 제어 평면을 소프트웨어 기반의 중앙 컨트롤러에서 관리하여 네트워크를 유연하고 효율적으로 운영하는 기술입니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'HTTP/2의 주요 개선 사항 중 하나인 멀티플렉싱(Multiplexing)이란?', 'MULTIPLE_CHOICE', 'ADVANCED', '단일 TCP 연결 상에서 여러 요청/응답을 병렬로 처리', '["모든 통신 내용을 암호화", "서버가 클라이언트에게 리소스를 미리 푸시", "단일 TCP 연결 상에서 여러 요청/응답을 병렬로 처리", "헤더 정보를 압축하여 전송"]'::jsonb, 'HTTP/1.1의 Head-of-Line Blocking 문제를 해결하기 위해, HTTP/2는 하나의 TCP 연결 내에서 여러 개의 요청과 응답 스트림을 동시에 주고받을 수 있는 멀티플렉싱 기능을 도입했습니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '애니캐스트(Anycast) 주소 지정 방식의 특징은?', 'MULTIPLE_CHOICE', 'ADVANCED', '동일한 IP 주소를 가진 여러 서버 중 가장 가까운 서버로 요청 라우팅', '["하나의 IP 주소가 여러 장치에 할당됨 (멀티캐스트)", "네트워크 내의 모든 장치에 데이터 전송 (브로드캐스트)", "동일한 IP 주소를 가진 여러 서버 중 가장 가까운 서버로 요청 라우팅", "특정 장치와 1:1 통신 (유니캐스트)"]'::jsonb, '애니캐스트는 동일한 IP 주소를 여러 지역에 분산된 서버들에 할당하고, 사용자의 요청을 네트워크 상에서 가장 가깝거나 효율적인 서버로 라우팅하는 방식입니다. 주로 DNS 서버나 CDN에서 사용됩니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'DNSSEC(DNS Security Extensions)의 주요 목적은?', 'MULTIPLE_CHOICE', 'ADVANCED', 'DNS 응답의 위변조 방지 및 무결성 보장', '["DNS 서버 간 통신 암호화", "DNS 쿼리 속도 향상", "DNS 응답의 위변조 방지 및 무결성 보장", "도메인 이름 등록 절차 간소화"]'::jsonb, 'DNSSEC는 DNS 데이터에 디지털 서명을 추가하여, DNS 스푸핑이나 캐시 포이즈닝 같은 공격으로 인한 위변조된 응답을 클라이언트가 검증하고 신뢰할 수 있도록 하는 보안 확장 기능입니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'TCP Reno와 TCP Tahoe 혼잡 제어 알고리즘의 주요 차이점은?', 'MULTIPLE_CHOICE', 'ADVANCED', 'Fast Recovery 메커니즘 유무', '["혼잡 윈도우 초기값", "Slow Start 단계 유무", "Fast Recovery 메커니즘 유무", "타임아웃 처리 방식"]'::jsonb, 'Tahoe는 타임아웃이나 3개의 중복 ACK 수신 시 혼잡 윈도우를 1로 줄이고 Slow Start를 다시 시작합니다. Reno는 3개의 중복 ACK 수신 시 혼잡 윈도우를 절반으로 줄이고 Fast Recovery 상태로 진입하여 더 빠른 회복을 시도합니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'SSL/TLS 프로토콜에서 암호화 방식, 키 교환 알고리즘 등을 협상하는 단계는?', 'MULTIPLE_CHOICE', 'ADVANCED', '핸드셰이크 (Handshake)', '["레코드 프로토콜 (Record Protocol)", "경고 프로토콜 (Alert Protocol)", "핸드셰이크 (Handshake)", "변경 암호 사양 프로토콜 (Change Cipher Spec Protocol)"]'::jsonb, 'TLS 핸드셰이크 과정에서 클라이언트와 서버는 서로 지원하는 암호화 스위트(Cipher Suite) 목록을 교환하고, 사용할 프로토콜 버전, 암호화 알고리즘, 키 교환 방식, 해시 함수 등을 협상하여 결정합니다.', 15, 60, (SELECT id FROM quiz_info));