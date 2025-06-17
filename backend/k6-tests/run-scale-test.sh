#!/bin/bash

# 캐시 스케일 테스트 실행 스크립트
# 다양한 데이터 크기별로 캐시 성능을 측정합니다.

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 테스트 데이터 크기 배열
DATA_SIZES=(10 100 500 1000)

# 결과 저장 디렉토리
RESULTS_DIR="scale_test_results"
mkdir -p ${RESULTS_DIR}

# 테스트 환경 준비 확인
echo -e "${BLUE}캐시 스케일 테스트를 시작합니다.${NC}"
echo -e "${YELLOW}주의: 이 테스트를 실행하기 전에 Spring Boot 애플리케이션이 실행 중이어야 합니다.${NC}"
echo ""

# 도커 컴포즈 상태 확인
docker-compose ps

read -p "테스트 환경이 준비되었습니까? (y/n): " ready
if [[ ! $ready =~ ^[Yy]$ ]]; then
    echo -e "${RED}테스트가 취소되었습니다.${NC}"
    exit 1
fi

# 결과 요약 파일 초기화
SUMMARY_FILE="${RESULTS_DIR}/summary.md"
echo "# 캐시 스케일 테스트 결과 요약" > ${SUMMARY_FILE}
echo "" >> ${SUMMARY_FILE}
echo "| 데이터 크기 | 퀴즈 상세 조회 (개선율) | 퀴즈 검색 (개선율) | 인기 퀴즈 추천 (개선율) | 평균 개선율 |" >> ${SUMMARY_FILE}
echo "|------------|-------------------------|-------------------|------------------------|------------|" >> ${SUMMARY_FILE}

# 각 데이터 크기별로 테스트 실행
for size in "${DATA_SIZES[@]}"; do
    echo -e "${GREEN}데이터 크기 ${size}개로 테스트 실행 중...${NC}"
    
    # 결과 파일 경로
    RESULT_FILE="${RESULTS_DIR}/size_${size}.txt"
    
    # k6 테스트 실행
    docker-compose exec -T k6 k6 run /scripts/cache-scale-test.js -e DATA_SIZE=${size} > ${RESULT_FILE}
    
    # 테스트 결과 파싱
    quiz_detail_ratio=$(grep "퀴즈 상세" ${RESULT_FILE} | awk -F '개선율=' '{sum+=$2; count++} END {if(count>0) printf "%.2f", sum/count}' | sed 's/배//')
    search_ratio=$(grep "퀴즈 검색" ${RESULT_FILE} | awk -F '개선율=' '{sum+=$2; count++} END {if(count>0) printf "%.2f", sum/count}' | sed 's/배//')
    popular_ratio=$(grep "인기 퀴즈 추천" ${RESULT_FILE} | awk -F '개선율=' '{sum+=$2; count++} END {if(count>0) printf "%.2f", sum/count}' | sed 's/배//')
    
    # 평균 개선율 계산
    avg_ratio=$(echo "scale=2; ($quiz_detail_ratio + $search_ratio + $popular_ratio) / 3" | bc)
    
    # 결과 요약에 추가
    echo "| $size | ${quiz_detail_ratio}x | ${search_ratio}x | ${popular_ratio}x | ${avg_ratio}x |" >> ${SUMMARY_FILE}
    
    echo -e "${BLUE}데이터 크기 ${size}개 테스트 완료${NC}"
    echo -e "퀴즈 상세 조회 개선율: ${quiz_detail_ratio}x"
    echo -e "퀴즈 검색 개선율: ${search_ratio}x"
    echo -e "인기 퀴즈 추천 개선율: ${popular_ratio}x"
    echo -e "평균 개선율: ${avg_ratio}x"
    echo ""
    
    # 다음 테스트 전 잠시 대기
    sleep 5
done

echo -e "${GREEN}모든 테스트가 완료되었습니다.${NC}"
echo -e "결과 요약은 ${SUMMARY_FILE} 파일에서 확인할 수 있습니다."
echo ""

# 결과 파일 보기
cat ${SUMMARY_FILE} 