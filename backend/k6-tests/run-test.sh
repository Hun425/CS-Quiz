#!/bin/bash
# k6 테스트 실행 스크립트 (Linux/Mac OS용)

# 기본 설정
TEST_FILE=${1:-"cache-performance-test.js"}
USE_DOCKER=${2:-false}

# 테스트 파일 존재 확인
if [ ! -f "$TEST_FILE" ]; then
    echo "테스트 파일이 존재하지 않습니다: $TEST_FILE"
    exit 1
fi

# 임시 파일 생성 함수
create_temp_file() {
    local src_file=$1
    local temp_file=$(mktemp)
    
    # 파일 복사
    cp "$src_file" "$temp_file"
    
    echo "$temp_file"
}

# Docker에서 실행할지 로컬에서 실행할지 결정
if [ "$USE_DOCKER" == "true" ]; then
    echo "Docker에서 k6 테스트 실행 중: $TEST_FILE"
    
    # 임시 파일 생성
    TEMP_FILE=$(create_temp_file "$TEST_FILE")
    
    # Docker 네트워크 설정을 위해 URL 수정
    sed -i 's|const BASE_URL = '\''http://127.0.0.1:8080/api'\''|const BASE_URL = '\''http://host.docker.internal:8080/api'\''|g' "$TEMP_FILE"
    
    # Docker 실행
    docker run --rm -i \
        -v "${TEMP_FILE}:/scripts/$(basename $TEST_FILE)" \
        --add-host=host.docker.internal:host-gateway \
        grafana/k6 run "/scripts/$(basename $TEST_FILE)"
    
    # 임시 파일 삭제
    rm "$TEMP_FILE"
else
    echo "로컬에서 k6 테스트 실행 중: $TEST_FILE"
    
    # 임시 파일 생성
    TEMP_FILE=$(create_temp_file "$TEST_FILE")
    
    # 로컬 URL 설정 확인
    sed -i 's|const BASE_URL = '\''http://host.docker.internal:8080/api'\''|const BASE_URL = '\''http://127.0.0.1:8080/api'\''|g' "$TEMP_FILE"
    
    # 로컬 k6 실행
    k6 run "$TEMP_FILE"
    
    # 임시 파일 삭제
    rm "$TEMP_FILE"
fi

echo "테스트 완료!" 