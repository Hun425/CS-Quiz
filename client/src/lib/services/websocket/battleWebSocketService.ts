// src/services/BattleWebSocketService.ts

import SockJS from "sockjs-client";
import { Client, IFrame } from "@stomp/stompjs";
import { useProfileStore } from "@/store/profileStore";
import { BattleWebSocketEvents } from "@/lib/types/battle";
import {
  BattleParticipantsPayload,
  BattleSocketEventKey,
} from "@/lib/types/battle";

// WebSocket 기본 주소
const WS_BASE_URL =
  process.env.NEXT_PUBLIC_WS_BASE_URL || "ws://localhost:8080/ws-battle/";

class BattleWebSocketService {
  private client: Client | null = null;
  private roomId: number | null = null;

  private eventHandlers: {
    [K in BattleSocketEventKey]?: (data: BattleWebSocketEvents[K]) => void;
  } = {};

  private connected = false;
  private connecting = false;
  private disconnecting = false;

  private connectionTimeoutId: NodeJS.Timeout | null = null;
  private reconnectAttempts = 0;
  private readonly maxReconnectAttempts = 3;

  /** ✅ WebSocket 연결 */
  async connect(roomId: number, userId: number): Promise<void> {
    if (!userId) {
      console.error("❌ WebSocket 연결 불가: userId가 없습니다.");
      return;
    }

    if (this.connecting || (this.connected && this.roomId === roomId)) return;

    if (this.connected || this.disconnecting) {
      await this.disconnect();
      await new Promise((r) => setTimeout(r, 1000));
    }

    this.connecting = true;
    this.roomId = roomId;
    this.reconnectAttempts = 0;

    return new Promise((resolve, reject) => {
      try {
        this.connectionTimeoutId = setTimeout(() => {
          this.connecting = false;
          reject(new Error("⏰ WebSocket 연결 시간 초과"));
        }, 10000);

        const socket = new SockJS(WS_BASE_URL);

        this.client = new Client({
          webSocketFactory: () => socket,
          debug: (str) => console.log("WebSocket Debug:", str),
          reconnectDelay: 5000,
          heartbeatIncoming: 4000,
          heartbeatOutgoing: 4000,
        });

        this.client.onConnect = (frame: IFrame) => {
          clearTimeout(this.connectionTimeoutId!);
          this.connectionTimeoutId = null;

          this.connected = true;
          this.connecting = false;

          this.subscribeToBattleEvents(roomId);
          this.joinBattle(userId);

          console.log("✅ WebSocket 연결 완료:", frame);
          resolve();
        };

        this.client.onStompError = (frame) => {
          clearTimeout(this.connectionTimeoutId!);
          this.connectionTimeoutId = null;

          this.connected = false;
          this.connecting = false;

          console.error("❌ WebSocket 오류:", frame.headers["message"]);
          reject(new Error("WebSocket 연결 오류"));
        };

        this.client.onWebSocketClose = () => {
          this.connected = false;
          this.handleReconnect(roomId, userId);
        };

        this.client.activate();
      } catch (err) {
        clearTimeout(this.connectionTimeoutId!);
        this.connectionTimeoutId = null;

        this.connected = false;
        this.connecting = false;
        console.error("❌ WebSocket 연결 실패:", err);
        reject(err);
      }
    });
  }

  /** ✅ 이벤트 트리거 */
  triggerEvent<K extends BattleSocketEventKey>(
    event: K,
    data: BattleWebSocketEvents[K]
  ) {
    console.log("eventHandlers keys:", Object.keys(this.eventHandlers));
    const handler = this.eventHandlers[event];
    if (handler) {
      handler(data);
    } else {
      console.warn(`⚠️ '${event}' 이벤트 핸들러 없음`);
    }
  }
  /**
  * → /topic/battle/{roomId}/participants   🔸 "PARTICIPANTS"
  *   → /topic/battle/{roomId}/start        🔸 "START"
  *   → /topic/battle/{roomId}/status       🔸 "STATUS"
  *   → /topic/battle/{roomId}/end          🔸 "END"
  *  → /topic/battle/{roomId}/question      🔸 "NEXT" 또는 "QUESTION" 등으로 해석 가능
  *  → /topic/battle/{roomId}/progress      🔸 "STATUS"로 같이 묶일 수도 있음
  *  → /user/{sessionId}/queue/battle/result🔸 "ANSWER"

   */
  /** ✅ 배틀 이벤트 구독 */
  private subscribeToBattleEvents(roomId: number) {
    if (!this.client) return;

    this.client.subscribe(`/topic/battle/${roomId}/participants`, (msg) => {
      const data: BattleParticipantsPayload = JSON.parse(msg.body);
      this.triggerEvent(BattleSocketEventKey.PARTICIPANTS, data);
    });

    this.client.subscribe(`/topic/battle/${roomId}/start`, (msg) => {
      const data = JSON.parse(msg.body);
      this.triggerEvent(BattleSocketEventKey.START, data);
    });

    this.client.subscribe(`/topic/battle/${roomId}/progress`, (msg) => {
      const data = JSON.parse(msg.body);
      this.triggerEvent(BattleSocketEventKey.PROGRESS, data);
    });

    this.client.subscribe(`/topic/battle/${roomId}/status`, (msg) => {
      const data = JSON.parse(msg.body);
      this.triggerEvent(BattleSocketEventKey.STATUS, data);
    });

    this.client.subscribe(`/topic/battle/${roomId}/question`, (msg) => {
      const data = JSON.parse(msg.body);
      this.triggerEvent(BattleSocketEventKey.NEXT_QUESTION, data);
    });

    this.client.subscribe(`/user/queue/battle/result`, (msg) => {
      const data = JSON.parse(msg.body);
      this.triggerEvent(BattleSocketEventKey.RESULT, data);
    });

    this.client.subscribe(`/topic/battle/${roomId}/end`, (msg) => {
      const data = JSON.parse(msg.body);
      this.triggerEvent(BattleSocketEventKey.END, data);
    });
  }

  /** ✅ 자동 재연결 */
  private handleReconnect(roomId: number, userId: number) {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.warn("❌ WebSocket 최대 재연결 시도 초과");
      return;
    }

    this.reconnectAttempts++;
    console.log(`🔄 WebSocket 재연결 시도 ${this.reconnectAttempts}`);

    setTimeout(
      () => this.connect(roomId, userId),
      5000 * this.reconnectAttempts
    );
  }

  /** ✅ 서버에 배틀 참가 요청 */
  private joinBattle(userId: number) {
    if (!this.client || !this.connected || !this.roomId) return;

    this.client.publish({
      destination: "/app/battle/join",
      body: JSON.stringify({ userId, roomId: this.roomId, isReady: false }),
    });

    console.log("📨 배틀 참가 요청 전송");
  }

  /** ✅ 서버에 준비 상태 전송 */
  toggleReady() {
    if (!this.client || !this.connected || !this.roomId) return;

    const userId = useProfileStore.getState().userProfile?.id;
    if (!userId) {
      console.error("❌ 준비 상태 변경 실패: userId 없음");
      return;
    }

    this.client.publish({
      destination: "/app/battle/ready",
      body: JSON.stringify({ userId, roomId: this.roomId }),
    });

    console.log("📨 준비 상태 요청 전송");
  }

  /** ✅ 서버에 정답 제출 */
  submitAnswer(questionId: number, answer: string, timeSpentSeconds: number) {
    if (!this.client || !this.connected || !this.roomId) return;

    const userId = useProfileStore.getState().userProfile?.id;
    if (!userId) {
      console.error("❌ 정답 제출 실패: userId 없음");
      return;
    }

    this.client.publish({
      destination: "/app/battle/answer",
      body: JSON.stringify({
        roomId: this.roomId,
        questionId,
        answer,
        timeSpentSeconds,
      }),
    });

    console.log("📨 정답 제출 전송");
  }

  /** ✅ 서버에 방 나가기 요청 */
  leaveBattle() {
    if (!this.client || !this.connected || !this.roomId) return;

    const userId = useProfileStore.getState().userProfile?.id;
    if (!userId) {
      console.error("❌ 방 나가기 실패: userId 없음");
      return;
    }

    this.client.publish({
      destination: "/app/battle/leave",
      body: JSON.stringify({
        userId,
        roomId: this.roomId,
      }),
    });

    console.log("📨 방 나가기 요청 전송");
  }

  /** ✅ 이벤트 핸들러 등록 */
  on<K extends BattleSocketEventKey>(
    event: K,
    handler: (data: BattleWebSocketEvents[K]) => void
  ) {
    this.eventHandlers[event] = handler;
  }

  /** ✅ 이벤트 핸들러 제거 */
  off<K extends keyof BattleWebSocketEvents>(event: K) {
    delete this.eventHandlers[event];
  }

  /** ✅ 모든 이벤트 핸들러 초기화 */
  clearEventHandlers() {
    this.eventHandlers = {};
  }
  /** ✅ 연결 종료 */
  async disconnect(): Promise<void> {
    if (!this.client || this.disconnecting) return;

    this.disconnecting = true;
    this.clearEventHandlers();

    this.client.deactivate();
    this.client = null;
    this.roomId = null;
    this.connected = false;
    this.disconnecting = false;

    console.log("🔌 WebSocket 연결 종료");
  }
}

// ✅ 싱글톤 인스턴스로 내보내기
const battleSocketClient = new BattleWebSocketService();
export default battleSocketClient;
