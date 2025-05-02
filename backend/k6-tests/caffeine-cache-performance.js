import http from 'k6/http';
import { sleep, check, group } from 'k6';
import { Trend, Rate, Counter, Gauge } from 'k6/metrics';
import { SharedArray } from 'k6/data';
import encoding from 'k6/encoding';

// 성능 메트릭 정의
const quizDetailTrend = new Trend('quiz_detail_response_time');
const quizSearchTrend = new Trend('quiz_search_response_time');
const comprehensiveTrend = new Trend('comprehensive_test_response_time');

const failRate = new Rate('request_fail_rate');
const cacheHitRate = new Rate('cache_hit_rate');
const firstRequestTrend = new Trend('first_request_response_time');  // 캐시 미스 시 응답 시간
const secondRequestTrend = new Trend('second_request_response_time');  // 캐시 히트 시 응답 시간
const cacheBenefitRatio = new Trend('cache_benefit_ratio');  // 캐시 히트 대비 미스의 성능 비율

const totalRequests = new Counter('total_requests');
const caffeineMisses = new Counter('caffeine_cache_misses');
const caffeineHits = new Counter('caffeine_cache_hits');
const cacheHitCount = new Counter('cache_hit_count');
const cacheMissCount = new Counter('cache_miss_count');

// API 엔드포인트 설정
// const BASE_URL = 'http://127.0.0.1:8080/api';
// Docker 환경에서 실행하는 경우 아래 URL 사용
const BASE_URL = 'http://host.docker.internal:8080/api';

// 테스트 데이터 (실제 환경에 맞게 수정 필요)
const quizIds = new SharedArray('quizIDs', function() {
  return [9001, 9002, 9003, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
});

const tagIds = new SharedArray('tagIDs', function() {
  return [1, 2, 3, 4, 5];
});

const difficulties = new SharedArray('difficulties', function() {
  return ['BEGINNER', 'INTERMEDIATE', 'ADVANCED'];
});

const searchKeywords = new SharedArray('searchKeywords', function() {
  return ['자바', '파이썬', '알고리즘', '데이터베이스', '스프링'];
});

// 인증 정보 설정
let authToken;

// 테스트 설정 최적화
export const options = {
  // 점진적인 단계적 부하 증가로 변경
  stages: [
    // 웜업: 사용자 0 -> 10명, 30초 동안
    { duration: '30s', target: 10 },
    // 부하 테스트: 사용자 10 -> 30명, 1분 동안
    { duration: '1m', target: 30 },
    // 피크 부하: 사용자 30명 유지, 1분 30초 동안
    { duration: '1m30s', target: 30 },
    // 감소: 사용자 30 -> 0명, 30초 동안
    { duration: '30s', target: 0 },
  ],
  // 임계값 설정 유지
  thresholds: {
    'quiz_detail_response_time': ['p(95)<150'],
    'quiz_search_response_time': ['p(95)<200'],
    'request_fail_rate': ['rate<0.01'],
    'second_request_response_time': ['p(95)<50'],
    'cache_hit_rate': ['rate>0.6'], // 캐시 히트율 60% 이상 기대
  },
  // 타임아웃과 연결 풀 세부 설정 추가
  httpDebug: 'full', // 문제 해결을 위한 디버그 로그 활성화
  insecureSkipTLSVerify: true,
  // 요청 타임아웃 설정
  timeout: '10s',
};

// 테스트 초기화 - 로그인 및 인증 토큰 획득
export function setup() {
  console.log('테스트 초기화: 테스트 토큰 API 호출');
  
  // 테스트 토큰 API 호출 (TestAuthController의 토큰 발급 API)
  const tokenResponse = http.post(`${BASE_URL}/test-auth/token`, JSON.stringify({
    username: 'k6tester'  // 테스트용 계정 (TestDataInitializer에서 생성됨)
  }), {
    headers: {
      'Content-Type': 'application/json',
    },
  });
  
  console.log(`토큰 API 응답: ${tokenResponse.status}`);
  
  // 응답 처리
  if (tokenResponse.status === 200) {
    try {
      const responseBody = JSON.parse(tokenResponse.body);
      // 응답 구조에 따라 토큰 추출
      authToken = responseBody.data.accessToken || responseBody.data.token;
      console.log('인증 토큰 획득 성공');
      return { authToken: authToken };
    } catch (e) {
      console.error('토큰 파싱 실패:', e);
      // 파싱 실패 시 고정 토큰 사용
      authToken = null;
    }
  } else {
    console.error('토큰 API 실패:', tokenResponse.status, tokenResponse.body);
    // 로그인 실패 시 일반 테스트는 진행 (인증이 필요한 API는 스킵)
    authToken = null;
  }
  
  return { authToken: authToken };
}

// 인증 헤더 추가
function getHeaders(data) {
  const token = data?.authToken || authToken;
  
  const headers = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
    'Connection': 'keep-alive',
    'User-Agent': 'k6-performance-test',
  };
  
  // 토큰이 있으면 추가
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  
  return headers;
}

// 검색 키워드 인코딩 함수
function encodeSearchKeyword(keyword) {
  try {
    return encodeURIComponent(keyword);
  } catch (e) {
    console.error(`키워드 인코딩 오류: ${e}`);
    return keyword;
  }
}

// 캐시 상태 확인 헬퍼 함수 (모든 테스트에서 공통으로 사용)
function checkCacheHit(response) {
  // 응답이나 헤더가 없으면 캐시 미스로 간주
  if (!response || !response.headers) {
    // console.log(`캐시 체크 불가: 헤더 없음`); // 디버깅 시에만 활성화
    return false;
  }

  // 더 자세한 헤더 디버깅 수행
  const headers = response.headers;

  // 캐시 상태를 확인할 수 있는 모든 가능한 헤더 이름
  const possibleCacheHeaders = [
    'x-cache-status',
    'X-Cache-Status',
    'x-cache',
    'X-Cache'
  ];

  // 모든 가능한 헤더 이름 확인
  let cacheStatus = '';
  let foundHeader = '';

  for (const headerName of possibleCacheHeaders) {
    if (headers[headerName]) {
      cacheStatus = headers[headerName].toUpperCase();
      foundHeader = headerName;
      break;
    }
  }

  // 캐시 상태 로그 (디버깅용)
  // if (__ITER < 5) {
  //   console.log(`[${response.request.method} ${response.request.url}] Cache header '${foundHeader}': ${cacheStatus}`);
  // }

  // HIT, PARTIAL_HIT, OK 등 다양한 캐시 히트 상태 지원
  const hitStatuses = ['HIT', 'PARTIAL_HIT', 'OK'];
  return hitStatuses.includes(cacheStatus) || cacheStatus.includes('HIT');
}

// JSON 응답 본문 검증 함수
function checkJsonResponse(response, checkNamePrefix) {
  let isJsonValid = false;
  let checkResult = {};

  if (response && typeof response.body === 'string') {
    try {
      const body = response.body.trim();
      // 빈 응답도 유효한 JSON이 아님
      if (body.length > 0 && (body.startsWith('{') || body.startsWith('['))) {
        JSON.parse(body);
        isJsonValid = true;
      } else if (body.length === 0) {
         // console.log(`${checkNamePrefix} 응답 본문 비어 있음`); // 빈 응답도 실패로 간주할 수 있음
         isJsonValid = false; // 또는 필요에 따라 true
      } else {
         // console.log(`${checkNamePrefix} 유효하지 않은 JSON 형식 시작: ${body.substring(0, 10)}`);
         isJsonValid = false;
      }
    } catch (e) {
      // console.error(`${checkNamePrefix} JSON 파싱 실패 (${response.request.url}): ${e.message}, 본문 시작: ${response.body.substring(0, 50)}`);
      isJsonValid = false;
    }
  } else if (response && response.json()) {
    // k6가 이미 JSON으로 파싱한 경우 (Content-Type 보고 판단)
    isJsonValid = true;
  } else {
    // console.log(`${checkNamePrefix} 응답 본문이 없거나 문자열이 아님`);
    isJsonValid = false;
  }

  checkResult[`${checkNamePrefix} JSON 유효성`] = () => isJsonValid;
  if (!isJsonValid && response) {
      // 실패 시 더 자세한 정보 로깅 (필요 시)
      console.error(`[${response.request.tags.name || 'Unknown Request'}] JSON 응답 검증 실패 - Status: ${response.status}, URL: ${response.request.url}, Body: ${response.body ? response.body.substring(0, 100) + '...' : 'N/A'}`);
  }
  return checkResult;
}

// 응답 헤더 로깅 함수
function logResponseHeaders(response, url, limit = 5) {
  // 첫 몇 번의 반복에서만 모든 헤더 출력 (로그 양 제한)
  if (__ITER < limit) {
    console.log(`\n===== ${url} 응답 헤더 =====`);
    for (const key in response.headers) {
      console.log(`${key}: ${response.headers[key]}`);
    }
    console.log("========================\n");
  }
}

// 퀴즈 상세 조회 테스트 (첫 요청과 두 번째 요청 시간 비교)
function testQuizDetailWithCachingEffect(data) {
  const quizId = quizIds[Math.floor(Math.random() * quizIds.length)];
  const url = `${BASE_URL}/quizzes/${quizId}`;
  
  // 첫 번째 요청 (캐시 미스 발생 가능)
  const firstParams = {
    headers: getHeaders(data),
    tags: { name: 'QuizDetail-First' }, // 태그 추가
    timeout: '5s',
  };
  const firstResponse = http.get(url, firstParams);
  firstRequestTrend.add(firstResponse.timings.duration);
  totalRequests.add(1);
  
  // 첫 요청의 헤더 로깅 (디버깅용)
  // if (__ITER < 3) {
  //   logResponseHeaders(firstResponse, `첫번째 요청 ${url}`);
  // }
  
  // 응답 체크 (JSON 검증 포함)
  const firstSuccess = check(firstResponse, {
    'QuizDetail-First: 상태 코드 200': (r) => r && r.status === 200,
    ...checkJsonResponse(firstResponse, 'QuizDetail-First:') // JSON 검증 함수 호출
  });
  failRate.add(!firstSuccess['QuizDetail-First: 상태 코드 200']); // 실패율 기록 (상태코드 기준)
  
  // 캐시 상태 확인
  const firstIsCacheHit = checkCacheHit(firstResponse);
  if (firstIsCacheHit) {
    caffeineHits.add(1);
    cacheHitCount.add(1);
  } else {
    caffeineMisses.add(1);
    cacheMissCount.add(1);
  }
  cacheHitRate.add(firstIsCacheHit); // 첫 요청의 캐시 히트율 (낮을 것으로 예상)
  
  sleep(0.5); // 약간의 지연
  
  // 두 번째 요청 (캐시 히트 기대)
  const secondParams = {
    headers: getHeaders(data), // 동일 헤더 사용
    tags: { name: 'QuizDetail-Second' }, // 태그 추가
    timeout: '3s', // 캐시 히트 시 더 짧은 타임아웃 가능
  };
  const secondResponse = http.get(url, secondParams);
  secondRequestTrend.add(secondResponse.timings.duration);
  totalRequests.add(1);
  
  // 두 번째 요청 헤더 로깅 (디버깅용)
  // if (__ITER < 3) {
  //   logResponseHeaders(secondResponse, `두번째 요청 ${url}`);
  // }
  
  // 응답 체크 (JSON 검증 포함)
  const secondSuccess = check(secondResponse, {
    'QuizDetail-Second: 상태 코드 200': (r) => r && r.status === 200,
    ...checkJsonResponse(secondResponse, 'QuizDetail-Second:') // JSON 검증 함수 호출
  });
  failRate.add(!secondSuccess['QuizDetail-Second: 상태 코드 200']);
  
  // 캐시 상태 확인
  const secondIsCacheHit = checkCacheHit(secondResponse);
  if (secondIsCacheHit) {
    caffeineHits.add(1);
    cacheHitCount.add(1);
  } else {
    caffeineMisses.add(1);
    cacheMissCount.add(1);
    // console.log(`[QuizDetail-Second] 캐시 미스 발생! URL: ${url}`); // 캐시 미스 시 로그
  }
  cacheHitRate.add(secondIsCacheHit); // 두 번째 요청의 캐시 히트율 (높을 것으로 예상)
  
  // 캐시 효과 분석
  if (firstResponse.timings && secondResponse.timings) {
    cacheBenefitRatio.add(firstResponse.timings.duration / secondResponse.timings.duration);
  }
  
  // 수정된 캐시 확인 로직
  const isCacheHit = secondIsCacheHit || firstIsCacheHit;
  
  // 이전 응답과 현재 응답의 내용이 같은지 확인 (추가 캐시 히트 확인 방법)
  let contentMatch = false;
  
  try {
    if (firstResponse.body && secondResponse.body) {
      // 응답 본문의 처음 100자만 비교
      const firstPart = firstResponse.body.substring(0, 100);
      const secondPart = secondResponse.body.substring(0, 100);
      contentMatch = firstPart === secondPart;
      
      if (__ITER < 3) {
        console.log(`응답 본문 비교: ${contentMatch ? '일치' : '불일치'}`);
      }
    }
  } catch (e) {
    console.log(`응답 본문 비교 중 오류: ${e.message}`);
  }
  
  // 캐시 상태 로깅 (간결하게)
  console.log(`[캐시테스트 ID=${quizId}] 첫요청=${firstResponse.timings.duration.toFixed(2)}ms, 두번째요청=${secondResponse.timings.duration.toFixed(2)}ms, 캐시=${isCacheHit ? 'HIT' : 'MISS'}, 내용일치=${contentMatch}`);
  
  // 메트릭 기록 - 캐시 히트 여부 (헤더 또는 내용 일치)
  const finalCacheHit = isCacheHit || contentMatch;
  
  // 메트릭 기록
  if (finalCacheHit) {
    caffeineHits.add(1);
    cacheHitCount.add(1);
  } else {
    caffeineMisses.add(1);
    cacheMissCount.add(1);
  }
  
  cacheHitRate.add(finalCacheHit);
  
  // 성능 개선 비율 계산
  if (firstSuccess && secondSuccess && firstResponse.timings.duration > 0 && secondResponse.timings.duration > 0) {
    const ratio = firstResponse.timings.duration / secondResponse.timings.duration;
    cacheBenefitRatio.add(ratio);
    
    // 5배 이상 개선되면 특별히 표시
    if (ratio >= 5) {
      console.log(`✨ 주목할만한 캐시 성능 개선: ${ratio.toFixed(2)}배 빨라짐!`);
    }
  }
  
  return {first: firstResponse, second: secondResponse};
}

// 퀴즈 상세 조회 테스트
function testQuizDetail(data) {
  const quizId = quizIds[Math.floor(Math.random() * quizIds.length)];
  const url = `${BASE_URL}/quizzes/${quizId}`;
  
  const params = {
    headers: getHeaders(data),
    tags: { name: 'QuizDetail-Single' }, // 태그 추가
    timeout: '5s',
  };
  
  const response = http.get(url, params);
  
  // 응답 체크
  const success = check(response, {
    'QuizDetail-Single: 상태 코드 200': (r) => r && r.status === 200,
    ...checkJsonResponse(response, 'QuizDetail-Single:') // JSON 검증 함수 호출
  });
  failRate.add(!success['QuizDetail-Single: 상태 코드 200']);

  // 필요한 경우에만 헤더 로깅
  if (__ITER === 0) {
    logResponseHeaders(response, url);
  }

  // 수정된 캐시 확인 로직
  const isCacheHit = checkCacheHit(response);
  
  // 더 간결한 로깅
  if (__ITER % 10 === 0) { // 10번째 요청마다 로그 출력
    console.log(`[퀴즈상세 ID=${quizId}] 응답시간=${response.timings.duration.toFixed(2)}ms, 캐시=${isCacheHit ? 'HIT' : 'MISS'}`);
  }
  
  // 캐시 상태 메트릭
  cacheHitRate.add(isCacheHit);
  
  // 직접 카운트
  if (isCacheHit) {
    cacheHitCount.add(1); 
  } else {
    cacheMissCount.add(1);
  }
  
  // 성능 메트릭 기록
  quizDetailTrend.add(response.timings.duration);
  failRate.add(!success['QuizDetail-Single: 상태 코드 200']);
  totalRequests.add(1);
  
  return response;
}

// 퀴즈 검색 테스트
function testQuizSearch(data) {
  // 검색 조건 랜덤 선택
  const keyword = searchKeywords[Math.floor(Math.random() * searchKeywords.length)];
  const difficulty = difficulties[Math.floor(Math.random() * difficulties.length)];
  const tagId = tagIds[Math.floor(Math.random() * tagIds.length)];
  const page = Math.floor(Math.random() * 5); // 0 ~ 4 페이지 랜덤 조회
  const size = 10; // 페이지 당 10개

  // 검색 URL 조합 (Query 파라미터 사용)
  let searchUrl = `${BASE_URL}/quizzes/search?page=${page}&size=${size}`;
  let useKeyword = false, useDifficulty = false, useTag = false;
  if (Math.random() < 0.5) { // 50% 확률로 키워드 검색
    searchUrl += `&title=${encodeSearchKeyword(keyword)}`;
    useKeyword = true;
  }
  if (Math.random() < 0.5) { // 50% 확률로 난이도 검색
    searchUrl += `&difficultyLevel=${difficulty}`;
    useDifficulty = true;
  }
  if (Math.random() < 0.3) { // 30% 확률로 태그 검색
    searchUrl += `&tagIds=${tagId}`;
    useTag = true;
  }

  const params = {
    headers: getHeaders(data),
    tags: { name: 'QuizSearch' }, // 태그 명확화
    timeout: '8s', // 검색은 조금 더 오래 걸릴 수 있음
  };

  const response = http.get(searchUrl, params);

  // 응답 체크 (JSON 검증 포함)
  const success = check(response, {
    'QuizSearch: 상태 코드 200': (r) => r && r.status === 200,
    ...checkJsonResponse(response, 'QuizSearch:') // JSON 검증 함수 호출
  });

  // 필요한 경우에만 헤더 로깅
  // if (__ITER < 2) {
  //   logResponseHeaders(response, `검색 URL: ${searchUrl}`);
  // }

  // 수정된 캐시 확인 로직
  const isCacheHit = checkCacheHit(response);

  // 로깅 (검색 조건 포함)
  // if (__ITER % 10 === 0) { // 10번째 요청마다 로그 출력
  //   let logMsg = `[QuizSearch ${useKeyword ? 'K:'+keyword : ''} ${useDifficulty ? 'D:'+difficulty : ''} ${useTag ? 'T:'+tagId : ''}] `;
  //   logMsg += `응답=${response.timings.duration.toFixed(2)}ms, 캐시=${isCacheHit ? 'HIT' : 'MISS'}`;
  //   console.log(logMsg);
  // }

  // 캐시 상태 메트릭
  cacheHitRate.add(isCacheHit);

  // 직접 카운트
  if (isCacheHit) {
    cacheHitCount.add(1);
  } else {
    cacheMissCount.add(1);
  }

  // 성능 메트릭 기록
  quizSearchTrend.add(response.timings.duration);
  // failRate 계산 오류 수정: success 객체 내의 '상태 코드 200' 키 사용
  failRate.add(!success['QuizSearch: 상태 코드 200']);
  totalRequests.add(1);

  // 오류 로깅 (상태 코드 기준)
  if (!success['QuizSearch: 상태 코드 200']) {
    // console.error(`❌ QuizSearch API 호출 실패: Status ${response.status}, URL: ${searchUrl}`); // 주석 처리: 혼란을 야기함
  }

  return response;
}

// 캐시 효과 분석 함수 (옵션)
function analyzeCacheEffectiveness() {
  const hitRate = cacheHitCount.value / (cacheHitCount.value + cacheMissCount.value) * 100;
  
  console.log(`\n===== 캐시 효과 분석 =====`);
  console.log(`캐시 히트율: ${hitRate.toFixed(2)}%`);
  console.log(`총 캐시 히트: ${cacheHitCount.value}회`);
  console.log(`총 캐시 미스: ${cacheMissCount.value}회`);
  
  if (hitRate >= 70) {
    console.log(`✨ 캐시 성능이 매우 좋습니다!`);
  } else if (hitRate >= 50) {
    console.log(`✓ 캐시 성능이 양호합니다.`);
  } else {
    console.log(`⚠️ 캐시 성능이 개선 필요합니다.`);
  }
  
  console.log(`========================\n`);
}

// 메인 함수 최적화
export default function(data) {
  // 인증 토큰 확인
  if (!data.authToken && __ITER === 0) {
    console.log('⚠️ 경고: 인증 토큰이 없습니다. 인증이 필요한 API 테스트는 스킵됩니다.');
  }
  
  // 주기적으로 캐시 효과 분석 (10번째 VU마다)
  if (__VU % 10 === 0 && __ITER === 10) {
    analyzeCacheEffectiveness();
  }
  
  // Caffeine 캐시 특화 테스트: 캐시 워밍업 효과 측정
  testQuizDetailWithCachingEffect(data);
  sleep(1);
  
  // 일반 테스트
  testQuizDetail(data);
  sleep(1);
  
  testQuizSearch(data);
  sleep(1);
  
  // 테스트가 끝나면 요약 정보 출력 (마지막 VU의 마지막 반복)
  if (__ITER === options.stages[options.stages.length-1].target - 1 && __VU === options.stages[options.stages.length-1].target) {
    console.log('\n========== 최종 테스트 요약 정보 ==========');
    console.log(`총 요청 수: ${totalRequests.value}`);
    console.log(`캐시 히트 카운트: ${cacheHitCount.value}`);
    console.log(`캐시 미스 카운트: ${cacheMissCount.value}`);
    console.log(`캐시 히트율: ${(cacheHitCount.value / (cacheHitCount.value + cacheMissCount.value) * 100).toFixed(2)}%`);
    console.log('==========================================\n');
  }
}

export function handleSummary(data) {
  console.log('\n📊 K6 Performance Test Summary 📊');
  console.log('=======================================');

  // 테스트 실행 정보
  console.log(`\n⏱️  테스트 기간: ${data.metrics.iteration_duration.values.p95 / 1000}s (p95)`);
  console.log(`🔄 총 반복 횟수: ${data.metrics.iterations.values.count}`);
  console.log(`👥 최대 가상 사용자: ${data.vus.max}`);
  console.log(`📈 총 요청 수: ${data.metrics.total_requests.values.count}`);

  // 주요 API 응답 시간 (p95)
  console.log('\n🚀 주요 API 응답 시간 (95th Percentile):');
  if (data.metrics.quiz_detail_response_time) {
    console.log(`  - 퀴즈 상세 조회 (Single): ${data.metrics.quiz_detail_response_time.values['p(95)'].toFixed(2)} ms`);
  }
   if (data.metrics.first_request_response_time) {
    console.log(`  - 퀴즈 상세 (First - Cache Miss): ${data.metrics.first_request_response_time.values['p(95)'].toFixed(2)} ms`);
  }
  if (data.metrics.second_request_response_time) {
    console.log(`  - 퀴즈 상세 (Second - Cache Hit): ${data.metrics.second_request_response_time.values['p(95)'].toFixed(2)} ms`);
  }
  if (data.metrics.quiz_search_response_time) {
    console.log(`  - 퀴즈 검색: ${data.metrics.quiz_search_response_time.values['p(95)'].toFixed(2)} ms`);
  }
  if (data.metrics.comprehensive_test_response_time) {
     console.log(`  - 종합 시나리오: ${data.metrics.comprehensive_test_response_time.values['p(95)'].toFixed(2)} ms`);
  }

  // 오류 및 실패율
  console.log('\n❗ 오류 및 실패율:');
  // request_fail_rate는 이제 실제 HTTP 실패율 (상태 코드 != 2xx, 3xx)을 반영할 가능성이 높음 (k6 내부 메트릭과 유사)
  // 또는 스크립트에서 상태코드 200 아닌 경우만 failRate.add 했으므로, 200 아닌 응답 비율을 나타냄.
  console.log(`  - 상태 코드 200 실패율 (Script): ${(data.metrics.request_fail_rate.values.rate * 100).toFixed(2)}%`);
  console.log(`  - 실제 HTTP 요청 실패율 (k6): ${(data.metrics.http_req_failed.values.rate * 100).toFixed(2)}% (${data.metrics.http_req_failed.values.passes} fails / ${data.metrics.http_reqs.values.count} reqs)`);
  const failedChecks = data.metrics.checks.values.fails;
  const totalChecks = data.metrics.checks.values.passes + failedChecks;
  console.log(`  - 총 검증(Checks) 실패 수: ${failedChecks} / ${totalChecks} (${(failedChecks/totalChecks * 100).toFixed(2)}%)`);

  // 실패한 Check 항목 상세 출력 (상위 5개)
  console.log('\n   Failed Checks Breakdown:');
  let failedCheckCount = 0;
  for (const checkName in data.metrics.checks.values.failures) {
    if (failedCheckCount < 5) {
        const failCount = data.metrics.checks.values.failures[checkName];
        console.log(`     - ${checkName}: ${failCount} failures`);
        failedCheckCount++;
    } else {
        console.log('     ... (more)');
        break;
    }
  }


  // 캐시 성능 지표
  console.log('\n💾 캐시 성능:');
  // ... existing code ...
}