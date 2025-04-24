#!/bin/bash

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=================================================${NC}"
echo -e "${GREEN}Quiz Platform 서비스 로그 확인 스크립트${NC}"
echo -e "${BLUE}=================================================${NC}"

# 현재 실행 중인 서비스 목록 표시
echo -e "${YELLOW}현재 실행 중인 서비스 목록:${NC}"
docker-compose ps

echo -e "\n${YELLOW}로그를 확인할 서비스를 선택하세요:${NC}"
echo "1) api-gateway"
echo "2) eureka-server"
echo "3) user-service"
echo "4) quiz-service"
echo "5) battle-service"
echo "6) 인프라 서비스 (postgres, redis, elasticsearch, kafka)"
echo "7) 모든 서비스 로그"
echo "8) 종료"
read -p "선택 (1-8): " service

case $service in
  1)
    echo -e "${GREEN}api-gateway 서비스의 로그를 확인합니다...${NC}"
    docker-compose logs -f api-gateway
    ;;
  2)
    echo -e "${GREEN}eureka-server 서비스의 로그를 확인합니다...${NC}"
    docker-compose logs -f eureka-server
    ;;
  3)
    echo -e "${GREEN}user-service 서비스의 로그를 확인합니다...${NC}"
    docker-compose logs -f user-service
    ;;
  4)
    echo -e "${GREEN}quiz-service 서비스의 로그를 확인합니다...${NC}"
    docker-compose logs -f quiz-service
    ;;
  5)
    echo -e "${GREEN}battle-service 서비스의 로그를 확인합니다...${NC}"
    docker-compose logs -f battle-service
    ;;
  6)
    echo -e "${GREEN}인프라 서비스 로그를 확인합니다...${NC}"
    echo -e "${YELLOW}어떤 인프라 서비스의 로그를 확인하시겠습니까?${NC}"
    echo "1) postgres"
    echo "2) redis"
    echo "3) elasticsearch"
    echo "4) zookeeper"
    echo "5) kafka"
    read -p "선택 (1-5): " infra
    
    case $infra in
      1) docker-compose logs -f postgres ;;
      2) docker-compose logs -f redis ;;
      3) docker-compose logs -f elasticsearch ;;
      4) docker-compose logs -f zookeeper ;;
      5) docker-compose logs -f kafka ;;
      *) echo -e "${YELLOW}잘못된 선택입니다.${NC}" ;;
    esac
    ;;
  7)
    echo -e "${GREEN}모든 서비스의 로그를 확인합니다...${NC}"
    docker-compose logs -f
    ;;
  8)
    echo -e "${GREEN}종료합니다.${NC}"
    exit 0
    ;;
  *)
    echo -e "${YELLOW}잘못된 선택입니다. 스크립트를 종료합니다.${NC}"
    exit 1
    ;;
esac 