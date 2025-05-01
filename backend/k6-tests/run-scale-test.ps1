# 캐시 스케일 테스트 실행 스크립트 (PowerShell 버전)
# 다양한 데이터 크기별로 캐시 성능을 측정합니다.

# 테스트 데이터 크기 배열
$DATA_SIZES = @(10, 100, 500, 1000)

# 결과 저장 디렉토리
$RESULTS_DIR = "scale_test_results"
if (-not (Test-Path $RESULTS_DIR)) {
    New-Item -ItemType Directory -Path $RESULTS_DIR | Out-Null
}

# 테스트 환경 준비 확인
Write-Host "캐시 스케일 테스트를 시작합니다." -ForegroundColor Blue
Write-Host "주의: 이 테스트를 실행하기 전에 Spring Boot 애플리케이션이 실행 중이어야 합니다." -ForegroundColor Yellow
Write-Host ""

# 도커 컴포즈 상태 확인
docker-compose ps

$ready = Read-Host "테스트 환경이 준비되었습니까? (y/n)"
if ($ready -notmatch "[Yy]") {
    Write-Host "테스트가 취소되었습니다." -ForegroundColor Red
    exit
}

# 결과 요약 파일 초기화
$SUMMARY_FILE = Join-Path $RESULTS_DIR "summary.md"
Set-Content -Path $SUMMARY_FILE -Value "# 캐시 스케일 테스트 결과 요약"
Add-Content -Path $SUMMARY_FILE -Value ""
Add-Content -Path $SUMMARY_FILE -Value "| 데이터 크기 | 퀴즈 상세 조회 (개선율) | 퀴즈 검색 (개선율) | 인기 퀴즈 추천 (개선율) | 평균 개선율 |"
Add-Content -Path $SUMMARY_FILE -Value "|------------|-------------------------|-------------------|------------------------|------------|"

# 각 데이터 크기별로 테스트 실행
foreach ($size in $DATA_SIZES) {
    Write-Host "데이터 크기 $size개로 테스트 실행 중..." -ForegroundColor Green
    
    # 결과 파일 경로
    $RESULT_FILE = Join-Path $RESULTS_DIR "size_$size.txt"
    
    # k6 테스트 실행
    docker-compose exec k6 k6 run /scripts/cache-scale-test.js -e DATA_SIZE=$size | Out-File -FilePath $RESULT_FILE
    
    # 테스트 결과 파싱
    $content = Get-Content $RESULT_FILE
    
    # 퀴즈 상세 조회 개선율 계산
    $quiz_detail_lines = $content | Where-Object { $_ -match "퀴즈 상세" }
    $quiz_detail_ratios = @()
    foreach ($line in $quiz_detail_lines) {
        if ($line -match "개선율=(\d+\.\d+)배") {
            $quiz_detail_ratios += [double]$Matches[1]
        }
    }
    $quiz_detail_ratio = if ($quiz_detail_ratios.Count -gt 0) { 
        "{0:N2}" -f ($quiz_detail_ratios | Measure-Object -Average).Average 
    } else { "N/A" }
    
    # 퀴즈 검색 개선율 계산
    $search_lines = $content | Where-Object { $_ -match "퀴즈 검색" }
    $search_ratios = @()
    foreach ($line in $search_lines) {
        if ($line -match "개선율=(\d+\.\d+)배") {
            $search_ratios += [double]$Matches[1]
        }
    }
    $search_ratio = if ($search_ratios.Count -gt 0) { 
        "{0:N2}" -f ($search_ratios | Measure-Object -Average).Average 
    } else { "N/A" }
    
    # 인기 퀴즈 추천 개선율 계산
    $popular_lines = $content | Where-Object { $_ -match "인기 퀴즈 추천" }
    $popular_ratios = @()
    foreach ($line in $popular_lines) {
        if ($line -match "개선율=(\d+\.\d+)배") {
            $popular_ratios += [double]$Matches[1]
        }
    }
    $popular_ratio = if ($popular_ratios.Count -gt 0) { 
        "{0:N2}" -f ($popular_ratios | Measure-Object -Average).Average 
    } else { "N/A" }
    
    # 평균 개선율 계산
    $avg_values = @()
    if ($quiz_detail_ratio -ne "N/A") { $avg_values += [double]$quiz_detail_ratio }
    if ($search_ratio -ne "N/A") { $avg_values += [double]$search_ratio }
    if ($popular_ratio -ne "N/A") { $avg_values += [double]$popular_ratio }
    
    $avg_ratio = if ($avg_values.Count -gt 0) { 
        "{0:N2}" -f ($avg_values | Measure-Object -Average).Average 
    } else { "N/A" }
    
    # 결과 요약에 추가
    Add-Content -Path $SUMMARY_FILE -Value "| $size | ${quiz_detail_ratio}x | ${search_ratio}x | ${popular_ratio}x | ${avg_ratio}x |"
    
    Write-Host "데이터 크기 $size개 테스트 완료" -ForegroundColor Blue
    Write-Host "퀴즈 상세 조회 개선율: ${quiz_detail_ratio}x"
    Write-Host "퀴즈 검색 개선율: ${search_ratio}x"
    Write-Host "인기 퀴즈 추천 개선율: ${popular_ratio}x"
    Write-Host "평균 개선율: ${avg_ratio}x"
    Write-Host ""
    
    # 다음 테스트 전 잠시 대기
    Start-Sleep -Seconds 5
}

Write-Host "모든 테스트가 완료되었습니다." -ForegroundColor Green
Write-Host "결과 요약은 $SUMMARY_FILE 파일에서 확인할 수 있습니다."
Write-Host ""

# 결과 파일 보기
Get-Content $SUMMARY_FILE 