export type CapsuleStatus =
  | 'DRAFT'
  | 'LOCKED'
  | 'UNLOCKING'
  | 'UNLOCKED'
  | 'EXPIRED'
  | 'CANCELLED';

export type ConditionType = 'DATE' | 'LOCATION' | 'IDENTITY' | 'CONNECTIVITY';

export type ConditionResultStatus = 'PENDING' | 'TRUE' | 'FALSE';

export interface User {
  id: string;
  email: string;
  displayName: string;
  emailVerified: boolean;
}

export interface AuthResponse {
  accessToken: string;
  user: User;
}

export interface Capsule {
  id: string;
  creatorId: string;
  recipientId: string;
  title: string;
  content: string | null;
  status: CapsuleStatus;
  logicOperator: 'AND' | 'OR';
  lockedAt: string | null;
  unlockedAt: string | null;
  createdAt: string;
}

export interface Condition {
  id: string;
  capsuleId: string;
  type: ConditionType;
  status: ConditionResultStatus;
  config: string;
  createdAt: string;
  evaluatedAt: string | null;
  satisfiedAt: string | null;
}

export interface CapsuleEvent {
  id: string;
  capsuleId: string;
  eventType: string;
  actorId: string | null;
  metadata: string | null;
  createdAt: string;
}

export interface ApiErrorBody {
  error?: string;
  message?: string;
}
