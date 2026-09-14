import { API_URL } from '@/constants/config';
import type {
  AuthResponse,
  Capsule,
  CapsuleEvent,
  Condition,
  ConditionType,
  User,
} from '@/types/api';

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly code?: string,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

type RequestOptions = {
  method?: string;
  token?: string | null;
  body?: unknown;
};

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const headers: Record<string, string> = {
    Accept: 'application/json',
  };
  if (options.body !== undefined) {
    headers['Content-Type'] = 'application/json';
  }
  if (options.token) {
    headers.Authorization = `Bearer ${options.token}`;
  }

  const response = await fetch(`${API_URL}${path}`, {
    method: options.method ?? 'GET',
    headers,
    body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
  });

  if (response.status === 204) {
    return undefined as T;
  }

  const text = await response.text();
  const data = text ? (JSON.parse(text) as unknown) : null;

  if (!response.ok) {
    const err = data as { message?: string; error?: string } | null;
    throw new ApiError(
      err?.message ?? `Request failed (${response.status})`,
      response.status,
      err?.error,
    );
  }

  return data as T;
}

export const api = {
  register(body: { email: string; password: string; displayName: string }) {
    return request<AuthResponse>('/api/auth/register', { method: 'POST', body });
  },

  login(body: { email: string; password: string }) {
    return request<AuthResponse>('/api/auth/login', { method: 'POST', body });
  },

  me(token: string) {
    return request<User>('/api/auth/me', { token });
  },

  lookupUser(token: string, email: string) {
    return request<User>(`/api/users/lookup?email=${encodeURIComponent(email)}`, { token });
  },

  listCapsules(token: string) {
    return request<Capsule[]>('/api/capsules', { token });
  },

  getCapsule(token: string, id: string) {
    return request<Capsule>(`/api/capsules/${id}`, { token });
  },

  createCapsule(
    token: string,
    body: { title: string; content: string; recipientId?: string },
  ) {
    return request<Capsule>('/api/capsules', { method: 'POST', token, body });
  },

  lockCapsule(token: string, id: string) {
    return request<Capsule>(`/api/capsules/${id}/lock`, { method: 'POST', token });
  },

  openCapsule(token: string, id: string) {
    return request<Capsule>(`/api/capsules/${id}/open`, { method: 'POST', token });
  },

  listConditions(token: string, capsuleId: string) {
    return request<Condition[]>(`/api/capsules/${capsuleId}/conditions`, { token });
  },

  addCondition(
    token: string,
    capsuleId: string,
    body: { type: ConditionType; config: Record<string, unknown> },
  ) {
    return request<Condition>(`/api/capsules/${capsuleId}/conditions`, {
      method: 'POST',
      token,
      body,
    });
  },

  locationCheck(
    token: string,
    capsuleId: string,
    body: { latitude: number; longitude: number; accuracyMeters?: number },
  ) {
    return request<void>(`/api/capsules/${capsuleId}/location-check`, {
      method: 'POST',
      token,
      body,
    });
  },

  listEvents(token: string, capsuleId: string) {
    return request<CapsuleEvent[]>(`/api/capsules/${capsuleId}/events`, { token });
  },
};
