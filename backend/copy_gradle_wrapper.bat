@echo off
echo 모듈 빌드에 필요한 Gradle 파일 복사중...

rem 모든 모듈 디렉토리 순회
for /d %%d in (modules\*) do (
    echo 모듈 처리중: %%d
    
    rem Gradle 래퍼 복사
    xcopy /E /I /Y gradle "%%d\gradle"
    copy /Y gradlew "%%d\"
    copy /Y gradlew.bat "%%d\"
    
    echo %%d에 Gradle 래퍼 복사 완료
)

echo 모든 모듈에 Gradle 래퍼 복사 완료
