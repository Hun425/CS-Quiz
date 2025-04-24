#!/bin/bash

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}=================================================${NC}"
echo -e "${GREEN}Quiz Platform 서비스 중지 스크립트${NC}"
echo -e "${BLUE}=================================================${NC}"

# 종료 모드 선택
echo -e "${YELLOW}종료 모드를 선택하세요:${NC}"
echo "1) 모든 서비스 중지 (컨테이너 유지)"
echo "2) 모든 서비스 중지 및 컨테이너 삭제"
echo "3) 모든 서비스 중지, 컨테이너 및 볼륨 삭제 (모든 데이터 삭제)"
echo "4) 취소"
read -p "선택 (1-4): " mode

case $mode in
  1)
    echo -e "${GREEN}모든 서비스를 중지합니다...${NC}"
    docker-compose stop
    echo -e "${GREEN}개발 환경 서비스도 중지합니다...${NC}"
    docker-compose -f docker-compose.dev.yml stop
    ;;
  2)
    echo -e "${YELLOW}모든 서비스를 중지하고 컨테이너를 삭제합니다...${NC}"
    docker-compose down
    echo -e "${YELLOW}개발 환경 컨테이너도 삭제합니다...${NC}"
    docker-compose -f docker-compose.dev.yml down
    ;;
  3)
    echo -e "${RED}경고: 모든 데이터가 삭제됩니다. 계속하시겠습니까? (y/n)${NC}"
    read -p "" confirm
    if [ "$confirm" = "y" ] || [ "$confirm" = "Y" ]; then
      echo -e "${RED}모든 서비스를 중지하고 컨테이너 및 볼륨을 삭제합니다...${NC}"
      docker-compose down -v
      echo -e "${RED}개발 환경 컨테이너 및 볼륨도 삭제합니다...${NC}"
      docker-compose -f docker-compose.dev.yml down -v
    else
      echo -e "${GREEN}작업이 취소되었습니다.${NC}"
      exit 0
    fi
    ;;
  4)
    echo -e "${GREEN}작업이 취소되었습니다.${NC}"
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
docker ps -a | grep "quiz-"

echo -e "\n${GREEN}작업이 완료되었습니다.${NC}" 