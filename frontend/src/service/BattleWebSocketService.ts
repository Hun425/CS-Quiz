// src/services/BattleWebSocketService.ts
import { useAuthStore } from '../store/authStore';
import { BattleJoinResponse, BattleAnswerResponse, BattleStartResponse, BattleEndResponse, BattleProgressResponse, BattleNextQuestionResponse } from '../types/api';

// WebSocket 메시지 타입
type MessageType = 'JOIN' | 'ANSWER' | 'START' | 'END' | 'PROGRESS' | 'NEXT_QUESTION';

// WebSocket 콜백 타입
type CallbackType<T> = (data: T) => void;

/**
 * 배틀 WebSocket 서비스
 * 실시간 퀴즈 대결 기능을 위한 WebSocket 통신을 처리합니다.
 */
class BattleWebSocketService {
    private socket: WebSocket | null = null;
    private roomId: number | null = null;
    private userId: number | null = null;
    private sessionId: string = '';
    private callbacks: Map<MessageType, CallbackType<any>> = new Map();
    private reconnectAttempts = 0;
    private maxReconnectAttempts = 5;
    private reconnectTimeout: number | null = null;

    // 서버 주소 설정
    private readonly SERVER_URL = 'ws://localhost:8080/ws';

    /**
     * 배틀룸에 연결
     */
    public connect(roomId: number): Promise<boolean> {
        return new Promise((resolve, reject) => {
            // 사용자 인증 정보 가져오기
            const { user, accessToken } = useAuthStore.getState();

            if (!user || !accessToken) {
                reject(new Error('사용자 인증 정보가 없습니다.'));
                return;
            }

            this.roomId = roomId;
            this.userId = user.id;

            // 이미 연결된 경우 재사용
            if (this.socket && this.socket.readyState === WebSocket.OPEN) {
                resolve(true);
                return;
            }

            // 기존 연결 종료
            this.disconnect();

            try {
                // WebSocket 연결 생성 (인증 토큰 포함)
                this.socket = new WebSocket(`${this.SERVER_URL}?token=${accessToken}`);

                this.socket.onopen = () => {
                    console.log('WebSocket 연결 성공');
                    this.reconnectAttempts = 0;
                    this.joinBattle();
                    resolve(true);
                };

                this.socket.onclose = (event) => {
                    console.log('WebSocket 연결 종료:', event.code, event.reason);
                    this.handleReconnect();
                };

                this.socket.onerror = (error) => {
                    console.error('WebSocket 오류:', error);
                    reject(error);
                };

                this.socket.onmessage = (event) => {
                    this.handleMessage(event.data);
                };
            } catch (error) {
                console.error('WebSocket 연결 실패:', error);
                reject(error);
            }
        });
    }

    /**
     * 연결 종료
     */
    public disconnect(): void {
        if (this.socket) {
            this.socket.close();
            this.socket = null;
        }

        if (this.reconnectTimeout) {
            clearTimeout(this.reconnectTimeout);
            this.reconnectTimeout = null;
        }

        this.roomId = null;
        this.callbacks.clear();
    }

    /**
     * 배틀룸 입장
     */
    private joinBattle(): void {
        if (!this.socket || this.socket.readyState !== WebSocket.OPEN || !this.roomId || !this.userId) {
            return;
        }

        // 입장 메시지 전송
        const joinMessage = {
            destination: '/app/battle/join',
            body: JSON.stringify({
                userId: this.userId,
                roomId: this.roomId,
                isReady: false
            })
        };

        this.sendMessage(joinMessage);
    }

    /**
     * 준비 상태 토글
     */
    public toggleReady(): void {
        if (!this.socket || this.socket.readyState !== WebSocket.OPEN || !this.roomId || !this.userId) {
            return;
        }

        // 준비 상태 토글 메시지 전송
        const readyMessage = {
            destination: '/app/battle/ready',
            body: JSON.stringify({
                userId: this.userId,
                roomId: this.roomId
            })
        };

        this.sendMessage(readyMessage);
    }

    /**
     * 답변 제출
     */
    public submitAnswer(questionId: number, answer: string, timeSpentSeconds: number): void {
        if (!this.socket || this.socket.readyState !== WebSocket.OPEN || !this.roomId || !this.userId) {
            return;
        }

        // 답변 제출 메시지 전송
        const answerMessage = {
            destination: '/app/battle/answer',
            body: JSON.stringify({
                roomId: this.roomId,
                questionId: questionId,
                answer: answer,
                timeSpentSeconds: timeSpentSeconds
            })
        };

        this.sendMessage(answerMessage);
    }

    /**
     * 메시지 전송
     */
    private sendMessage(message: { destination: string; body: string }): void {
        if (!this.socket || this.socket.readyState !== WebSocket.OPEN) {
            console.error('WebSocket이 연결되지 않았습니다.');
            return;
        }

        // STOMP 프레임 형식으로 메시지 전송
        const frame = `SEND\ndestination:${message.destination}\n\n${message.body}\n\0`;
        this.socket.send(frame);
    }

    /**
     * 수신 메시지 처리
     */
    private handleMessage(data: string): void {
        try {
            // STOMP 프레임 파싱
            const frameLines = data.split('\n');
            const command = frameLines[0];

            // 헤더와 본문 분리
            const headerEndIndex = frameLines.indexOf('');
            const headers = this.parseHeaders(frameLines.slice(1, headerEndIndex));
            const body = frameLines.slice(headerEndIndex + 1).join('\n').replace('\0', '');

            // 메시지 타입 결정
            let messageType: MessageType | null = null;
            const destination = headers.destination || '';

            if (destination.includes('/participants')) {
                messageType = 'JOIN';
            } else if (destination.includes('/start')) {
                messageType = 'START';
            } else if (destination.includes('/progress')) {
                messageType = 'PROGRESS';
            } else if (destination.includes('/question')) {
                messageType = 'NEXT_QUESTION';
            } else if (destination.includes('/end')) {
                messageType = 'END';
            } else if (destination.includes('/result')) {
                messageType = 'ANSWER';
            }

            // 세션 ID 저장
            if (command === 'CONNECTED' && headers['session-id']) {
                this.sessionId = headers['session-id'];
            }

            // 콜백 호출
            if (messageType && this.callbacks.has(messageType) && body) {
                const callback = this.callbacks.get(messageType);
                if (callback) {
                    try {
                        const parsedData = JSON.parse(body);
                        callback(parsedData);
                    } catch (e) {
                        console.error('메시지 파싱 오류:', e);
                    }
                }
            }
        } catch (error) {
            console.error('메시지 처리 오류:', error);
        }
    }

    /**
     * STOMP 헤더 파싱
     */
    private parseHeaders(headerLines: string[]): Record<string, string> {
        const headers: Record<string, string> = {};
        headerLines.forEach(line => {
            const [key, value] = line.split(':');
            if (key && value) {
                headers[key.trim()] = value.trim();
            }
        });
        return headers;
    }

    /**
     * 재연결 처리
     */
    private handleReconnect(): void {
        if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            console.log('최대 재연결 시도 횟수 초과');
            return;
        }

        this.reconnectAttempts++;
        const delay = Math.min(1000 * 2 ** this.reconnectAttempts, 30000);

        console.log(`${delay}ms 후 재연결 시도 (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);

        this.reconnectTimeout = window.setTimeout(() => {
            if (this.roomId) {
                this.connect(this.roomId).catch(error => {
                    console.error('재연결 실패:', error);
                });
            }
        }, delay);
    }

    /**
     * 이벤트 핸들러 등록
     */
    public on<T>(event: MessageType, callback: CallbackType<T>): void {
        this.callbacks.set(event, callback);
    }

    /**
     * 이벤트 핸들러 제거
     */
    public off(event: MessageType): void {
        this.callbacks.delete(event);
    }

    /**
     * 배틀룸 나가기
     */
    public leaveBattle(): void {
        this.disconnect();
    }
}

// 싱글톤 인스턴스 생성 및 내보내기
export const battleWebSocketService = new BattleWebSocketService();
export default battleWebSocketService;