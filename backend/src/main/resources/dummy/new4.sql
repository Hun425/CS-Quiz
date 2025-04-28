-- 새로운 더미 데이터 추가 (인프라/쿠버네티스 초급, 중급, 고급)

-- 1. 필요한 태그 추가 (쿠버네티스)
INSERT INTO public.tags (created_at, name, description)
VALUES
    (NOW(), '쿠버네티스', '쿠버네티스(Kubernetes) 컨테이너 오케스트레이션 플랫폼 관련 개념, 오브젝트, 운영')
ON CONFLICT (name) DO NOTHING; -- 이미 존재하면 추가하지 않음

-- (선택) 쿠버네티스 태그를 데브옵스 또는 시스템설계 태그의 하위 태그로 설정할 경우
-- WITH parent_tag AS (SELECT id FROM public.tags WHERE name = '데브옵스'), -- 또는 '시스템설계'
--      k8s_tag_update AS (UPDATE public.tags SET parent_id = (SELECT id FROM parent_tag) WHERE name = '쿠버네티스' AND parent_id IS NULL)
-- SELECT 'Kubernetes tag parent updated if necessary';

-- 2. 새로운 퀴즈 추가 (쿠버네티스 초급, 중급, 고급 - 각 1개씩, 총 3개 퀴즈)

-- 관리자 ID 및 쿠버네티스 태그 ID 가져오기
WITH admin_user AS (
    SELECT id FROM public.users WHERE role = 'ADMIN' LIMIT 1
), k8s_tag AS (
    SELECT id FROM public.tags WHERE name = '쿠버네티스'
),
-- 쿠버네티스 퀴즈 생성
     inserted_k8s_quizzes AS (
         INSERT INTO public.quizzes (
                                     created_at, updated_at, title, description, difficulty_level,
                                     is_public, question_count, quiz_type, time_limit,
                                     creator_id, attempt_count, avg_score, view_count, valid_until
             )
             VALUES
                 -- 쿠버네티스 초급
                 (NOW() - INTERVAL '2 days', NOW(), '쿠버네티스 기초 이해하기', '컨테이너, 파드, 노드, 클러스터 등 쿠버네티스 기본 개념과 용어를 묻습니다.', 'BEGINNER',
                  true, 10, 'REGULAR', 25, (SELECT id FROM admin_user), floor(random() * 70 + 15), random() * 30 + 60, floor(random() * 400 + 50), NULL),
                 -- 쿠버네티스 중급
                 (NOW() - INTERVAL '1 day', NOW(), '쿠버네티스 핵심 오브젝트 활용', '디플로이먼트, 서비스, 네임스페이스, 볼륨 등 주요 오브젝트 사용법을 다룹니다.', 'INTERMEDIATE',
                  true, 10, 'REGULAR', 35, (SELECT id FROM admin_user), floor(random() * 55 + 10), random() * 25 + 65, floor(random() * 320 + 40), NULL),
                 -- 쿠버네티스 고급
                 (NOW(), NOW(), '쿠버네티스 심화 및 운영 전략', '헬름, 인그레스, RBAC, 컨트롤 플레인 등 고급 개념과 운영 노하우를 평가합니다.', 'ADVANCED',
                  true, 10, 'REGULAR', 45, (SELECT id FROM admin_user), floor(random() * 45 + 5), random() * 15 + 70, floor(random() * 250 + 30), NULL)
             RETURNING id, title -- 생성된 퀴즈 ID와 제목 반환
     ),
-- 생성된 퀴즈와 태그 연결
     quiz_tag_linking_k8s AS (
         INSERT INTO public.quiz_tags (quiz_id, tag_id)
             SELECT iq.id, kt.id FROM inserted_k8s_quizzes iq, k8s_tag kt WHERE iq.title LIKE '쿠버네티스%'
     )
-- 최종 SELECT 문 (결과 표시용, 실제 작업에는 영향 없음)
SELECT 'Kubernetes Quizzes and Tags inserted/linked successfully';


-- 3. 새로운 질문 추가 (쿠버네티스 퀴즈별 10개씩, 총 30개)

-- 쿠버네티스 초급 질문 (10개)
WITH quiz_info AS (
    SELECT id FROM public.quizzes WHERE title = '쿠버네티스 기초 이해하기' LIMIT 1
)
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds, quiz_id
)
VALUES
    (NOW(), NOW(), '컨테이너(Container) 기술의 주요 장점이 아닌 것은?', 'MULTIPLE_CHOICE', 'BEGINNER', '운영체제 커널 제공', '["애플리케이션 격리", "빠른 배포 및 확장", "운영체제 커널 제공", "환경 일관성 유지"]'::jsonb, '컨테이너는 호스트 OS의 커널을 공유하며, 자체적인 OS 커널을 포함하지 않습니다. 이는 가상 머신(VM)과의 주요 차이점 중 하나입니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '쿠버네티스(Kubernetes)란 무엇인가요?', 'MULTIPLE_CHOICE', 'BEGINNER', '컨테이너화된 애플리케이션의 배포, 확장, 관리를 자동화하는 시스템', '["컨테이너 이미지를 만드는 도구", "리눅스 배포판의 일종", "컨테이너화된 애플리케이션의 배포, 확장, 관리를 자동화하는 시스템", "클라우드 가상 머신 서비스"]'::jsonb, '쿠버네티스는 컨테이너 오케스트레이션 플랫폼으로, 컨테이너 기반 애플리케이션의 라이프사이클 관리를 자동화합니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '쿠버네티스에서 배포할 수 있는 가장 작은 단위는 무엇인가요?', 'MULTIPLE_CHOICE', 'BEGINNER', '파드 (Pod)', '["컨테이너 (Container)", "파드 (Pod)", "노드 (Node)", "서비스 (Service)"]'::jsonb, '파드는 하나 이상의 컨테이너 그룹과 이들이 공유하는 스토리지/네트워크 리소스를 포함하는 쿠버네티스의 가장 기본적인 배포 단위입니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '쿠버네티스 클러스터에서 워커 머신(Worker Machine)을 무엇이라고 부르나요?', 'MULTIPLE_CHOICE', 'BEGINNER', '노드 (Node)', '["마스터 (Master)", "노드 (Node)", "파드 (Pod)", "클러스터 (Cluster)"]'::jsonb, '노드는 쿠버네티스 클러스터에서 실제로 컨테이너(파드)가 실행되는 워커 머신(물리 서버 또는 가상 머신)을 의미합니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '쿠버네티스 오브젝트의 상태를 확인하는 가장 기본적인 `kubectl` 명령어는?', 'MULTIPLE_CHOICE', 'BEGINNER', 'kubectl get', '["kubectl create", "kubectl apply", "kubectl delete", "kubectl get"]'::jsonb, '`kubectl get <오브젝트 타입> [오브젝트 이름]` 명령어는 지정한 쿠버네티스 오브젝트의 요약된 상태 정보를 조회합니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '쿠버네티스에서 애플리케이션의 배포 및 업데이트를 선언적으로 관리하는 오브젝트는?', 'MULTIPLE_CHOICE', 'BEGINNER', '디플로이먼트 (Deployment)', '["서비스 (Service)", "파드 (Pod)", "디플로이먼트 (Deployment)", "네임스페이스 (Namespace)"]'::jsonb, '디플로이먼트는 파드와 레플리카셋(ReplicaSet)에 대한 선언적 업데이트를 제공하여, 애플리케이션의 배포, 롤링 업데이트, 롤백 등을 관리합니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '쿠버네티스 클러스터 내부에서 파드 그룹에 접근할 수 있는 안정적인 IP 주소와 포트를 제공하는 오브젝트는?', 'MULTIPLE_CHOICE', 'BEGINNER', '서비스 (Service)', '["인그레스 (Ingress)", "서비스 (Service)", "엔드포인트 (Endpoint)", "네트워크 폴리시 (Network Policy)"]'::jsonb, '서비스는 특정 레이블을 가진 파드 그룹에 대한 고정적인 접근 지점(가상 IP 주소 및 포트)을 제공하여, 파드가 생성되거나 삭제되어도 서비스 이름이나 IP를 통해 안정적으로 접근할 수 있게 합니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '쿠버네티스 오브젝트의 상세 정보를 확인하는 `kubectl` 명령어는?', 'MULTIPLE_CHOICE', 'BEGINNER', 'kubectl describe', '["kubectl logs", "kubectl exec", "kubectl describe", "kubectl config"]'::jsonb, '`kubectl describe <오브젝트 타입> <오브젝트 이름>` 명령어는 오브젝트의 생성 정보, 상태, 이벤트 등 자세한 정보를 보여줍니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '컨테이너 이미지를 저장하고 관리하는 레지스트리(Registry)의 예시가 아닌 것은?', 'MULTIPLE_CHOICE', 'BEGINNER', 'Git', '["Docker Hub", "Amazon ECR", "Google GCR", "Git"]'::jsonb, 'Docker Hub, ECR, GCR 등은 컨테이너 이미지를 저장하는 레지스트리 서비스입니다. Git은 소스 코드 버전 관리 시스템입니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '쿠버네티스 클러스터를 구성하는 두 가지 주요 역할의 노드는 무엇인가요?', 'MULTIPLE_CHOICE', 'BEGINNER', '컨트롤 플레인 노드와 워커 노드', '["마스터 노드와 슬레이브 노드", "API 서버 노드와 데이터베이스 노드", "컨트롤 플레인 노드와 워커 노드", "프록시 노드와 스케줄러 노드"]'::jsonb, '쿠버네티스 클러스터는 클러스터 전체를 관리하는 컨트롤 플레인(마스터) 노드들과 실제 애플리케이션(파드)을 실행하는 워커 노드들로 구성됩니다.', 5, 30, (SELECT id FROM quiz_info));

-- 쿠버네티스 중급 질문 (10개)
WITH quiz_info AS (
    SELECT id FROM public.quizzes WHERE title = '쿠버네티스 핵심 오브젝트 활용' LIMIT 1
)
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds, quiz_id
)
VALUES
    (NOW(), NOW(), '쿠버네티스에서 상태를 가지는(Stateful) 애플리케이션을 배포하는 데 적합한 워크로드 오브젝트는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'StatefulSet', '["Deployment", "ReplicaSet", "DaemonSet", "StatefulSet"]'::jsonb, 'StatefulSet은 각 파드가 고유하고 안정적인 네트워크 식별자(이름)와 영구적인 스토리지를 가지도록 보장하여, 데이터베이스와 같이 상태 유지가 중요한 애플리케이션 배포에 사용됩니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '쿠버네티스 클러스터의 모든 (또는 특정) 노드에 파드를 하나씩 실행시키는 데 사용되는 워크로드 오브젝트는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'DaemonSet', '["Deployment", "DaemonSet", "Job", "CronJob"]'::jsonb, 'DaemonSet은 클러스터의 각 노드마다 특정 파드(예: 로그 수집 에이전트, 모니터링 에이전트)가 항상 실행되도록 보장합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '쿠버네티스 서비스(Service) 타입 중 클러스터 외부에서 노드의 IP와 특정 포트를 통해 파드에 접근할 수 있게 하는 타입은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'NodePort', '["ClusterIP", "NodePort", "LoadBalancer", "ExternalName"]'::jsonb, 'NodePort 타입 서비스는 각 노드의 지정된 포트로 들어오는 요청을 해당 서비스를 통해 파드로 전달합니다. 클러스터 외부에서 테스트하거나 직접 접근해야 할 때 사용될 수 있습니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '쿠버네티스 클러스터 내에서 리소스를 논리적으로 분리하고 관리하기 위한 가상 클러스터 단위는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'Namespace', '["Label", "Annotation", "Namespace", "Context"]'::jsonb, 'Namespace는 하나의 쿠버네티스 클러스터 내에서 여러 팀이나 프로젝트가 리소스를 격리하여 사용할 수 있도록 논리적인 그룹을 제공합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '쿠버네티스 파드에서 환경 변수나 설정 파일을 분리하여 관리하기 위해 사용하는 오브젝트는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'ConfigMap', '["Secret", "ConfigMap", "Volume", "ResourceQuota"]'::jsonb, 'ConfigMap은 애플리케이션 설정 정보를 키-값 쌍의 형태로 저장하고, 이를 파드에서 환경 변수나 볼륨 마운트를 통해 사용할 수 있게 합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '쿠버네티스에서 비밀번호, API 키 등 민감한 데이터를 저장하고 관리하는 데 사용되는 오브젝트는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'Secret', '["ConfigMap", "Secret", "SecurityContext", "NetworkPolicy"]'::jsonb, 'Secret은 ConfigMap과 유사하지만, Base64 인코딩 등을 통해 민감한 정보를 저장하고 파드에 안전하게 전달하는 데 사용됩니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '쿠버네티스에서 파드가 사용할 영구적인 저장 공간을 요청하는 오브젝트는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'PersistentVolumeClaim (PVC)', '["PersistentVolume (PV)", "StorageClass", "PersistentVolumeClaim (PVC)", "Volume"]'::jsonb, 'PVC는 사용자가 필요한 스토리지의 용량, 접근 모드 등을 명시하여 클러스터에 요청하는 오브젝트입니다. 관리자는 PV를 미리 생성하거나 동적 프로비저닝을 통해 이 요청을 만족시키는 스토리지를 제공합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '쿠버네티스 디플로이먼트가 원하는 수의 파드 복제본(Replica)을 유지하도록 관리하는 기본 컨트롤러는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'ReplicaSet', '["ReplicationController", "ReplicaSet", "Deployment", "PodController"]'::jsonb, 'ReplicaSet은 지정된 수의 파드 복제본이 항상 실행되도록 보장하는 역할을 합니다. 일반적으로 사용자는 직접 ReplicaSet을 관리하기보다 Deployment를 통해 간접적으로 사용합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '쿠버네티스 스케줄러가 파드를 특정 노드에 배치할 때 고려하는 요소가 아닌 것은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '파드의 실행 시간', '["노드의 리소스 요구량 (CPU, Memory)", "노드 셀렉터 (Node Selector) / 어피니티 (Affinity)", "테인트 (Taint) 와 톨러레이션 (Toleration)", "파드의 실행 시간"]'::jsonb, '쿠버네티스 스케줄러는 파드의 리소스 요구사항, 노드의 가용 리소스, 노드 레이블/셀렉터, 어피니티/안티-어피니티 규칙, 테인트/톨러레이션 등을 고려하여 최적의 노드를 선택합니다. 파드의 예상 실행 시간은 직접적인 고려 대상이 아닙니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '쿠버네티스 서비스 타입 중 클라우드 제공업체의 로드 밸런서를 프로비저닝하여 외부 트래픽을 서비스로 전달하는 타입은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'LoadBalancer', '["ClusterIP", "NodePort", "LoadBalancer", "ExternalName"]'::jsonb, 'LoadBalancer 타입 서비스는 클라우드 환경(AWS, GCP, Azure 등)에서 외부 로드 밸런서를 자동으로 생성하고 이를 통해 외부 인터넷 트래픽을 클러스터 내부의 파드로 전달합니다.', 10, 45, (SELECT id FROM quiz_info));

-- 쿠버네티스 고급 질문 (10개)
WITH quiz_info AS (
    SELECT id FROM public.quizzes WHERE title = '쿠버네티스 심화 및 운영 전략' LIMIT 1
)
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds, quiz_id
)
VALUES
    (NOW(), NOW(), '쿠버네티스 애플리케이션 패키지 관리 도구로, 차트(Chart)라는 단위로 애플리케이션과 의존성을 정의하고 배포하는 것은?', 'MULTIPLE_CHOICE', 'ADVANCED', 'Helm', '["kubectl", "Kustomize", "Helm", "Skaffold"]'::jsonb, 'Helm은 쿠버네티스용 패키지 매니저로, 차트라는 템플릿 기반 패키지를 사용하여 복잡한 애플리케이션의 설치, 업그레이드, 관리를 용이하게 합니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '쿠버네티스 클러스터 외부에서 들어오는 HTTP/HTTPS 트래픽을 내부 서비스로 라우팅하는 규칙을 정의하는 오브젝트는?', 'MULTIPLE_CHOICE', 'ADVANCED', 'Ingress', '["Service (LoadBalancer)", "Ingress", "API Gateway", "NetworkPolicy"]'::jsonb, 'Ingress는 클러스터 외부의 요청을 받아 호스트 이름이나 경로(path)에 따라 적절한 내부 서비스로 전달하는 L7 라우팅 규칙을 정의합니다. 실제로 동작하려면 Ingress Controller가 필요합니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '쿠버네티스 클러스터 내 파드 간의 네트워크 통신을 제어(허용/차단)하는 데 사용되는 오브젝트는?', 'MULTIPLE_CHOICE', 'ADVANCED', 'NetworkPolicy', '["SecurityGroup", "FirewallRule", "NetworkPolicy", "Service"]'::jsonb, 'NetworkPolicy는 파드 수준에서 네트워크 트래픽 흐름을 정의하는 방화벽 규칙과 유사한 기능을 제공하여, 특정 파드 그룹이 다른 파드 그룹과 통신할 수 있는지 여부를 제어합니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '쿠버네티스 RBAC(Role-Based Access Control)에서 클러스터 전체 범위의 권한을 정의하는 데 사용되는 오브젝트는?', 'MULTIPLE_CHOICE', 'ADVANCED', 'ClusterRole', '["Role", "ClusterRole", "RoleBinding", "ServiceAccount"]'::jsonb, 'Role은 특정 네임스페이스 내의 리소스에 대한 권한을 정의하지만, ClusterRole은 네임스페이스에 상관없이 클러스터 전체 범위의 리소스(예: 노드)나 모든 네임스페이스의 특정 리소스에 대한 권한을 정의합니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '쿠버네티스 API를 확장하여 사용자 정의 리소스를 만들고 관리할 수 있게 하는 메커니즘은?', 'MULTIPLE_CHOICE', 'ADVANCED', 'Custom Resource Definition (CRD)', '["Annotation", "Label", "Custom Controller", "Custom Resource Definition (CRD)"]'::jsonb, 'CRD를 사용하면 쿠버네티스 API에 사용자가 정의한 새로운 종류의 리소스 타입을 등록할 수 있습니다. 이후 이 커스텀 리소스를 관리하는 로직(Operator)을 구현할 수 있습니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '쿠버네티스에서 CRD와 커스텀 컨트롤러를 사용하여 특정 애플리케이션의 운영 지식(Operational Knowledge)을 자동화하는 패턴은?', 'MULTIPLE_CHOICE', 'ADVANCED', 'Operator Pattern', '["Sidecar Pattern", "Adapter Pattern", "Operator Pattern", "Ambassador Pattern"]'::jsonb, 'Operator 패턴은 특정 애플리케이션(예: 데이터베이스)의 설치, 설정, 백업, 업그레이드 등 운영 작업을 자동화하기 위해 CRD와 이를 관리하는 커스텀 컨트롤러(Operator)를 함께 사용하는 방식입니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '쿠버네티스 컨트롤 플레인의 핵심 구성 요소로, 클러스터의 모든 상태 정보를 저장하는 분산 키-값 저장소는?', 'MULTIPLE_CHOICE', 'ADVANCED', 'etcd', '["kube-apiserver", "kube-scheduler", "etcd", "kube-controller-manager"]'::jsonb, 'etcd는 쿠버네티스 클러스터의 모든 설정 데이터, 상태 정보, 오브젝트 정의 등을 저장하는 일관성 있고 고가용성을 지원하는 분산 키-값 저장소입니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '쿠버네티스 컨트롤 플레인 구성 요소 중 API 요청을 검증하고 처리하며, 클러스터 상태를 변경하는 유일한 창구 역할을 하는 것은?', 'MULTIPLE_CHOICE', 'ADVANCED', 'kube-apiserver', '["etcd", "kube-apiserver", "kube-scheduler", "cloud-controller-manager"]'::jsonb, 'kube-apiserver는 쿠버네티스 API의 프론트엔드 역할을 하며, 모든 내부/외부 통신(kubectl, 컨트롤러, kubelet 등)의 중심 허브 역할을 수행합니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '쿠버네티스 워커 노드에서 파드 내 컨테이너의 시작/중지 등 라이프사이클을 관리하고 노드의 상태를 컨트롤 플레인에 보고하는 에이전트는?', 'MULTIPLE_CHOICE', 'ADVANCED', 'kubelet', '["kube-proxy", "container runtime (Docker/containerd)", "kubelet", "CNI plugin"]'::jsonb, 'kubelet은 각 워커 노드에서 실행되며, 컨트롤 플레인(kube-apiserver)으로부터 파드 명세(PodSpec)를 받아 컨테이너 런타임을 통해 컨테이너를 관리하고 노드 상태를 주기적으로 보고합니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '쿠버네티스 워커 노드에서 서비스(Service) 개념을 구현하고 네트워크 규칙(iptables 등)을 관리하여 파드로 트래픽을 전달하는 역할을 하는 컴포넌트는?', 'MULTIPLE_CHOICE', 'ADVANCED', 'kube-proxy', '["kubelet", "CNI plugin", "CoreDNS", "kube-proxy"]'::jsonb, 'kube-proxy는 각 노드에서 실행되는 네트워크 프록시로, 쿠버네티스 서비스(Service)의 가상 IP로 오는 트래픽을 해당 서비스에 속한 실제 파드로 전달(로드 밸런싱)하는 네트워크 규칙을 관리합니다.', 15, 60, (SELECT id FROM quiz_info));