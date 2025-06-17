#!/bin/bash

# 최적화된 Docker 배포 스크립트
echo "🚀 최적화된 배포 시작..."

# 1. 기존 컨테이너 정리 (선택적)
read -p "기존 컨테이너를 정리하시겠습니까? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🧹 기존 컨테이너 정리 중..."
    docker-compose down
    # 사용하지 않는 이미지만 정리 (캐시 보존)
    docker image prune -f
fi

# 2. BuildKit 활성화 (더 빠른 빌드)
export DOCKER_BUILDKIT=1
export COMPOSE_DOCKER_CLI_BUILD=1

echo "⚡ BuildKit 활성화됨"

# 3. 빌드 방식 선택
read -p "캐시를 무시하고 처음부터 빌드하시겠습니까? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🔨 전체 재빌드 시작... (시간이 오래 걸립니다)"
    docker-compose build --no-cache --parallel
else
    echo "🔨 스마트 빌드 시작... (캐시 활용)"
    docker-compose build --parallel
fi

# 4. 백그라운드에서 서비스 시작
echo "🌟 서비스 시작 중..."
docker-compose up -d

# 5. 상태 확인
echo "📊 컨테이너 상태 확인..."
docker-compose ps

echo "✅ 배포 완료!"
echo "🌐 Frontend: http://$(curl -s ifconfig.me):80"
echo "🔧 Backend: http://$(curl -s ifconfig.me):8080"

# 6. 로그 확인 옵션
read -p "실시간 로그를 확인하시겠습니까? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    docker-compose logs -f
fi 