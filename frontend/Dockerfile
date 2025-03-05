# 빌드 단계
FROM node:18-alpine AS build
WORKDIR /app

# 의존성 파일 복사 및 설치
COPY package*.json ./
RUN npm ci

# 소스 코드 복사 및 빌드
COPY . .

# NGINX 환경 변수 설정 (백엔드 API URL)
ARG REACT_APP_API_URL=http://localhost:8080
ENV REACT_APP_API_URL=${REACT_APP_API_URL}

RUN npm run build

# 실행 단계
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# nginx 설정 복사
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 빌드 단계에서 생성된 정적 파일 복사
COPY --from=build /app/build .

# 헬스체크를 위한 설정
HEALTHCHECK --interval=30s --timeout=3s CMD wget -O /dev/null http://localhost || exit 1

# 컨테이너 실행 (nginx)
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]