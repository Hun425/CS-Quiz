// src/service/BattleWebSocketService.ts
import { client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useAuthStore } from '../store/authStore';

// 이벤트 타입 정의
type EventType = 'JOIN' | 'START' | 'PROGRESS' | 'NEXT_QUESTION' | 'END' | 'ANSWER';

// 이벤트 핸들러 타입 정의
type EventHandler<T> = (data: T) => void;

// 이벤트 핸들러 맵 타입 정의
interface EventHandlerMap {
    [key: string]: EventHandler<any>[];
}

/**
 * 배틀 WebSocket 서비스
 *
 * 실시간 배틀 진행을 위한 WebSocket 연결을 관리합니다.
 */
class BattleWebSocketService {
    private client: client | null = null;
    private roomId: number | null = null;
    private connected: boolean = false;
    private eventHandlers: EventHandlerMap = {};
    private reconnectAttempts: number = 0;
    private readonly MAX_RECONNECT_ATTEMPTS = 5;
    private readonly API_URL = 'http://localhost:8080'; // API 서버 URL

    /**
     * WebSocket 연결 초기화 및 연결
     * @param roomId 배틀룸 ID
     */
    public async connect(roomId: number): Promise<void> {
        if (this.connected && this.roomId === roomId) {
            console.log('이미 연결되어 있습니다.');
            return;
        }

        this.roomId = roomId;

        return new Promise((resolve, reject) => {
            try {
                // SockJS 및 STOMP 클라이언트 생성
                const socket = new SockJS(`${this.API_URL}/ws`);
                this.client = new client({
                    webSocketFactory: () => socket,
                    debug: function(str) {
                        // console.log('STOMP: ' + str); // 개발 시에만 활성화
                    },
                    reconnectDelay: 5000, // 재연결 시도 간격 (ms)
                    heartbeatIncoming: 4000,
                    heartbeatOutgoing: 4000
                });

                // 연결 성공 핸들러
                this.client.onConnect = (frame) => {
                    console.log('WebSocket 연결 성공:', frame);
                    this.connected = true;
                    this.reconnectAttempts = 0;

                    // 배틀룸 관련 토픽 구독
                    this.subscribeToRoomTopics(roomId);

                    // 배틀룸 입장 메시지 전송
                    this.joinBattle(roomId);

                    resolve();
                };

                // 연결 오류 핸들러
                this.client.onStompError = (frame) => {
                    console.error('STOMP 오류:', frame);
                    reject(new Error(`STOMP 오류: ${frame.headers.message}`));
                };

                // 웹소켓 연결 실패 핸들러
                socket.onerror = (error) => {
                    console.error('WebSocket 연결 오류:', error);
                    reject(new Error('WebSocket 연결 실패'));
                };

                // 연결 종료 핸들러
                this.client.onWebSocketClose = (event) => {
                    console.log('WebSocket 연결 종료:', event);
                    this.connected = false;

                    // 자동 재연결 시도
                    if (this.reconnectAttempts < this.MAX_RECONNECT_ATTEMPTS) {
                        this.reconnectAttempts++;
                        console.log(`재연결 시도 ${this.reconnectAttempts}/${this.MAX_RECONNECT_ATTEMPTS}...`);
                        setTimeout(() => this.connect(roomId), 3000);
                    }
                };

                // 클라이언트 활성화 및 연결
                this.client.activate();
            } catch (error) {
                console.error('WebSocket 서비스 초기화 오류:', error);
                reject(error);
            }
        });
    }

    /**
     * 배틀룸 관련 토픽 구독
     * @param roomId 배틀룸 ID
     */
    private subscribeToRoomTopics(roomId: number): void {
        if (!this.client || !this.connected) {
            console.error('WebSocket이 연결되지 않았습니다.');
            return;
        }

        // 참가자 변경 구독
        this.client.subscribe(`/topic/battle/${roomId}/participants`, (message) => {
            this.handleMessage('JOIN', message);
        });

        // 배틀 시작 구독
        this.client.subscribe(`/topic/battle/${roomId}/start`, (message) => {
            this.handleMessage('START', message);
        });

        // 진행 상황 구독
        this.client.subscribe(`/topic/battle/${roomId}/progress`, (message) => {
            this.handleMessage('PROGRESS', message);
        });

        // 문제 구독
        this.client.subscribe(`/topic/battle/${roomId}/question`, (message) => {
            this.handleMessage('NEXT_QUESTION', message);
        });

        // 종료 알림 구독
        this.client.subscribe(`/topic/battle/${roomId}/end`, (message) => {
            this.handleMessage('END', message);
        });

        // 개인 결과 구독 (사용자별 큐)
        const { getAccessToken } = useAuthStore.getState();
        const token = getAccessToken();

        if (token) {
            this.client.subscribe(`/user/queue/battle/result`, (message) => {
                this.handleMessage('ANSWER', message);
            }, { 'Authorization': `Bearer ${token}` });
        }
    }

    /**
     * 메시지 처리
     * @param type 이벤트 타입
     * @param message STOMP 메시지
     */
    private handleMessage(type: EventType, message: IMessage): void {
        try {
            const data = JSON.parse(message.body);
            console.log(`[${type}] 메시지 수신:`, data);

            // 등록된 이벤트 핸들러 호출
            const handlers = this.eventHandlers[type] || [];
            handlers.forEach(handler => handler(data));
        } catch (error) {
            console.error(`메시지 처리 중 오류 (${type}):`, error);
        }
    }

    /**
     * 이벤트 리스너 등록
     * @param type 이벤트 타입
     * @param handler 이벤트 핸들러
     */
    public on<T>(type: EventType, handler: EventHandler<T>): void {
        if (!this.eventHandlers[type]) {
            this.eventHandlers[type] = [];
        }
        this.eventHandlers[type].push(handler);
    }

    /**
     * 이벤트 리스너 제거
     * @param type 이벤트 타입
     * @param handler 이벤트 핸들러
     */
    public off<T>(type: EventType, handler: EventHandler<T>): void {
        if (!this.eventHandlers[type]) return;

        this.eventHandlers[type] = this.eventHandlers[type]
            .filter(h => h !== handler);
    }

    /**
     * 배틀룸 입장 메시지 전송
     * @param roomId 배틀룸 ID
     */
    private joinBattle(roomId: number): void {
        if (!this.client || !this.connected) {
            console.error('WebSocket이 연결되지 않았습니다.');
            return;
        }

        const { user } = useAuthStore.getState();
        if (!user) {
            console.error('사용자 정보가 없습니다.');
            return;
        }

        const joinRequest = {
            roomId: roomId,
            userId: user.id,
            isReady: false
        };

        this.client.publish({
            destination: '/app/battle/join',
            body: JSON.stringify(joinRequest)
        });
    }

    /**
     * 답변 제출
     * @param questionId 문제 ID
     * @param answer 답변 내용
     * @param timeSpentSeconds 소요 시간 (초)
     */
    public submitAnswer(questionId: number, answer: string, timeSpentSeconds: number): void {
        if (!this.client || !this.connected || !this.roomId) {
            console.error('WebSocket이 연결되지 않았거나 배틀룸 ID가 없습니다.');
            return;
        }

        const answerRequest = {
            roomId: this.roomId,
            questionId: questionId,
            answer: answer,
            timeSpentSeconds: timeSpentSeconds
        };

        this.client.publish({
            destination: '/app/battle/answer',
            body: JSON.stringify(answerRequest)
        });
    }

    /**
     * WebSocket 연결 종료
     */
    public disconnect(): void {
        if (this.client) {
            this.client.deactivate();
            this.client = null;
        }
        this.roomId = null;
        this.connected = false;
        this.eventHandlers = {};
    }
}

// 싱글톤 인스턴스 생성 및 내보내기
const battleWebSocketService = new BattleWebSocketService();
export default battleWebSocketService;