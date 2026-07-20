import { Navigate, Outlet } from 'react-router-dom';
import type { StoredSession } from '../api/client';

export function ProtectedRoute({ session, redirectTo }: { session: StoredSession | null; redirectTo: string }) {
  if (!session) {
    return <Navigate to={redirectTo} replace />;
  }
  return <Outlet />;
}
