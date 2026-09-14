import * as SecureStore from 'expo-secure-store';
import { create } from 'zustand';

import { api } from '@/services/api';
import type { User } from '@/types/api';

const TOKEN_KEY = 'timecapsule_access_token';

type AuthState = {
  token: string | null;
  user: User | null;
  hydrated: boolean;
  hydrate: () => Promise<void>;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string, displayName: string) => Promise<void>;
  logout: () => Promise<void>;
};

export const useAuthStore = create<AuthState>((set, get) => ({
  token: null,
  user: null,
  hydrated: false,

  async hydrate() {
    try {
      const token = await SecureStore.getItemAsync(TOKEN_KEY);
      if (!token) {
        set({ token: null, user: null, hydrated: true });
        return;
      }
      const user = await api.me(token);
      set({ token, user, hydrated: true });
    } catch {
      await SecureStore.deleteItemAsync(TOKEN_KEY);
      set({ token: null, user: null, hydrated: true });
    }
  },

  async login(email, password) {
    const response = await api.login({ email, password });
    await SecureStore.setItemAsync(TOKEN_KEY, response.accessToken);
    set({ token: response.accessToken, user: response.user });
  },

  async register(email, password, displayName) {
    const response = await api.register({ email, password, displayName });
    await SecureStore.setItemAsync(TOKEN_KEY, response.accessToken);
    set({ token: response.accessToken, user: response.user });
  },

  async logout() {
    await SecureStore.deleteItemAsync(TOKEN_KEY);
    set({ token: null, user: null });
  },
}));

export function useAuthToken(): string {
  const token = useAuthStore((s) => s.token);
  if (!token) {
    throw new Error('Not authenticated');
  }
  return token;
}
