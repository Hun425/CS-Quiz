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
    private connecting: boolean = false;
    private disconnecting: boolean = false;
    private connectionTimeoutId: NodeJS.Timeout | null = null;

    // WebSocket 연결
    async connect(roomId: number): Promise<void> {
        // 이미 연결 중이거나 동일한 방에 연결되어 있는 경우 처리
        if (this.connecting) {
            console.log('연결 프로세스가 이미 진행 중입니다.');
            return Promise.resolve();
        }

        if (this.connected && this.roomId === roomId) {
            console.log('이미 이 방에 연결되어 있습니다.');
            return Promise.resolve();
        }

        // 기존 타임아웃 정리
        if (this.connectionTimeoutId) {
            clearTimeout(this.connectionTimeoutId);
            this.connectionTimeoutId = null;
        }

        // 연결 중이거나 해제 중인 경우, 기존 연결을 정리
        if (this.connected || this.disconnecting) {
            await this.disconnect();
            // 연결 해제 후 약간의 지연 시간 추가
            await new Promise(resolve => setTimeout(resolve, 1000));
        }

        this.connecting = true;
        this.roomId = roomId;

        return new Promise((resolve, reject) => {
            try {
                // 연결 타임아웃 설정 (10초)
                this.connectionTimeoutId = setTimeout(() => {
                    console.error('WebSocket 연결 시간 초과');
                    this.connecting = false;
                    reject(new Error('WebSocket 연결 시간 초과'));
                }, 10000);

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
                    if (this.connectionTimeoutId) {
                        clearTimeout(this.connectionTimeoutId);
                        this.connectionTimeoutId = null;
                    }

                    console.log('WebSocket 연결 성공:', frame);
                    this.connected = true;
                    this.connecting = false;

                    // 배틀룸 참가자 구독
                    this.client!.subscribe(`/topic/battle/${roomId}/participants`, message => {
                        const data = JSON.parse(message.body);
                        console.log('참가자 업데이트 수신:', data);
                        this.triggerEvent('PARTICIPANTS', data);
                    });

                    // 배틀 시작 구독
                    this.client!.subscribe(`/topic/battle/${roomId}/start`, message => {
                        const data = JSON.parse(message.body);
                        console.log('배틀 시작 수신:', data);
                        this.triggerEvent('START', data);
                    });

                    // 배틀 진행 상황 구독
                    this.client!.subscribe(`/topic/battle/${roomId}/progress`, message => {
                        const data = JSON.parse(message.body);
                        console.log('배틀 진행 상황 수신:', data);
                        this.triggerEvent('PROGRESS', data);
                    });

                    // 다음 문제 구독
                    this.client!.subscribe(`/topic/battle/${roomId}/question`, message => {
                        const data = JSON.parse(message.body);
                        console.log('다음 문제 수신:', data);
                        this.triggerEvent('NEXT_QUESTION', data);
                    });

                    // 배틀 종료 구독
                    this.client!.subscribe(`/topic/battle/${roomId}/end`, message => {
                        const data = JSON.parse(message.body);
                        console.log('배틀 종료 수신:', data);
                        this.triggerEvent('END', data);
                    });

                    // 배틀 상태 변경 구독
                    this.client!.subscribe(`/topic/battle/${roomId}/status`, message => {
                        const data = JSON.parse(message.body);
                        console.log('배틀 상태 변경 수신:', data);
                        this.triggerEvent('STATUS', data);
                    });

                    // 개인 결과 구독 (현재 세션 ID를 사용하여 구독)
                    this.client!.subscribe(`/user/queue/battle/result`, message => {
                        const data = JSON.parse(message.body);
                        console.log('개인 답변 결과 수신:', data);
                        this.triggerEvent('ANSWER', data);
                    });

                    // 배틀룸 입장 요청
                    this.joinBattle();

                    resolve();
                };

                // 연결 실패 콜백
                this.client.onStompError = (frame) => {
                    if (this.connectionTimeoutId) {
                        clearTimeout(this.connectionTimeoutId);
                        this.connectionTimeoutId = null;
                    }

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
                if (this.connectionTimeoutId) {
                    clearTimeout(this.connectionTimeoutId);
                    this.connectionTimeoutId = null;
                }

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
            console.log('배틀룸 참가 요청 전송 완료:', { userId: user.id, roomId: this.roomId });
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

        const { user } = useAuthStore.getState();
        if (!user || !user.id) {
            console.error('사용자 정보를 찾을 수 없습니다');
            return;
        }

        // 답변 제출 메시지 전송
        try {
            this.client.publish({
                destination: '/app/battle/answer',
                body: JSON.stringify({
                    roomId: this.roomId,
                    questionId: questionId,
                    answer: answer,
                    timeSpentSeconds: timeSpentSeconds
                })
            });
            console.log('답변 제출 완료:', { questionId, answer, timeSpentSeconds });
        } catch (error) {
            console.error('답변 제출 중 오류:', error);
        }
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
        try {
            this.client.publish({
                destination: '/app/battle/ready',
                body: JSON.stringify({
                    userId: user.id,
                    roomId: this.roomId
                })
            });
            console.log('준비 상태 토글 요청 전송 완료:', { userId: user.id, roomId: this.roomId });
        } catch (error) {
            console.error('준비 상태 토글 요청 전송 중 오류:', error);
        }
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
                // 기존 이벤트 핸들러 정리
                this.eventHandlers.clear();

                // 타임아웃 설정 - 연결 종료가 지연될 경우를 대비
                const timeoutId = setTimeout(() => {
                    console.log('WebSocket 연결 종료 타임아웃 - 강제 종료');
                    this.client = null;
                    this.roomId = null;
                    this.connected = false;
                    this.disconnecting = false;
                    resolve();
                }, 3000);

                if (this.client && this.client.connected) {
                    // 정상 종료를 위한 이벤트 핸들러
                    this.client.onDisconnect = () => {
                        clearTimeout(timeoutId);
                        this.client = null;
                        this.roomId = null;
                        this.connected = false;
                        this.disconnecting = false;
                        console.log('WebSocket 연결이 완전히 종료되었습니다');
                        resolve();
                    };

                    this.client.deactivate();
                } else {
                    // 클라이언트가 없거나 연결되지 않은 경우
                    clearTimeout(timeoutId);
                    this.client = null;
                    this.roomId = null;
                    this.connected = false;
                    this.disconnecting = false;
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
        // 기존 핸들러가 있다면 제거 후 새로운 핸들러 등록
        if (this.eventHandlers.has(event)) {
            console.log(`이벤트 '${event}'의 기존 핸들러를 대체합니다.`);
        }
        this.eventHandlers.set(event, handler);
        console.log(`'${event}' 이벤트 핸들러 등록됨`);
    }

    // 이벤트 핸들러 제거
    off(event: string) {
        const wasRemoved = this.eventHandlers.delete(event);
        if (wasRemoved) {
            console.log(`'${event}' 이벤트 핸들러 제거됨`);
        } else {
            console.log(`'${event}' 이벤트 핸들러가 존재하지 않습니다.`);
        }
    }

    // 모든 이벤트 핸들러 제거
    clearAllHandlers() {
        const handlerCount = this.eventHandlers.size;
        this.eventHandlers.clear();
        console.log(`모든 이벤트 핸들러가 제거되었습니다. (총 ${handlerCount}개)`);
    }

    // 이벤트 트리거
    private triggerEvent(event: string, data: any) {
        const handler = this.eventHandlers.get(event);
        if (handler) {
            console.log(`'${event}' 이벤트 트리거됨:`, data);
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

    // 현재 방 ID 가져오기
    getCurrentRoomId(): number | null {
        return this.roomId;
    }
}

// 싱글톤 인스턴스 생성 및 내보내기
const battleWebSocketService = new BattleWebSocketService();
export default battleWebSocketService;