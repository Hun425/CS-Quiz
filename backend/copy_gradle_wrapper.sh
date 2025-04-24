#!/bin/bash

# 프로젝트 루트 디렉토리에서 실행해야 합니다.
echo "모듈 빌드에 필요한 Gradle 파일 복사중..."

# 모든 모듈 디렉토리 순회
for module_dir in modules/*; do
  if [ -d "$module_dir" ]; then
    echo "모듈 처리중: $module_dir"
    
    # Gradle 래퍼 복사
    cp -r gradle "$module_dir/"
    cp gradlew "$module_dir/"
    cp gradlew.bat "$module_dir/"
    
    echo "$module_dir에 Gradle 래퍼 복사 완료"
  fi
done

echo "모든 모듈에 Gradle 래퍼 복사 완료"
