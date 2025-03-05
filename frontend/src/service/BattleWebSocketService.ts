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
    private reconnectAttempts: number = 0;
    private maxReconnectAttempts: number = 3;

    // WebSocket 연결
    async connect(roomId: number): Promise<void> {
        console.log(`[WebSocket] 연결 시도: roomId=${roomId}, 현재 상태: connected=${this.connected}, connecting=${this.connecting}`);

        // 이미 연결 중이거나 동일한 방에 연결되어 있는 경우 처리
        if (this.connecting) {
            console.log('[WebSocket] 이미 연결 진행 중입니다. 중복 요청 무시');
            return Promise.resolve();
        }

        if (this.connected && this.roomId === roomId) {
            console.log('[WebSocket] 이미 이 방에 연결되어 있습니다. 중복 요청 무시');
            return Promise.resolve();
        }

        // 기존 타임아웃 정리
        if (this.connectionTimeoutId) {
            clearTimeout(this.connectionTimeoutId);
            this.connectionTimeoutId = null;
        }

        // 기존 연결이 있으면 완전히 종료 후 진행
        if (this.client && (this.connected || this.disconnecting)) {
            console.log('[WebSocket] 기존 연결 종료 후 재연결 시도');

            try {
                this.connecting = true; // 연결 중 상태로 설정하여 중복 시도 방지
                await this.disconnect();
                // 연결 해제 후 약간의 지연 시간 추가 (중요!)
                await new Promise(resolve => setTimeout(resolve, 1000));
            } catch (error) {
                console.error('[WebSocket] 이전 연결 종료 중 오류:', error);
                // 오류 발생 시에도 계속 진행
            } finally {
                // 연결 상태 초기화
                this.connected = false;
                this.disconnecting = false;
            }
        }

        this.connecting = true;
        this.roomId = roomId;
        this.reconnectAttempts = 0;

        return this.establishConnection();
    }

    // 실제 연결 설정 메서드 (재시도에 사용)
    private async establishConnection(): Promise<void> {
        return new Promise((resolve, reject) => {
            try {
                console.log(`[WebSocket] 연결 수립 시도 #${this.reconnectAttempts + 1}`);

                // 연결 타임아웃 설정 (10초)
                this.connectionTimeoutId = setTimeout(() => {
                    console.error('[WebSocket] 연결 시간 초과');
                    this.handleConnectionFailure(reject);
                }, 10000);

                // API 기본 URL을 사용하여 WebSocket 연결
                const socket = new SockJS(`http://localhost:8080/ws-battle`);

                this.client = new Client({
                    webSocketFactory: () => socket,
                    debug: (str) => {
                        if (str.includes('error') || str.includes('failed')) {
                            console.error('[WebSocket]', str);
                        } else {
                            console.log('[WebSocket]', str);
                        }
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

                    console.log('[WebSocket] 연결 성공:', frame);
                    this.connected = true;
                    this.connecting = false;
                    this.reconnectAttempts = 0;

                    if (!this.roomId) {
                        console.error('[WebSocket] roomId가 null입니다! 예상치 못한 상태');
                        this.disconnect();
                        reject(new Error("roomId가 null입니다"));
                        return;
                    }

                    // 배틀룸 참가자 구독
                    this.client!.subscribe(`/topic/battle/${this.roomId}/participants`, message => {
                        try {
                            const data = JSON.parse(message.body);
                            console.log('[WebSocket] 참가자 업데이트 수신:', data);
                            this.triggerEvent('PARTICIPANTS', data);
                        } catch (e) {
                            console.error('[WebSocket] 참가자 데이터 처리 오류:', e);
                        }
                    });

                    // 배틀 시작 구독
                    this.client!.subscribe(`/topic/battle/${this.roomId}/start`, message => {
                        try {
                            const data = JSON.parse(message.body);
                            console.log('[WebSocket] 배틀 시작 수신:', data);
                            this.triggerEvent('START', data);
                        } catch (e) {
                            console.error('[WebSocket] 배틀 시작 데이터 처리 오류:', e);
                        }
                    });

                    // 배틀 진행 상황 구독
                    this.client!.subscribe(`/topic/battle/${this.roomId}/progress`, message => {
                        try {
                            const data = JSON.parse(message.body);
                            console.log('[WebSocket] 배틀 진행 상황 수신:', data);
                            this.triggerEvent('PROGRESS', data);
                        } catch (e) {
                            console.error('[WebSocket] 진행 상황 데이터 처리 오류:', e);
                        }
                    });

                    this.client!.subscribe(`/topic/battle/${this.roomId}/question`, message => {
                        try {
                            const data = JSON.parse(message.body);
                            console.log('[WebSocket] 다음 문제 수신:', data);

                            // 게임 종료 여부 명시적으로 로깅
                            if (data.isGameOver) {
                                console.log('[WebSocket] 게임 종료 감지됨 (isGameOver=true)');
                            }

                            this.triggerEvent('NEXT_QUESTION', data);
                        } catch (e) {
                            console.error('[WebSocket] 다음 문제 데이터 처리 오류:', e);
                        }
                    });

// 배틀 종료 구독
                    this.client!.subscribe(`/topic/battle/${this.roomId}/end`, message => {
                        try {
                            const data = JSON.parse(message.body);
                            console.log('[WebSocket] 배틀 종료 수신:', data);

                            // 종료 이벤트 발생 시 명확한 로깅 추가
                            console.log('[WebSocket] 배틀 종료 이벤트 발생 - 결과 페이지로 이동 예정');

                            this.triggerEvent('END', data);

                            // 종료 이벤트가 발생했지만 처리되지 않은 경우를 대비한 안전장치 추가
                            setTimeout(() => {
                                console.log('[WebSocket] 배틀 종료 이벤트 추가 확인');
                                // 여기서는 직접 페이지 이동을 하지 않고 다시 한번 이벤트를 트리거
                                this.triggerEvent('END_CONFIRMED', data);
                            }, 3000);
                        } catch (e) {
                            console.error('[WebSocket] 배틀 종료 데이터 처리 오류:', e);
                        }
                    });

                    // 배틀 상태 변경 구독
                    this.client!.subscribe(`/topic/battle/${this.roomId}/status`, message => {
                        try {
                            const data = JSON.parse(message.body);
                            console.log('[WebSocket] 배틀 상태 변경 수신:', data);

                            // 종료 상태인 경우 명확한 로깅
                            if (data.status === 'FINISHED') {
                                console.log('[WebSocket] 배틀 FINISHED 상태 감지 - 결과 페이지로 이동 준비');
                            }

                            this.triggerEvent('STATUS', data);

                            // 종료 상태일 때 추가 종료 처리 트리거
                            if (data.status === 'FINISHED') {
                                setTimeout(() => {
                                    console.log('[WebSocket] 배틀 FINISHED 상태 추가 확인');
                                    this.triggerEvent('STATUS_FINISHED_CONFIRMED', data);
                                }, 5000);
                            }
                        } catch (e) {
                            console.error('[WebSocket] 상태 변경 데이터 처리 오류:', e);
                        }
                    });

                    // 개인 결과 구독 (현재 세션 ID를 사용하여 구독)
                    this.client!.subscribe(`/user/queue/battle/result`, message => {
                        try {
                            const data = JSON.parse(message.body);
                            console.log('[WebSocket] 개인 답변 결과 수신:', data);
                            this.triggerEvent('ANSWER', data);
                        } catch (e) {
                            console.error('[WebSocket] 답변 결과 데이터 처리 오류:', e);
                        }
                    });

                    // 배틀룸 입장 요청
                    setTimeout(() => {
                        this.joinBattle();
                    }, 500); // 구독이 완료된 후 입장 요청 (지연 추가)

                    resolve();
                };

                // 연결 실패 콜백
                this.client.onStompError = (frame) => {
                    if (this.connectionTimeoutId) {
                        clearTimeout(this.connectionTimeoutId);
                        this.connectionTimeoutId = null;
                    }

                    console.error('[WebSocket] STOMP 오류:', frame.headers['message']);
                    console.error('[WebSocket] 추가 상세정보:', frame.body);

                    this.handleConnectionFailure(reject);
                };

                // 연결 종료 콜백
                this.client.onWebSocketClose = (closeEvent: CloseEvent) => {
                    console.log('[WebSocket] 연결 닫힘:', closeEvent);

                    if (this.connecting) {
                        // 연결 시도 중 닫힘 - 재시도 고려
                        this.handleConnectionFailure(reject);
                    } else {
                        this.connected = false;
                    }
                };

                // WebSocket 활성화
                this.client.activate();

            } catch (error) {
                if (this.connectionTimeoutId) {
                    clearTimeout(this.connectionTimeoutId);
                    this.connectionTimeoutId = null;
                }

                console.error('[WebSocket] 연결 시도 중 예외 발생:', error);
                this.handleConnectionFailure(reject);
            }
        });
    }

    // 연결 실패 처리
    private handleConnectionFailure(reject: (reason?: any) => void) {
        this.reconnectAttempts++;

        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            console.log(`[WebSocket] 재연결 시도 ${this.reconnectAttempts}/${this.maxReconnectAttempts}`);

            // 지수 백오프로 재시도 (1초, 2초, 4초...)
            const backoffTime = Math.min(1000 * Math.pow(2, this.reconnectAttempts - 1), 8000);

            setTimeout(() => {
                this.establishConnection()
                    .then(() => console.log('[WebSocket] 재연결 성공!'))
                    .catch(err => {
                        console.error('[WebSocket] 최종 연결 실패:', err);
                        this.connected = false;
                        this.connecting = false;
                        reject(err);
                    });
            }, backoffTime);
        } else {
            console.error('[WebSocket] 최대 재연결 시도 횟수 초과');
            this.connected = false;
            this.connecting = false;
            reject(new Error('WebSocket 연결 실패: 최대 재시도 횟수 초과'));
        }
    }

    // 배틀룸 참가 요청
    private joinBattle() {
        if (!this.client || !this.connected || !this.roomId) {
            console.error('[WebSocket] joinBattle 실패: 연결이 설정되지 않았거나 roomId가 없습니다');
            return;
        }

        // 사용자 정보 가져오기
        const { user } = useAuthStore.getState();
        if (!user || !user.id) {
            console.error('[WebSocket] joinBattle 실패: 사용자 정보를 찾을 수 없습니다');
            return;
        }

        // 배틀룸 참가 메시지 전송
        try {
            console.log('[WebSocket] 배틀룸 참가 요청 전송:', { userId: user.id, roomId: this.roomId });

            this.client.publish({
                destination: '/app/battle/join',
                body: JSON.stringify({
                    userId: user.id,
                    roomId: this.roomId,
                    isReady: false
                })
            });

            console.log('[WebSocket] 배틀룸 참가 요청 전송 완료');
        } catch (error) {
            console.error('[WebSocket] 배틀룸 참가 요청 전송 중 오류:', error);
        }
    }

    // 답변 제출
    // 답변 제출
    submitAnswer(questionId: number, answer: string, timeSpentSeconds: number) {
        if (!this.client || !this.connected || !this.roomId) {
            console.error('[WebSocket] 답변 제출 실패: 연결이 설정되지 않았거나 roomId가 없습니다');
            return;
        }

        const { user } = useAuthStore.getState();
        if (!user || !user.id) {
            console.error('[WebSocket] 답변 제출 실패: 사용자 정보를 찾을 수 없습니다');
            return;
        }

        // 답변 제출 메시지 전송
        try {
            console.log('[WebSocket] 답변 제출:', { questionId, answer, timeSpentSeconds });

            // 문제 ID에 대한 유효성 검사 추가
            if (!questionId) {
                console.error('[WebSocket] 답변 제출 실패: 문제 ID가 없습니다');
                return;
            }

            this.client.publish({
                destination: '/app/battle/answer',
                body: JSON.stringify({
                    roomId: this.roomId,
                    questionId: questionId,  // 문제 ID를 정확히 전달
                    answer: answer,
                    timeSpentSeconds: timeSpentSeconds
                })
            });

            console.log('[WebSocket] 답변 제출 완료');
        } catch (error) {
            console.error('[WebSocket] 답변 제출 중 오류:', error);
        }
    }

    // 준비 상태 토글
    toggleReady() {
        if (!this.client || !this.connected || !this.roomId) {
            console.error('[WebSocket] 준비 상태 토글 실패: 연결이 설정되지 않았거나 roomId가 없습니다');
            return;
        }

        const { user } = useAuthStore.getState();
        if (!user || !user.id) {
            console.error('[WebSocket] 준비 상태 토글 실패: 사용자 정보를 찾을 수 없습니다');
            return;
        }

        // 준비 상태 토글 메시지 전송
        try {
            console.log('[WebSocket] 준비 상태 토글 요청 전송:', { userId: user.id, roomId: this.roomId });

            this.client.publish({
                destination: '/app/battle/ready',
                body: JSON.stringify({
                    userId: user.id,
                    roomId: this.roomId
                })
            });

            console.log('[WebSocket] 준비 상태 토글 요청 전송 완료');
        } catch (error) {
            console.error('[WebSocket] 준비 상태 토글 요청 전송 중 오류:', error);
        }
    }

    // WebSocket 연결 종료
    async disconnect(): Promise<void> {
        console.log('[WebSocket] 연결 종료 요청');

        if (!this.client) {
            console.log('[WebSocket] 연결을 종료할 수 없음: 클라이언트가 없음');
            return Promise.resolve();
        }

        if (this.disconnecting) {
            console.log('[WebSocket] 이미 연결 종료 중');
            return Promise.resolve();
        }

        this.disconnecting = true;

        return new Promise<void>((resolve) => {
            try {
                // 기존 이벤트 핸들러 정리
                this.eventHandlers.clear();
                console.log('[WebSocket] 이벤트 핸들러 정리 완료');

                // 타임아웃 설정 - 연결 종료가 지연될 경우를 대비
                const timeoutId = setTimeout(() => {
                    console.log('[WebSocket] 연결 종료 타임아웃 - 강제 종료');
                    this.forceCleanup();
                    resolve();
                }, 3000);

                if (this.client && this.client.connected) {
                    // 정상 종료를 위한 이벤트 핸들러
                    this.client.onDisconnect = () => {
                        clearTimeout(timeoutId);
                        console.log('[WebSocket] 연결이 정상적으로 종료됨');
                        this.forceCleanup();
                        resolve();
                    };

                    // 연결 해제 시도
                    console.log('[WebSocket] 클라이언트 deactivate 호출');
                    this.client.deactivate();
                } else {
                    // 클라이언트가 없거나 연결되지 않은 경우
                    clearTimeout(timeoutId);
                    console.log('[WebSocket] 연결되지 않은 상태에서 종료 처리');
                    this.forceCleanup();
                    resolve();
                }
            } catch (error) {
                console.error('[WebSocket] 종료 중 오류:', error);
                this.forceCleanup();
                console.log('[WebSocket] 오류와 함께 강제 종료 처리');
                resolve();
            }
        });
    }

    // 강제 정리
    private forceCleanup() {
        this.client = null;
        this.roomId = null;
        this.connected = false;
        this.connecting = false;
        this.disconnecting = false;
        this.eventHandlers.clear();
    }

    // 이벤트 핸들러 등록
    on<T>(event: string, handler: EventHandler<T>) {
        // 기존 핸들러가 있다면 제거 후 새로운 핸들러 등록
        if (this.eventHandlers.has(event)) {
            console.log(`[WebSocket] 이벤트 '${event}'의 기존 핸들러를 대체합니다.`);
        }
        this.eventHandlers.set(event, handler);
        console.log(`[WebSocket] '${event}' 이벤트 핸들러 등록됨`);
    }

    // 이벤트 핸들러 제거
    off(event: string) {
        const wasRemoved = this.eventHandlers.delete(event);
        if (wasRemoved) {
            console.log(`[WebSocket] '${event}' 이벤트 핸들러 제거됨`);
        } else {
            console.log(`[WebSocket] '${event}' 이벤트 핸들러가 존재하지 않습니다.`);
        }
    }

    // 모든 이벤트 핸들러 제거
    clearAllHandlers() {
        const handlerCount = this.eventHandlers.size;
        this.eventHandlers.clear();
        console.log(`[WebSocket] 모든 이벤트 핸들러가 제거되었습니다. (총 ${handlerCount}개)`);
    }

// 이벤트 트리거 함수 강화
    // 수정된 코드
    // 수정된 코드:
    triggerEvent(eventName: string, data: any) {
        const handler = this.eventHandlers.get(eventName);
        if (!handler) {
            console.warn(`[WebSocket] 이벤트 '${eventName}'에 대한 핸들러가 없습니다!`);

            if (eventName === 'END_CONFIRMED' || eventName === 'STATUS_FINISHED_CONFIRMED') {
                console.log(`[WebSocket] '${eventName}' 이벤트 기본 처리 적용`);

                // sessionStorage에서 룸 ID 가져오기 시도
                const storedRoomId = sessionStorage.getItem('lastBattleRoomId');
                const currentRoomId = this.getCurrentRoomId() || (storedRoomId ? parseInt(storedRoomId) : null);

                console.log(`[WebSocket] 현재 roomId: ${currentRoomId}, 소스: ${this.getCurrentRoomId() ? 'service' : 'sessionStorage'}`);

                if (currentRoomId) {
                    window.location.href = `/battles/${currentRoomId}/results`;
                } else {
                    // roomId를 찾을 수 없는 경우 배틀 목록 페이지로 이동
                    console.error('[WebSocket] roomId를 찾을 수 없어 배틀 목록으로 이동합니다');
                    window.location.href = '/battles';
                }
            }
            return;
        }

        handler(data);
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

    // 연결 상태 상세 정보
    getConnectionStatus(): {connected: boolean, connecting: boolean, disconnecting: boolean, roomId: number | null} {
        return {
            connected: this.connected,
            connecting: this.connecting,
            disconnecting: this.disconnecting,
            roomId: this.roomId
        };
    }
}

// 싱글톤 인스턴스 생성 및 내보내기
const battleWebSocketService = new BattleWebSocketService();
export default battleWebSocketService;