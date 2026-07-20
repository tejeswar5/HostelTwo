import axios, { type AxiosInstance } from 'axios';
import type { AuthResponse } from './types';

export interface StoredSession {
  accessToken: string;
  refreshToken: string;
  userId: number;
  fname: string;
  lname: string;
  email: string;
}

function sessionFromAuthResponse(auth: AuthResponse): StoredSession {
  return {
    accessToken: auth.accessToken,
    refreshToken: auth.refreshToken,
    userId: auth.userId,
    fname: auth.fname,
    lname: auth.lname,
    email: auth.email,
  };
}

/**
 * One of these is created per backend (lessor/renter). Each keeps its own
 * localStorage-backed session and refreshes its own access token on a 401 - the two
 * apps are independent services with independent tokens, even though a browser could
 * in principle be logged into both at once in different tabs.
 */
export function createApiClient(baseURL: string, storageKey: string) {
  const client: AxiosInstance = axios.create({ baseURL });

  function getSession(): StoredSession | null {
    const raw = localStorage.getItem(storageKey);
    return raw ? (JSON.parse(raw) as StoredSession) : null;
  }

  function setSession(auth: AuthResponse) {
    localStorage.setItem(storageKey, JSON.stringify(sessionFromAuthResponse(auth)));
  }

  function clearSession() {
    localStorage.removeItem(storageKey);
  }

  client.interceptors.request.use((config) => {
    const session = getSession();
    if (session) {
      config.headers.Authorization = `Bearer ${session.accessToken}`;
    }
    return config;
  });

  let refreshInFlight: Promise<string | null> | null = null;

  async function refreshAccessToken(): Promise<string | null> {
    const session = getSession();
    if (!session) return null;
    try {
      const { data } = await axios.post<AuthResponse>(`${baseURL}/auth/refresh`, {
        refreshToken: session.refreshToken,
      });
      setSession(data);
      return data.accessToken;
    } catch {
      clearSession();
      return null;
    }
  }

  client.interceptors.response.use(
    (response) => response,
    async (error) => {
      const original = error.config;
      if (error.response?.status === 401 && !original._retry && getSession()) {
        original._retry = true;
        refreshInFlight ??= refreshAccessToken().finally(() => {
          refreshInFlight = null;
        });
        const newToken = await refreshInFlight;
        if (newToken) {
          original.headers.Authorization = `Bearer ${newToken}`;
          return client(original);
        }
      }
      return Promise.reject(error);
    },
  );

  return { client, getSession, setSession, clearSession };
}
