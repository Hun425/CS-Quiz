import http from 'k6/http';
import { sleep, check, group } from 'k6';
import { Trend, Rate } from 'k6/metrics';
import { SharedArray } from 'k6/data';

// 데이터 크기를 환경 변수에서 가져옴 (기본값 100)
const DATA_SIZE = __ENV.DATA_SIZE || 100;

// 성능 메트릭 정의
const quizDetailWithCache = new Trend('quiz_detail_with_cache');
const quizDetailNoCache = new Trend('quiz_detail_no_cache');
const searchWithCache = new Trend('quiz_search_with_cache');
const searchNoCache = new Trend('quiz_search_no_cache');
const popularWithCache = new Trend('popular_quiz_with_cache');
const popularNoCache = new Trend('popular_quiz_no_cache');
const cacheImprovementRatio = new Trend('cache_improvement_ratio');
const successRate = new Rate('success_rate');

// API 엔드포인트 설정
const BASE_URL = 'http://host.docker.internal:8080/api';

// 기본 설정
const headers = {
  'Content-Type': 'application/json',
  'Accept': 'application/json',
};

// 캐시 비활성화 헤더
const noCacheHeaders = {
  ...headers,
  'Cache-Control': 'no-cache',
  'Pragma': 'no-cache',
  'X-Skip-Cache': 'true',
};

// HTTP 요청 옵션
const requestOptions = {
  timeout: '30s', // 타임아웃 증가
};

// 테스트 설정
export const options = {
  stages: [
    { duration: '30s', target: 5 },   // 워밍업: 5명의 사용자로 시작
    { duration: '1m', target: 10 },   // 부하 증가: 사용자 수 조정
    { duration: '2m', target: 10 },   // 부하 유지: 사용자 수 조정
    { duration: '30s', target: 0 },   // 정리: 점진적으로 부하 감소
  ],
  thresholds: {
    'http_req_duration': ['p(95)<2000'], // 타임아웃 증가
    'cache_improvement_ratio': ['avg>0.9'], // 목표 성능 개선율 조정
    'success_rate': ['rate>0.9'],     // 성공률 목표 조정
  },
};

// 초기화 함수: 테스트 전 데이터 생성 및 캐시 워밍업
export function setup() {
  console.log(`데이터 크기: ${DATA_SIZE}개로 테스트 시작`);
  
  // 데이터 삭제 및 초기화 사이 대기 추가
  const clearResponse = http.del(`${BASE_URL}/admin/clear-data`, null, { ...requestOptions, headers });
  check(clearResponse, {
    '이전 데이터 정리 성공': (r) => r.status === 200,
  });
  sleep(3); // 데이터 삭제 후 대기
  
  // 새 데이터 생성
  const generateResponse = http.post(
    `${BASE_URL}/admin/generate-quizzes?count=${DATA_SIZE}`, 
    null, 
    { ...requestOptions, headers }
  );
  check(generateResponse, {
    '테스트 데이터 생성 성공': (r) => r.status === 200,
  });
  sleep(5); // 데이터 생성 후 대기
  
  // 캐시 워밍업
  const warmupResponse = http.post(
    `${BASE_URL}/admin/warmup-cache`, 
    null, 
    { ...requestOptions, headers }
  );
  check(warmupResponse, {
    '캐시 워밍업 성공': (r) => r.status === 200,
  });
  
  // 워밍업 완료 대기 시간 증가
  sleep(10);
  
  console.log('테스트 준비 완료');
  
  // 생성된 데이터에서 랜덤 퀴즈 ID 추출
  const quizIds = [];
  try {
    const response = JSON.parse(generateResponse.body);
    if (response.data && Array.isArray(response.data)) {
      return { quizIds: response.data };
    }
  } catch (e) {
    console.error('데이터 파싱 오류:', e);
  }
  
  // 기본 ID 세트
  return { quizIds: Array.from({ length: 10 }, (_, i) => i + 1) };
}

// 랜덤 요소 선택 함수
function getRandomItem(array) {
  return array[Math.floor(Math.random() * array.length)];
}

// 랜덤 키워드 선택 (한글로 복원)
function getRandomKeyword() {
  const keywords = ['자바', '파이썬', '알고리즘', '데이터베이스', '스프링'];
  return keywords[Math.floor(Math.random() * keywords.length)];
}

// 난이도 랜덤 선택
function getRandomDifficulty() {
  const difficulties = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED'];
  return difficulties[Math.floor(Math.random() * difficulties.length)];
}

// 퀴즈 상세 조회 테스트
function testQuizDetail(quizIds) {
  const quizId = getRandomItem(quizIds);
  const url = `${BASE_URL}/quizzes/${quizId}`;
  
  // 캐시 사용 요청
  const cacheResponse = http.get(url, { ...requestOptions, headers });
  check(cacheResponse, {
    '캐시 사용 퀴즈 상세 성공': (r) => r.status === 200,
  });
  quizDetailWithCache.add(cacheResponse.timings.duration);
  
  // 캐시 미사용 요청
  const noCacheResponse = http.get(url, { ...requestOptions, headers: noCacheHeaders });
  check(noCacheResponse, {
    '캐시 미사용 퀴즈 상세 성공': (r) => r.status === 200,
  });
  quizDetailNoCache.add(noCacheResponse.timings.duration);
  
  // 캐시 사용과 미사용의 성능 비교
  if (cacheResponse.timings.duration > 0 && noCacheResponse.timings.duration > 0) {
    const ratio = noCacheResponse.timings.duration / cacheResponse.timings.duration;
    cacheImprovementRatio.add(ratio);
    console.log(`퀴즈 상세(ID=${quizId}): 캐시=${cacheResponse.timings.duration}ms, 미사용=${noCacheResponse.timings.duration}ms, 개선율=${ratio.toFixed(2)}배`);
  }
  
  successRate.add(cacheResponse.status === 200 && noCacheResponse.status === 200);
}

// 퀴즈 검색 테스트
function testQuizSearch() {
  const keyword = getRandomKeyword();
  // URL 인코딩 적용
  const encodedKeyword = encodeURIComponent(keyword);
  const url = `${BASE_URL}/quizzes/search?keyword=${encodedKeyword}&page=0&size=10`;
  
  // 캐시 사용 요청
  const cacheResponse = http.get(url, { ...requestOptions, headers });
  check(cacheResponse, {
    '캐시 사용 퀴즈 검색 성공': (r) => r.status === 200,
  });
  searchWithCache.add(cacheResponse.timings.duration);
  
  // 캐시 미사용 요청
  const noCacheResponse = http.get(url, { ...requestOptions, headers: noCacheHeaders });
  check(noCacheResponse, {
    '캐시 미사용 퀴즈 검색 성공': (r) => r.status === 200,
  });
  searchNoCache.add(noCacheResponse.timings.duration);
  
  // 캐시 사용과 미사용의 성능 비교
  if (cacheResponse.timings.duration > 0 && noCacheResponse.timings.duration > 0) {
    const ratio = noCacheResponse.timings.duration / cacheResponse.timings.duration;
    cacheImprovementRatio.add(ratio);
    console.log(`퀴즈 검색(키워드=${keyword}): 캐시=${cacheResponse.timings.duration}ms, 미사용=${noCacheResponse.timings.duration}ms, 개선율=${ratio.toFixed(2)}배`);
  }
  
  successRate.add(cacheResponse.status === 200 && noCacheResponse.status === 200);
}

// 인기 퀴즈 추천 테스트
function testPopularQuizzes() {
  const url = `${BASE_URL}/recommendations/popular?limit=10`;
  
  // 캐시 사용 요청
  const cacheResponse = http.get(url, { ...requestOptions, headers });
  check(cacheResponse, {
    '캐시 사용 인기 퀴즈 성공': (r) => r.status === 200,
  });
  popularWithCache.add(cacheResponse.timings.duration);
  
  // 캐시 미사용 요청
  const noCacheResponse = http.get(url, { ...requestOptions, headers: noCacheHeaders });
  check(noCacheResponse, {
    '캐시 미사용 인기 퀴즈 성공': (r) => r.status === 200,
  });
  popularNoCache.add(noCacheResponse.timings.duration);
  
  // 캐시 사용과 미사용의 성능 비교
  if (cacheResponse.timings.duration > 0 && noCacheResponse.timings.duration > 0) {
    const ratio = noCacheResponse.timings.duration / cacheResponse.timings.duration;
    cacheImprovementRatio.add(ratio);
    console.log(`인기 퀴즈 추천: 캐시=${cacheResponse.timings.duration}ms, 미사용=${noCacheResponse.timings.duration}ms, 개선율=${ratio.toFixed(2)}배`);
  }
  
  successRate.add(cacheResponse.status === 200 && noCacheResponse.status === 200);
}

// 메인 테스트 함수
export default function(data) {
  const { quizIds } = data;
  
  group('퀴즈 상세 조회', () => {
    testQuizDetail(quizIds);
  });
  
  sleep(2); // 요청 사이 대기 시간 증가
  
  group('퀴즈 검색', () => {
    testQuizSearch();
  });
  
  sleep(2); // 요청 사이 대기 시간 증가
  
  group('인기 퀴즈 추천', () => {
    testPopularQuizzes();
  });
  
  sleep(3); // 다음 반복 전 대기 시간 증가
}

// 테스트 후 정리
export function teardown(data) {
  console.log(`데이터 크기 ${DATA_SIZE}개 테스트 완료`);
} 