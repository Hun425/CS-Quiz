import http from 'k6/http';
import { sleep, check } from 'k6';
import { Trend, Rate, Counter } from 'k6/metrics';

// 성능 메트릭 정의
const firstRequestTrend = new Trend('first_request_response_time');
const secondRequestTrend = new Trend('second_request_response_time');
const cacheBenefitRatio = new Trend('cache_benefit_ratio');
const cacheHitRate = new Rate('cache_hit_rate');
const totalRequests = new Counter('total_requests');
const caffeineCacheMisses = new Counter('caffeine_cache_misses');

// API 엔드포인트 설정
const BASE_URL = 'http://localhost:8080/api';

// 테스트할 엔드포인트 설정
const TEST_ENDPOINTS = [
  { name: 'quiz_detail', url: '/quizzes/1' },
  { name: 'popular_quiz', url: '/recommendations/popular?limit=10' },
  { name: 'quiz_search', url: '/quizzes/search?keyword=자바&page=0&size=10' }
];

// 테스트 설정
export const options = {
  vus: 1,
  iterations: 5,
  thresholds: {
    'second_request_response_time': ['p(95)<50'],
    'popular_quiz_response_time': ['p(95)<100'],
    'quiz_detail_response_time': ['p(95)<150'],
    'quiz_search_response_time': ['p(95)<200'],
    'request_fail_rate': ['rate<0.01'],
  },
};

// 헤더 설정
const headers = {
  'Content-Type': 'application/json',
  'Accept': 'application/json',
};

// 테스트 함수
export default function() {
  for (const endpoint of TEST_ENDPOINTS) {
    const url = `${BASE_URL}${endpoint.url}`;
    console.log(`테스트 시작: ${endpoint.name}, URL: ${url}`);
    
    // 캐시 초기화를 위한 첫 번째 요청 - 캐시 무효화 헤더 사용
    const firstHeaders = {
      ...headers,
      'Cache-Control': 'no-cache',
      'X-No-Cache': 'true'
    };
    
    console.log('첫 번째 요청 전송 (캐시 무효화)');
    const firstResponse = http.get(url, { headers: firstHeaders });
    
    // 응답 체크
    const firstSuccess = check(firstResponse, {
      '첫 번째 요청 성공': (r) => r.status === 200,
      '첫 번째 요청 JSON 응답': (r) => r.headers['Content-Type'] && r.headers['Content-Type'].includes('application/json'),
    });
    
    // 첫 번째 요청 성능 기록
    firstRequestTrend.add(firstResponse.timings.duration);
    console.log(`첫 번째 요청 응답 시간: ${firstResponse.timings.duration}ms`);
    
    // 캐시 헤더 확인
    console.log(`첫 번째 요청 캐시 상태: ${firstResponse.headers['X-Cache-Status'] || '없음'}`);
    
    // 캐시가 적용될 시간 대기 (2초)
    sleep(2);
    
    // 두 번째 요청 - 캐시 활용 (캐시 무효화 헤더 없음)
    console.log('두 번째 요청 전송 (캐시 활용)');
    const secondResponse = http.get(url, { headers: headers });
    
    // 응답 체크
    const secondSuccess = check(secondResponse, {
      '두 번째 요청 성공': (r) => r.status === 200,
      '두 번째 요청 JSON 응답': (r) => r.headers['Content-Type'] && r.headers['Content-Type'].includes('application/json'),
    });
    
    // 두 번째 요청 성능 기록
    secondRequestTrend.add(secondResponse.timings.duration);
    console.log(`두 번째 요청 응답 시간: ${secondResponse.timings.duration}ms`);
    
    // 캐시 헤더 확인
    const cacheStatus = secondResponse.headers['X-Cache-Status'] || secondResponse.headers['x-cache-status'] || '없음';
    console.log(`두 번째 요청 캐시 상태: ${cacheStatus}`);
    
    // 캐시 히트 여부 확인
    const isCacheHit = cacheStatus === 'HIT';
    
    if (isCacheHit) {
      console.log('캐시 히트 감지됨! ✅');
    } else {
      console.log('캐시 미스 감지됨! ❌');
      caffeineCacheMisses.add(1);
    }
    
    // 캐시 히트율 측정
    cacheHitRate.add(isCacheHit);
    
    // 캐시 혜택 비율 계산 (첫 번째 응답 시간 / 두 번째 응답 시간)
    if (firstSuccess && secondSuccess && firstResponse.timings.duration > 0 && secondResponse.timings.duration > 0) {
      const ratio = firstResponse.timings.duration / secondResponse.timings.duration;
      cacheBenefitRatio.add(ratio);
      console.log(`캐시 성능 향상 비율: ${ratio.toFixed(2)}배`);
    }
    
    // 카운터 증가
    totalRequests.add(2); // 첫 번째와 두 번째 요청 모두 카운트
    
    // 다음 엔드포인트 테스트 전 잠시 대기
    sleep(3);
  }
} 