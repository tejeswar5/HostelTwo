import { createContext, useContext, useMemo, useState, type ReactNode } from 'react';
import type { AuthResponse } from '../api/types';
import type { StoredSession } from '../api/client';

interface AuthContextValue {
  session: StoredSession | null;
  login: (auth: AuthResponse) => void;
  logout: () => void;
}

interface Session {
  getSession: () => StoredSession | null;
  setSession: (auth: AuthResponse) => void;
  clearSession: () => void;
}

/**
 * One context per backend (lessor/renter) so a browser can - in theory - hold a
 * renter session and a lessor session at the same time without them clobbering
 * each other, matching the two independent services underneath.
 */
export function createAuthContext(api: Session) {
  const Context = createContext<AuthContextValue | null>(null);

  function AuthProvider({ children }: { children: ReactNode }) {
    const [session, setSessionState] = useState<StoredSession | null>(() => api.getSession());

    const value = useMemo<AuthContextValue>(
      () => ({
        session,
        login: (auth) => {
          api.setSession(auth);
          setSessionState(api.getSession());
        },
        logout: () => {
          api.clearSession();
          setSessionState(null);
        },
      }),
      [session],
    );

    return <Context.Provider value={value}>{children}</Context.Provider>;
  }

  function useAuth() {
    const ctx = useContext(Context);
    if (!ctx) throw new Error('useAuth must be used within its matching AuthProvider');
    return ctx;
  }

  return { AuthProvider, useAuth };
}
