import http from 'k6/http';
import { sleep, check } from 'k6';
import { Trend, Rate, Counter } from 'k6/metrics';
import { SharedArray } from 'k6/data';

// 성능 메트릭 정의
const quizDetailTrend = new Trend('quiz_detail_response_time');
const quizSearchTrend = new Trend('quiz_search_response_time');
const popularQuizTrend = new Trend('popular_quiz_response_time');
const categoryRecommendTrend = new Trend('category_recommend_response_time');
const difficultyRecommendTrend = new Trend('difficulty_recommend_response_time');

const failRate = new Rate('request_fail_rate');

// API 엔드포인트 설정
// const BASE_URL = 'http://127.0.0.1:8080/api';
// Docker 환경에서 실행하는 경우 아래 URL 사용
const BASE_URL = 'http://host.docker.internal:8080/api';

// 테스트 데이터 (실제 환경에 맞게 수정 필요)
const quizIds = new SharedArray('quizIDs', function() {
  return [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
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

const headers = {
  'Content-Type': 'application/json',
  'Accept': 'application/json',
};

// 테스트 설정
export const options = {
  stages: [
    // 웜업
    { duration: '30s', target: 10 },
    // 기본 부하
    { duration: '1m', target: 20 },
    // 피크 부하
    { duration: '2m', target: 30 },
    // 피크 후 감소
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    'quiz_detail_response_time': ['p(95)<200'],
    'quiz_search_response_time': ['p(95)<300'],
    'popular_quiz_response_time': ['p(95)<200'],
    'request_fail_rate': ['rate<0.01'], // 실패율 1% 미만
  },
};

// 퀴즈 상세 조회 테스트
function testQuizDetail() {
  const quizId = quizIds[Math.floor(Math.random() * quizIds.length)];
  const url = `${BASE_URL}/quizzes/${quizId}`;
  
  const response = http.get(url, { headers });
  
  const success = check(response, {
    '응답 상태 200': (r) => r && r.status === 200,
    'JSON 응답': (r) => r && r.headers && r.headers['Content-Type'] && r.headers['Content-Type'].includes('application/json'),
  });
  
  quizDetailTrend.add(response.timings.duration);
  failRate.add(!success);
  
  return response;
}

// 퀴즈 검색 테스트
function testQuizSearch() {
  const keyword = searchKeywords[Math.floor(Math.random() * searchKeywords.length)];
  const url = `${BASE_URL}/quizzes/search?keyword=${keyword}&page=0&size=10`;
  
  const response = http.get(url, { headers });
  
  const success = check(response, {
    '응답 상태 200': (r) => r && r.status === 200,
    'JSON 응답': (r) => r && r.headers && r.headers['Content-Type'] && r.headers['Content-Type'].includes('application/json'),
  });
  
  quizSearchTrend.add(response.timings.duration);
  failRate.add(!success);
  
  return response;
}

// 인기 퀴즈 조회 테스트
function testPopularQuizzes() {
  const limit = 10;
  // 수정: 실제 백엔드의 인기 퀴즈 API 경로 사용
  const url = `${BASE_URL}/recommendations/popular?limit=${limit}`;
  
  const response = http.get(url, { headers });
  
  const success = check(response, {
    '응답 상태 200': (r) => r && r.status === 200,
    'JSON 응답': (r) => r && r.headers && r.headers['Content-Type'] && r.headers['Content-Type'].includes('application/json'),
  });
  
  popularQuizTrend.add(response.timings.duration);
  failRate.add(!success);
  
  return response;
}

// 카테고리별 추천 테스트
function testCategoryRecommendations() {
  const tagId = tagIds[Math.floor(Math.random() * tagIds.length)];
  // 수정: 실제 백엔드의 카테고리별 추천 API 경로 사용
  const url = `${BASE_URL}/recommendations/category/${tagId}?limit=5`;
  
  const response = http.get(url, { headers });
  
  const success = check(response, {
    '응답 상태 200': (r) => r && r.status === 200,
    'JSON 응답': (r) => r && r.headers && r.headers['Content-Type'] && r.headers['Content-Type'].includes('application/json'),
  });
  
  categoryRecommendTrend.add(response.timings.duration);
  failRate.add(!success);
  
  return response;
}

// 난이도별 추천 테스트
function testDifficultyRecommendations() {
  const difficulty = difficulties[Math.floor(Math.random() * difficulties.length)];
  // 수정: 실제 백엔드의 난이도별 추천 API 경로 사용
  const url = `${BASE_URL}/recommendations/difficulty/${difficulty}?limit=5`;
  
  const response = http.get(url, { headers });
  
  const success = check(response, {
    '응답 상태 200': (r) => r && r.status === 200,
    'JSON 응답': (r) => r && r.headers && r.headers['Content-Type'] && r.headers['Content-Type'].includes('application/json'),
  });
  
  difficultyRecommendTrend.add(response.timings.duration);
  failRate.add(!success);
  
  return response;
}

// 메인 함수
export default function() {
  testQuizDetail();
  sleep(1);
  
  testQuizSearch();
  sleep(1);
  
  testPopularQuizzes();
  sleep(1);
  
  testCategoryRecommendations();
  sleep(1);
  
  testDifficultyRecommendations();
  sleep(1);
} 