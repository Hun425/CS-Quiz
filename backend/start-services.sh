#!/bin/bash

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=================================================${NC}"
echo -e "${GREEN}Quiz Platform 서비스 시작 스크립트${NC}"
echo -e "${BLUE}=================================================${NC}"

# 실행 모드 선택
echo -e "${YELLOW}실행 모드를 선택하세요:${NC}"
echo "1) 개발 모드 (소스 코드 볼륨 마운트, 디버깅 포트 활성화)"
echo "2) 프로덕션 모드"
echo "3) 인프라 서비스만 실행 (DB, Redis, Kafka 등)"
echo "4) 종료"
read -p "선택 (1-4): " mode

case $mode in
  1)
    echo -e "${GREEN}개발 모드로 서비스를 시작합니다...${NC}"
    docker-compose -f docker-compose.dev.yml up -d
    ;;
  2)
    echo -e "${GREEN}프로덕션 모드로 서비스를 시작합니다...${NC}"
    docker-compose up -d
    ;;
  3)
    echo -e "${GREEN}인프라 서비스만 시작합니다...${NC}"
    docker-compose up -d postgres redis elasticsearch zookeeper kafka
    ;;
  4)
    echo -e "${YELLOW}종료합니다.${NC}"
    exit 0
    ;;
  *)
    echo -e "${YELLOW}잘못된 선택입니다. 스크립트를 종료합니다.${NC}"
    exit 1
    ;;
esac

echo -e "${BLUE}=================================================${NC}"
echo -e "${GREEN}서비스 상태 확인${NC}"
echo -e "${BLUE}=================================================${NC}"
docker-compose ps

echo -e "\n${GREEN}서비스가 시작되었습니다.${NC}"
echo -e "${YELLOW}로그를 확인하려면 다음 명령어를 사용하세요:${NC}"
echo "docker-compose logs -f [서비스명]"
echo "예: docker-compose logs -f api-gateway"