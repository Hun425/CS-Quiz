// BattleWebSocketService.ts 수정

import SockJS from 'sockjs-client';
import { Client, IFrame } from '@stomp/stompjs';
import { useAuthStore } from '../store/authStore';

type EventHandler<T> = (data: T) => void;

class BattleWebSocketService {
    private client: Client | null = null;
    private roomId: number | null = null;
    private eventHandlers: Map<string, EventHandler<any>> = new Map();
    private connected: boolean = false;
    private connecting: boolean = false;
    private disconnecting: boolean = false;

    // WebSocket 연결
    async connect(roomId: number): Promise<void> {
        // 이미 연결 중이거나 동일한 방에 연결되어 있는 경우 처리
        if (this.connecting) {
            console.log('연결 프로세스가 이미 진행 중입니다.');
            return;
        }

        if (this.connected && this.roomId === roomId) {
            console.log('이미 이 방에 연결되어 있습니다.');
            return;
        }

        // 연결 중이거나 해제 중인 경우, 기존 연결을 정리
        if (this.connected || this.disconnecting) {
            await this.disconnect();
            // 연결 해제 후 약간의 지연 시간 추가
            await new Promise(resolve => setTimeout(resolve, 500));
        }

        this.connecting = true;
        this.roomId = roomId;

        return new Promise((resolve, reject) => {
            try {
                // API 기본 URL을 사용하여 WebSocket 연결
                const socket = new SockJS(`http://localhost:8080/ws-battle`);

                this.client = new Client({
                    webSocketFactory: () => socket,
                    debug: (str) => {
                        console.log('WebSocket Debug:', str);
                    },
                    reconnectDelay: 5000,
                    heartbeatIncoming: 4000,
                    heartbeatOutgoing: 4000,
                });

                // 연결 성공 콜백
                this.client.onConnect = (frame: IFrame) => {
                    console.log('WebSocket 연결 성공:', frame);
                    this.connected = true;
                    this.connecting = false;

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
                    this.connecting = false;
                    reject(new Error('WebSocket 연결 오류'));
                };

                // 연결 종료 콜백
                this.client.onWebSocketClose = (closeEvent: CloseEvent) => {
                    console.log('WebSocket 연결 닫힘:', closeEvent);
                    this.connected = false;
                    this.connecting = false;
                };

                // WebSocket 활성화
                this.client.activate();
            } catch (error) {
                console.error('WebSocket 연결 시도 중 예외 발생:', error);
                this.connected = false;
                this.connecting = false;
                reject(error);
            }
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
        try {
            this.client.publish({
                destination: '/app/battle/join',
                body: JSON.stringify({
                    userId: user.id,
                    roomId: this.roomId,
                    isReady: false
                })
            });
            console.log('배틀룸 참가 요청 전송 완료');
        } catch (error) {
            console.error('배틀룸 참가 요청 전송 중 오류:', error);
        }
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

    // 준비 상태 토글
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

        // 준비 상태 토글 메시지 전송
        this.client.publish({
            destination: '/app/battle/ready',
            body: JSON.stringify({
                userId: user.id,
                roomId: this.roomId
            })
        });
    }

    // WebSocket 연결 종료
    async disconnect(): Promise<void> {
        if (!this.client || this.disconnecting) {
            console.log('연결을 종료할 수 없음: 클라이언트가 없거나 이미 종료 중');
            return Promise.resolve();
        }

        this.disconnecting = true;

        return new Promise<void>((resolve) => {
            try {
                if (this.client && this.client.connected) {
                    // 정상 종료를 위한 이벤트 핸들러
                    const onDisconnect = () => {
                        this.client = null;
                        this.roomId = null;
                        this.connected = false;
                        this.disconnecting = false;
                        this.eventHandlers.clear();
                        console.log('WebSocket 연결이 완전히 종료되었습니다');
                        resolve();
                    };

                    // 종료 후 클린업
                    this.client.onDisconnect = onDisconnect;
                    this.client.deactivate();
                } else {
                    // 클라이언트가 없거나 연결되지 않은 경우
                    this.client = null;
                    this.roomId = null;
                    this.connected = false;
                    this.disconnecting = false;
                    this.eventHandlers.clear();
                    console.log('WebSocket 연결이 종료되었습니다');
                    resolve();
                }
            } catch (error) {
                console.error('WebSocket 종료 중 오류:', error);
                this.client = null;
                this.roomId = null;
                this.connected = false;
                this.disconnecting = false;
                this.eventHandlers.clear();
                console.log('WebSocket 연결이 오류와 함께 종료되었습니다');
                resolve();
            }
        });
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

    // 연결 중인지 확인
    isConnecting(): boolean {
        return this.connecting;
    }
}

// 싱글톤 인스턴스 생성 및 내보내기
const battleWebSocketService = new BattleWebSocketService();
export default battleWebSocketService;