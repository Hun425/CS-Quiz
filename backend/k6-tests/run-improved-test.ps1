# 캐시 성능 개선 테스트 실행 스크립트
# 사용법: .\run-improved-test.ps1

# Spring 애플리케이션이 실행 중인지 확인
$springAppRunning = $true
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -Method Get -ErrorAction Stop
    if ($response.StatusCode -ne 200) {
        $springAppRunning = $false
    }
} catch {
    $springAppRunning = $false
}

if (-not $springAppRunning) {
    Write-Host "경고: Spring 애플리케이션이 실행 중이지 않습니다. 먼저 애플리케이션을 실행해 주세요." -ForegroundColor Yellow
    exit 1
}

# 환경 변수 설정 (Docker가 없는 경우 직접 로컬 테스트용)
$env:K6_WEB_DASHBOARD = "true"

# 현재 날짜와 시간으로 결과 폴더 생성
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$resultsDir = "results_$timestamp"
New-Item -Path $resultsDir -ItemType Directory -Force | Out-Null

Write-Host "캐시 테스트를 시작합니다. 결과는 $resultsDir 폴더에 저장됩니다." -ForegroundColor Cyan

# 실행 안내
Write-Host "1. 애플리케이션 프로필을 'debug'로 설정했는지 확인하세요." -ForegroundColor Green
Write-Host "2. k6 테스트가 끝난 후 application-debug.yml의 로그를 확인하세요." -ForegroundColor Green

# k6 설치 여부 확인
$k6Installed = $null
try {
    $k6Installed = Get-Command k6 -ErrorAction SilentlyContinue
} catch {
    $k6Installed = $null
}

if ($k6Installed -eq $null) {
    Write-Host "k6가 설치되어 있지 않습니다. Docker를 사용하여 테스트를 실행합니다." -ForegroundColor Yellow
    
    # Docker가 설치되어 있는지 확인
    $dockerInstalled = $null
    try {
        $dockerInstalled = Get-Command docker -ErrorAction SilentlyContinue
    } catch {
        $dockerInstalled = $null
    }
    
    if ($dockerInstalled -eq $null) {
        Write-Host "ERROR: Docker가 설치되어 있지 않습니다. k6 또는 Docker를 설치한 후 다시 시도하세요." -ForegroundColor Red
        exit 1
    }
    
    # Docker를 사용하여 테스트 실행
    docker run -i --rm -v ${PWD}:/scripts grafana/k6 run /scripts/improved-cache-test.js --out json=/scripts/$resultsDir/results.json
} else {
    # 로컬 k6를 사용하여 테스트 실행
    k6 run improved-cache-test.js --out json=$resultsDir/results.json
}

Write-Host "테스트가 완료되었습니다. 결과는 $resultsDir 폴더에 저장되었습니다." -ForegroundColor Green
Write-Host "결과를 확인한 후, 애플리케이션 로그에서 캐시 동작을 분석해보세요." -ForegroundColor Cyan 