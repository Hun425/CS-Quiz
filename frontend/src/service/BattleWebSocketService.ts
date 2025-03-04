// src/service/BattleWebSocketService.ts
import SockJS from 'sockjs-client';
import { Client, IFrame } from '@stomp/stompjs';
import { useAuthStore } from '../store/authStore';

type EventHandler<T> = (data: T) => void;

class BattleWebSocketService {
    private client: Client | null = null;
    private roomId: number | null = null;
    private eventHandlers: Map<string, EventHandler<any>> = new Map();
    private connected: boolean = false;

    // WebSocket 연결
    async connect(roomId: number): Promise<void> {
        if (this.connected) {
            return;
        }

        this.roomId = roomId;

        return new Promise((resolve, reject) => {
            // API 기본 URL을 사용하여 WebSocket 연결
            // 환경 변수나 설정에 따라 적절히 수정해야 합니다
            const socket = new SockJS(`http://localhost:8080/ws-battle`);

            this.client = new Client({
                webSocketFactory: () => socket,
                debug: function (str) {
                    console.log(str);
                },
                reconnectDelay: 5000,
                heartbeatIncoming: 4000,
                heartbeatOutgoing: 4000,
            });

            // 연결 성공 콜백
            this.client.onConnect = (frame: IFrame) => {
                console.log('WebSocket 연결 성공:', frame);
                this.connected = true;

                // 배틀룸 참가자 구독
                this.client!.subscribe(`/topic/battle/${roomId}/participants`, message => {
                    const data = JSON.parse(message.body);
                    this.triggerEvent('JOIN', data);
                });

                // 배틀 시작 구독
                this.client!.subscribe(`/topic/battle/${roomId}/start`, message => {
                    const data = JSON.parse(message.body);
                    this.triggerEvent('START', data);
                });

                // 배틀 진행 상황 구독
                this.client!.subscribe(`/topic/battle/${roomId}/progress`, message => {
                    const data = JSON.parse(message.body);
                    this.triggerEvent('PROGRESS', data);
                });

                // 다음 문제 구독
                this.client!.subscribe(`/topic/battle/${roomId}/question`, message => {
                    const data = JSON.parse(message.body);
                    this.triggerEvent('NEXT_QUESTION', data);
                });

                // 배틀 종료 구독
                this.client!.subscribe(`/topic/battle/${roomId}/end`, message => {
                    const data = JSON.parse(message.body);
                    this.triggerEvent('END', data);
                });

                // 개인 결과 구독 (현재 세션 ID를 사용하여 구독)
                this.client!.subscribe(`/user/queue/battle/result`, message => {
                    const data = JSON.parse(message.body);
                    this.triggerEvent('ANSWER', data);
                });

                // 배틀룸 입장 요청
                this.joinBattle();

                resolve();
            };

            // 연결 실패 콜백
            this.client.onStompError = (frame) => {
                console.error('WebSocket 오류:', frame.headers['message']);
                console.error('추가 상세정보:', frame.body);
                this.connected = false;
                reject(new Error('WebSocket 연결 오류'));
            };

            // WebSocket 활성화
            this.client.activate();
        });
    }

    // 배틀룸 참가 요청
    private joinBattle() {
        if (!this.client || !this.connected || !this.roomId) {
            console.error('WebSocket 연결이 설정되지 않았거나 roomId가 없습니다');
            return;
        }

        // 사용자 정보 가져오기
        const { user } = useAuthStore.getState();
        if (!user || !user.id) {
            console.error('사용자 정보를 찾을 수 없습니다');
            return;
        }

        // 배틀룸 참가 메시지 전송
        this.client.publish({
            destination: '/app/battle/join',
            body: JSON.stringify({
                userId: user.id,
                roomId: this.roomId,
                isReady: false
            })
        });
    }

    // 답변 제출
    submitAnswer(questionId: number, answer: string, timeSpentSeconds: number) {
        if (!this.client || !this.connected || !this.roomId) {
            console.error('WebSocket 연결이 설정되지 않았거나 roomId가 없습니다');
            return;
        }

        // 답변 제출 메시지 전송
        this.client.publish({
            destination: '/app/battle/answer',
            body: JSON.stringify({
                roomId: this.roomId,
                questionId: questionId,
                answer: answer,
                timeSpentSeconds: timeSpentSeconds
            })
        });
    }

    // 준비 상태 변경 메시지 전송 (추가 기능)
    toggleReady() {
        if (!this.client || !this.connected || !this.roomId) {
            console.error('WebSocket 연결이 설정되지 않았거나 roomId가 없습니다');
            return;
        }

        const { user } = useAuthStore.getState();
        if (!user || !user.id) {
            console.error('사용자 정보를 찾을 수 없습니다');
            return;
        }

        this.client.publish({
            destination: '/app/battle/ready',
            body: JSON.stringify({
                userId: user.id,
                roomId: this.roomId
            })
        });
    }

    // WebSocket 연결 종료
    disconnect() {
        if (this.client) {
            this.client.deactivate();
            this.client = null;
        }
        this.roomId = null;
        this.connected = false;
        this.eventHandlers.clear();
        console.log('WebSocket 연결이 종료되었습니다');
    }

    // 이벤트 핸들러 등록
    on<T>(event: string, handler: EventHandler<T>) {
        this.eventHandlers.set(event, handler);
    }

    // 이벤트 핸들러 제거
    off(event: string) {
        this.eventHandlers.delete(event);
    }

    // 이벤트 트리거
    private triggerEvent(event: string, data: any) {
        const handler = this.eventHandlers.get(event);
        if (handler) {
            handler(data);
        } else {
            console.warn(`이벤트 '${event}'에 대한 핸들러가 없습니다.`);
        }
    }

    // 연결 상태 확인
    isConnected(): boolean {
        return this.connected;
    }
}

// 싱글톤 인스턴스 생성 및 내보내기
const battleWebSocketService = new BattleWebSocketService();
export default battleWebSocketService;