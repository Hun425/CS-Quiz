// 게임 배틀 관련 WebSocket 서비스

import SockJS from "sockjs-client";
import { Client, IFrame } from "@stomp/stompjs";
import { useProfileStore } from "@/store/profileStore";
import { BattleWebSocketEvents } from "@/lib/types/websocket";
import { BattleStatus, Participant } from "../types/battle";

// ✅ 환경 변수에서 WebSocket URL 가져오기
const WS_BASE_URL =
  process.env.NEXT_PUBLIC_WS_BASE_URL || "ws://localhost:8080/ws-battle";

type EventHandlers = {
  [K in keyof BattleWebSocketEvents]?: (data: BattleWebSocketEvents[K]) => void;
};

class BattleWebSocketService {
  private client: Client | null = null;
  private roomId: number | null = null;
  private eventHandlers: EventHandlers = {};
  private connected: boolean = false;
  private connecting: boolean = false;
  private disconnecting: boolean = false;
  private connectionTimeoutId: NodeJS.Timeout | null = null;
  private reconnectAttempts: number = 0;
  private maxReconnectAttempts: number = 3;

  // ✅ WebSocket 연결
  async connect(roomId: number, userId: number): Promise<void> {
    if (!userId) {
      console.error("WebSocket 연결 불가: userId가 없음");
      return;
    }

    // 이미 연결 중이거나 동일한 방에 연결된 경우 무시
    if (this.connecting || (this.connected && this.roomId === roomId)) {
      console.log("이미 연결되어 있거나 진행 중입니다.");
      return;
    }

    // 기존 연결이 있으면 해제 후 다시 연결
    if (this.connected || this.disconnecting) {
      await this.disconnect();
      await new Promise((resolve) => setTimeout(resolve, 1000)); // 약간의 지연 시간 추가
    }

    this.connecting = true;
    this.roomId = roomId;
    this.reconnectAttempts = 0; // 재연결 횟수 초기화

    return new Promise((resolve, reject) => {
      try {
        // 연결 타임아웃 설정 (10초)
        this.connectionTimeoutId = setTimeout(() => {
          console.error("WebSocket 연결 시간 초과");
          this.connecting = false;
          reject(new Error("WebSocket 연결 시간 초과"));
        }, 10000);

        // ✅ WebSocket URL 사용
        const socket = new SockJS(WS_BASE_URL);

        this.client = new Client({
          webSocketFactory: () => socket,
          debug: (str) => {
            console.log("WebSocket Debug:", str);
          },
          reconnectDelay: 5000,
          heartbeatIncoming: 4000,
          heartbeatOutgoing: 4000,
        });

        // 연결 성공 콜백
        this.client.onConnect = (frame: IFrame) => {
          clearTimeout(this.connectionTimeoutId!);
          this.connectionTimeoutId = null;

          console.log("WebSocket 연결 성공:", frame);
          this.connected = true;
          this.connecting = false;

          // ✅ 배틀룸 이벤트 구독
          this.subscribeToBattleEvents(roomId);

          // 배틀룸 참가 요청
          this.joinBattle(userId);

          resolve();
        };

        // 연결 실패 콜백
        this.client.onStompError = (frame) => {
          clearTimeout(this.connectionTimeoutId!);
          this.connectionTimeoutId = null;

          console.error("WebSocket 오류:", frame.headers["message"]);
          this.connected = false;
          this.connecting = false;
          reject(new Error("WebSocket 연결 오류"));
        };

        // 연결 종료 시 자동 재연결
        this.client.onWebSocketClose = () => {
          console.log("WebSocket 연결 끊김");
          this.connected = false;
          this.handleReconnect(roomId, userId);
        };

        // WebSocket 활성화
        this.client.activate();
      } catch (error) {
        clearTimeout(this.connectionTimeoutId!);
        this.connectionTimeoutId = null;
        console.error("WebSocket 연결 중 오류:", error);
        this.connected = false;
        this.connecting = false;
        reject(error);
      }
    });
  }

  // ✅ WebSocket 자동 재연결 (최대 3번)
  private handleReconnect(roomId: number, userId: number) {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error("WebSocket 최대 재연결 횟수 도달");
      return;
    }

    this.reconnectAttempts++;
    console.log(
      `WebSocket 재연결 시도 (${this.reconnectAttempts}/${this.maxReconnectAttempts})`
    );

    setTimeout(() => {
      this.connect(roomId, userId);
    }, 5000 * this.reconnectAttempts);
  }

  // ✅ 배틀 이벤트 구독
  private subscribeToBattleEvents(roomId: number) {
    if (!this.client) return;

    this.client.subscribe(`/topic/battle/${roomId}/participants`, (message) => {
      const data: Participant[] = JSON.parse(message.body);
      console.log("참가자 업데이트 수신:", data);
      this.triggerEvent("PARTICIPANTS", data);
    });

    this.client.subscribe(`/topic/battle/${roomId}/start`, (message) => {
      const data = JSON.parse(message.body) as { startTime: string };
      console.log("배틀 시작 수신:", data);
      this.triggerEvent("START", data);
    });

    this.client.subscribe(`/topic/battle/${roomId}/progress`, (message) => {
      const data = JSON.parse(message.body) as {
        questionId: number;
        status: string;
      };
      console.log("배틀 진행 상황 수신:", data);
      this.triggerEvent("PROGRESS", data);
    });

    this.client.subscribe(`/topic/battle/${roomId}/end`, (message) => {
      const data = JSON.parse(message.body) as { winnerId: number };
      console.log("배틀 종료 수신:", data);
      this.triggerEvent("END", data);
    });

    this.client.subscribe(`/topic/battle/${roomId}/status`, (message) => {
      const data = JSON.parse(message.body) as { status: BattleStatus };
      console.log("배틀 상태 변경 수신:", data);
      this.triggerEvent("STATUS", data);
    });
  }
  // ✅ 배틀룸 참가 요청
  private joinBattle(userId: number) {
    if (!this.client || !this.connected || !this.roomId) return;

    try {
      this.client.publish({
        destination: "/app/battle/join",
        body: JSON.stringify({ userId, roomId: this.roomId, isReady: false }),
      });

      console.log("배틀룸 참가 요청 전송 완료");
    } catch (error) {
      console.error("배틀룸 참가 요청 전송 중 오류:", error);
    }
  }

  // ✅ 준비 상태 토글
  toggleReady() {
    if (!this.client || !this.connected || !this.roomId) return;

    // ✅ React Hook 대신 getState()로 userId 가져오기
    const userId = useProfileStore.getState().userProfile?.id;

    if (!userId) {
      console.error("사용자 정보를 찾을 수 없습니다");
      return;
    }

    try {
      this.client.publish({
        destination: "/app/battle/ready",
        body: JSON.stringify({ userId: userId, roomId: this.roomId }),
      });

      console.log("준비 상태 변경 요청 전송 완료");
    } catch (error) {
      console.error("준비 상태 변경 요청 중 오류:", error);
    }
  }

  // ✅ 이벤트 핸들러 등록
  on<K extends keyof BattleWebSocketEvents>(
    event: K,
    handler: (data: BattleWebSocketEvents[K]) => void
  ) {
    this.eventHandlers[event] = handler as unknown as EventHandlers[K];
    console.log(`'${event}' 이벤트 핸들러 등록됨`);
  }

  // ✅ 특정 이벤트 핸들러 제거
  off<K extends keyof BattleWebSocketEvents>(event: K) {
    delete this.eventHandlers[event];
    console.log(`'${event}' 이벤트 핸들러 제거됨`);
  }

  // ✅ 이벤트 핸들러 초기화 함수
  // ✅ 모든 이벤트 핸들러 초기화
  clearEventHandlers() {
    this.eventHandlers = {};
    console.log("모든 WebSocket 이벤트 핸들러 초기화 완료");
  }

  // ✅ 이벤트 트리거 함수
  private triggerEvent<K extends keyof BattleWebSocketEvents>(
    event: K,
    data: BattleWebSocketEvents[K]
  ) {
    const handler = this.eventHandlers[event];
    if (handler) {
      handler(data);
    } else {
      console.warn(`'${event}' 이벤트 핸들러가 등록되지 않음`);
    }
  }

  // ✅ WebSocket 연결 종료
  async disconnect(): Promise<void> {
    if (!this.client || this.disconnecting) return;

    this.disconnecting = true;
    this.clearEventHandlers();
    console.log("모든 이벤트 핸들러 제거됨");

    this.client.deactivate();
    this.client = null;
    this.roomId = null;
    this.connected = false;
    this.disconnecting = false;
    console.log("WebSocket 연결 종료 완료");
  }
}

// ✅ 싱글톤 인스턴스 생성 및 내보내기
const battleWebSocketService = new BattleWebSocketService();
export default battleWebSocketService;
