# k6 테스트 실행 스크립트

param (
    [string]$testFile = "cache-performance-test.js",
    [switch]$docker = $false
)

# 테스트 파일 경로 확인
$testFilePath = Join-Path -Path "." -ChildPath $testFile
if (-Not (Test-Path $testFilePath)) {
    Write-Error "테스트 파일이 존재하지 않습니다: $testFilePath"
    exit 1
}

# Docker에서 실행할지 로컬에서 실행할지 결정
if ($docker) {
    Write-Host "Docker에서 k6 테스트 실행 중: $testFile"
    
    # Docker 네트워크 설정을 위해 URL 수정
    $content = Get-Content $testFilePath -Raw
    $content = $content -replace 'const BASE_URL = ''http://127.0.0.1:8080/api'';', 'const BASE_URL = ''http://host.docker.internal:8080/api'';'
    $content = $content -replace '// const BASE_URL = ''http://host.docker.internal:8080/api'';', '// 로컬 실행 시: const BASE_URL = ''http://127.0.0.1:8080/api'';'
    $tempFile = [System.IO.Path]::GetTempFileName()
    $content | Set-Content $tempFile
    
    # Docker 실행
    docker run --rm -i `
        -v "${tempFile}:/scripts/$testFile" `
        --add-host=host.docker.internal:host-gateway `
        grafana/k6 run "/scripts/$testFile"
        
    # 임시 파일 삭제
    Remove-Item $tempFile
}
else {
    Write-Host "로컬에서 k6 테스트 실행 중: $testFile"
    
    # 로컬 URL 설정 확인
    $content = Get-Content $testFilePath -Raw
    $content = $content -replace 'const BASE_URL = ''http://host.docker.internal:8080/api'';', 'const BASE_URL = ''http://127.0.0.1:8080/api'';'
    $content = $content -replace '// const BASE_URL = ''http://127.0.0.1:8080/api'';', '// Docker 실행 시: const BASE_URL = ''http://host.docker.internal:8080/api'';'
    $tempFile = [System.IO.Path]::GetTempFileName()
    $content | Set-Content $tempFile
    
    # 로컬 k6 실행
    k6 run $tempFile
    
    # 임시 파일 삭제
    Remove-Item $tempFile
}

Write-Host "테스트 완료!" 