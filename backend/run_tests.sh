#!/bin/bash

# 모든 모듈 테스트 실행
./gradlew test

# 특정 모듈의 테스트만 실행
# ./gradlew :modules:user:test
# ./gradlew :modules:quiz:test
# ./gradlew :modules:battle:test
# ./gradlew :modules:common:test

# 특정 테스트 클래스만 실행
# ./gradlew :modules:user:test --tests "com.quizplatform.user.domain.UserEntityTest"
# ./gradlew :modules:common:test --tests "com.quizplatform.common.event.KafkaEventTest" 