export interface TagResponse {
  id: number;
  name: string;
  description: string;
  quizCount: number;
  synonyms: string[];
  parentId: number | null;
}

export interface TagCreateRequest {
  name: string;
  description?: string;
  parentId?: number;
  synonyms?: string[];
}
