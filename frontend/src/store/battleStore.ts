// src/store/battleStore.ts
import { create } from 'zustand';
import { BattleRoomResponse, BattleStatus, ParticipantDto } from '../types/battle.ts';

// 배틀 스토어 상태 타입
interface BattleState {
    // 배틀룸 정보
    currentRoom: BattleRoomResponse | null;
    isReady: boolean;
    status: BattleStatus;

    // 문제 및 진행 상태
    currentQuestionIndex: number;
    totalQuestions: number;
    remainingTimeSeconds: number;
    timeLimit: number;

    // 참가자 정보
    participants: ParticipantDto[];

    // 액션
    setCurrentRoom: (room: BattleRoomResponse | null) => void;
    setIsReady: (ready: boolean) => void;
    setStatus: (status: BattleStatus) => void;
    setCurrentQuestionIndex: (index: number) => void;
    setTotalQuestions: (count: number) => void;
    setRemainingTimeSeconds: (seconds: number) => void;
    setTimeLimit: (seconds: number) => void;
    setParticipants: (participants: ParticipantDto[]) => void;
    updateParticipant: (participantId: number, data: Partial<ParticipantDto>) => void;
    resetState: () => void;
}

// 초기 상태
const initialState = {
    currentRoom: null,
    isReady: false,
    status: BattleStatus.WAITING,
    currentQuestionIndex: 0,
    totalQuestions: 0,
    remainingTimeSeconds: 0,
    timeLimit: 0,
    participants: [],
};


// 배틀 스토어 생성
export const useBattleStore = create<BattleState>((set) => ({
    ...initialState,

    // 안전한 참가자 업데이트
    updateParticipantSafely: (participantId: number, updateFn: (participant: ParticipantDto) => Partial<ParticipantDto>) => {
        set((state) => {
            const participant = state.participants.find(p => p.id === participantId);
            if (!participant) return state;

            const updateData = updateFn(participant);
            return {
                participants: state.participants.map(p =>
                    p.id === participantId ? { ...p, ...updateData } : p
                )
            };
        });
    },

    // 배틀 상태 안전하게 업데이트
    updateBattleStateSafely: (updates: Partial<BattleState>) => {
        set(state => {
            const newState = { ...state, ...updates };
            // 여기서 추가 검증 로직 구현 가능
            return newState;
        });
    },

    // 배틀룸 정보 설정
    setCurrentRoom: (room) => set({ currentRoom: room }),

    // 준비 상태 설정
    setIsReady: (ready) => set({ isReady: ready }),

    // 배틀 상태 설정
    setStatus: (status) => set({ status }),

    // 현재 문제 인덱스 설정
    setCurrentQuestionIndex: (index) => set({ currentQuestionIndex: index }),

    // 전체 문제 수 설정
    setTotalQuestions: (count) => set({ totalQuestions: count }),

    // 남은 시간 설정
    setRemainingTimeSeconds: (seconds) => set({ remainingTimeSeconds: seconds }),

    // 제한 시간 설정
    setTimeLimit: (seconds) => set({ timeLimit: seconds }),

    // 참가자 목록 설정
    setParticipants: (participants) => set({ participants }),

    // 특정 참가자 정보 업데이트
    updateParticipant: (participantId, data) => set((state) => ({
        participants: state.participants.map(p =>
            p.id === participantId ? { ...p, ...data } : p
        )
    })),

    // 상태 초기화
    resetState: () => set(initialState)
}));

