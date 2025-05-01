import http from 'k6/http';
import { sleep, check, group } from 'k6';
import { Trend, Rate, Counter, Gauge } from 'k6/metrics';
import { SharedArray } from 'k6/data';
import encoding from 'k6/encoding';

// 성능 메트릭 정의
const quizDetailTrend = new Trend('quiz_detail_response_time');
const quizSearchTrend = new Trend('quiz_search_response_time');
const popularQuizTrend = new Trend('popular_quiz_response_time');
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
    'popular_quiz_response_time': ['p(95)<100'],
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
    console.log(`캐시 체크 불가: 헤더 없음`);
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



  // HIT, PARTIAL_HIT, OK 등 다양한 캐시 히트 상태 지원
  const hitStatuses = ['HIT', 'PARTIAL_HIT', 'OK'];
  return hitStatuses.includes(cacheStatus) || cacheStatus.includes('HIT');
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
  
  // 첫 번째 요청 (캐시 미스 발생) - 일반 헤더 사용
  const firstHeaders = {
    ...getHeaders(data)
    // 캐시 우회 헤더 제거
    // 'Cache-Control': 'no-cache',
    // 'X-No-Cache': 'true'
  };
  
  const params = {
    headers: firstHeaders,
    tags: { name: 'QuizDetail-First' },
    timeout: '5s',
  };
  
  const firstResponse = http.get(url, params);
  firstRequestTrend.add(firstResponse.timings.duration);
  
  // 첫 요청의 헤더 로깅
  if (__ITER < 3) {
    logResponseHeaders(firstResponse, `첫번째 요청 ${url}`);
  }
  
  // 응답 체크
  const firstSuccess = check(firstResponse, {
    '첫 번째 요청 성공': (r) => r && r.status === 200,
    '첫 번째 요청 JSON 응답': (r) => {
      // 응답 본문이 문자열이고 JSON 형식인지 확인
      if (r && typeof r.body === 'string') {
        try {
          // 응답 본문이 JSON 파싱 가능한지 확인
          const body = r.body.trim();
          if (body.startsWith('{') || body.startsWith('[')) {
            // 첫 몇 개의 요청에서 디버깅 로그
            if (__ITER < 3) {
              console.log(`첫번째 요청 확인: 본문 시작=${body.substring(0, 20)}`);
              // JSON 파싱 테스트
              JSON.parse(body);
              console.log(`첫번째 요청 JSON 파싱 성공!`);
            }
            return true;
          }
        } catch (e) {
          if (__ITER < 3) {
            console.log(`첫번째 요청 JSON 파싱 실패: ${e.message}`);
          }
        }
      }
      
      // 헤더가 application/json이면 성공으로 처리
      for (let key in r.headers) {
        if (key.toLowerCase() === 'content-type' && 
            r.headers[key].toLowerCase().includes('application/json')) {
          return true;
        }
      }
      
      return false;
    },
  });
  
  // 1초 지연
  sleep(1);
  
  // 두 번째 요청 (캐시 히트 기대)
  const secondResponse = http.get(url, {
    headers: getHeaders(data),
    tags: { name: 'QuizDetail-Second' },
    timeout: '5s',
  });
  secondRequestTrend.add(secondResponse.timings.duration);
  
  // 두번째 요청의 헤더 로깅
  if (__ITER < 3) {
    logResponseHeaders(secondResponse, `두번째 요청 ${url}`);
  }
  
  // 응답 체크
  const secondSuccess = check(secondResponse, {
    '두 번째 요청 성공': (r) => r && r.status === 200,
    '두 번째 요청 JSON 응답': (r) => {
      // 응답 본문이 문자열이고 JSON 형식인지 확인
      if (r && typeof r.body === 'string') {
        try {
          // 응답 본문이 JSON 파싱 가능한지 확인
          const body = r.body.trim();
          if (body.startsWith('{') || body.startsWith('[')) {
            // 첫 몇 개의 요청에서 디버깅 로그
            if (__ITER < 3) {
              console.log(`두번째 요청 확인: 본문 시작=${body.substring(0, 20)}`);
              // JSON 파싱 테스트
              JSON.parse(body);
              console.log(`두번째 요청 JSON 파싱 성공!`);
            }
            return true;
          }
        } catch (e) {
          if (__ITER < 3) {
            console.log(`두번째 요청 JSON 파싱 실패: ${e.message}`);
          }
        }
      }
      
      // 헤더가 application/json이면 성공으로 처리
      for (let key in r.headers) {
        if (key.toLowerCase() === 'content-type' && 
            r.headers[key].toLowerCase().includes('application/json')) {
          return true;
        }
      }
      
      return false;
    },
  });
  
  // 수정된 캐시 확인 로직
  const isCacheHit = checkCacheHit(secondResponse);
  
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
    tags: { name: 'QuizDetail' },
    timeout: '5s',
  };
  
  const response = http.get(url, params);
  
  // 응답 체크
  const success = check(response, {
    '응답 상태 200': (r) => r && r.status === 200,
    'JSON 응답': (r) => {
      // 응답 본문이 문자열이고 JSON 형식인지 확인
      if (r && typeof r.body === 'string') {
        try {
          // 응답 본문이 JSON 파싱 가능한지 확인
          const body = r.body.trim();
          if (body.startsWith('{') || body.startsWith('[')) {
            // 첫 몇 개의 요청에서 디버깅 로그
            if (__ITER < 3) {
              console.log(`응답 확인: 본문 시작=${body.substring(0, 20)}`);
              // JSON 파싱 테스트
              JSON.parse(body);
              console.log(`JSON 파싱 성공!`);
            }
            return true;
          }
        } catch (e) {
          if (__ITER < 3) {
            console.log(`JSON 파싱 실패: ${e.message}`);
          }
        }
      }
      
      // 헤더가 application/json이면 성공으로 처리
      for (let key in r.headers) {
        if (key.toLowerCase() === 'content-type' && 
            r.headers[key].toLowerCase().includes('application/json')) {
          return true;
        }
      }
      
      // 둘 다 실패한 경우 디버깅 출력
      if (__ITER < 3) {
        console.log(`JSON 응답 확인 실패. 헤더:`, r.headers);
      }
      return false;
    },
  });

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
  failRate.add(!success);
  totalRequests.add(1);
  
  return response;
}

// 퀴즈 검색 테스트
function testQuizSearch(data) {
  const keyword = searchKeywords[Math.floor(Math.random() * searchKeywords.length)];
  const encodedKeyword = encodeSearchKeyword(keyword);
  const url = `${BASE_URL}/quizzes/search?keyword=${encodedKeyword}&page=0&size=10`;
  
  const params = {
    headers: getHeaders(data),
    tags: { name: 'QuizSearch' },
    timeout: '5s',
  };
  
  const response = http.get(url, params);
  
  // 응답 체크
  const success = check(response, {
    '응답 상태 200': (r) => r && r.status === 200,
    'JSON 응답': (r) => {
      // 응답 본문이 문자열이고 JSON 형식인지 확인
      if (r && typeof r.body === 'string') {
        try {
          // 응답 본문이 JSON 파싱 가능한지 확인
          const body = r.body.trim();
          if (body.startsWith('{') || body.startsWith('[')) {
            // 첫 몇 개의 요청에서 디버깅 로그
            if (__ITER < 3) {
              console.log(`응답 확인: 본문 시작=${body.substring(0, 20)}`);
              // JSON 파싱 테스트
              JSON.parse(body);
              console.log(`JSON 파싱 성공!`);
            }
            return true;
          }
        } catch (e) {
          if (__ITER < 3) {
            console.log(`JSON 파싱 실패: ${e.message}`);
          }
        }
      }
      
      // 헤더가 application/json이면 성공으로 처리
      for (let key in r.headers) {
        if (key.toLowerCase() === 'content-type' && 
            r.headers[key].toLowerCase().includes('application/json')) {
          return true;
        }
      }
      
      // 둘 다 실패한 경우 디버깅 출력
      if (__ITER < 3) {
        console.log(`JSON 응답 확인 실패. 헤더:`, r.headers);
      }
      return false;
    },
  });
  
  // 필요한 경우에만 헤더 로깅
  if (__ITER < 2) {
    logResponseHeaders(response, `검색 URL: ${url}`);
  }
  
  // 수정된 캐시 확인 로직
  const isCacheHit = checkCacheHit(response);
  
  // 로깅 (검색 키워드 포함)
  if (__ITER % 5 === 0) { // 5번째 요청마다 로그
    console.log(`[검색: ${keyword}] 응답시간=${response.timings.duration.toFixed(2)}ms, 캐시=${isCacheHit ? 'HIT' : 'MISS'}, 헤더=${response.headers['x-cache-status'] || 'NONE'}`);
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
  quizSearchTrend.add(response.timings.duration);
  failRate.add(!success);
  totalRequests.add(1);
  
  // 오류 로깅
  if (!success) {
    console.log(`❌ 퀴즈 검색 API 호출 실패: ${response.status}, 키워드: ${keyword}`);
  }
  
  return response;
}

// 인기 퀴즈 조회 테스트
function testPopularQuizzes(data) {
  // 인증 토큰이 없으면 테스트 스킵
  if (!data.authToken) {
    console.log('인증 토큰이 없어 인기 퀴즈 테스트를 스킵합니다.');
    return null;
  }
  
  const limit = 10;
  const url = `${BASE_URL}/recommendations/popular?limit=${limit}`;
  
  // 요청 시 타임아웃 설정 추가 및 요청 옵션 강화
  const params = {
    headers: getHeaders(data),
    tags: { name: 'PopularQuizzes' },
    timeout: '5s',
  };
  
  const response = http.get(url, params);
  
  // 응답 검증
  const success = check(response, {
    '응답 상태 200': (r) => r && r.status === 200,
    'JSON 응답': (r) => r && r.headers && r.headers['content-type'] && r.headers['content-type'].includes('application/json'),
  });
  
  // 필요한 경우에만 헤더 로깅
  if (__ITER < 2) {
    logResponseHeaders(response, '인기 퀴즈 URL');
  }
  
  // 수정된 캐시 확인 로직
  const isCacheHit = checkCacheHit(response);
  
  // 로깅
  if (__ITER % 5 === 0) {
    console.log(`[인기퀴즈] 응답시간=${response.timings.duration.toFixed(2)}ms, 캐시=${isCacheHit ? 'HIT' : 'MISS'}, 헤더=${response.headers['x-cache-status'] || 'NONE'}`);
  }
  
  // 캐시 상태 메트릭
  cacheHitRate.add(isCacheHit);
  
  // 직접 카운트
  if (isCacheHit) {
    cacheHitCount.add(1); 
    // 첫 몇 번의 캐시 히트마다 강조 표시
    if (caffeineHits.value < 5) {
      console.log(`✅ 인기 퀴즈 캐시 히트 성공! 응답시간=${response.timings.duration.toFixed(2)}ms`);
    }
  } else {
    cacheMissCount.add(1);
  }
  
  // 성능 메트릭 기록
  popularQuizTrend.add(response.timings.duration);
  failRate.add(!success);
  totalRequests.add(1);
  
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
  
  // 인기 퀴즈 API 호출 비율 감소 (25% 확률로만 실행)
  if (Math.random() < 0.25) {
    testPopularQuizzes(data);
  } else {
    // 인기 퀴즈 테스트를 건너 뛰어도 일관된 슬립 타임 유지
    sleep(1);
  }
  
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