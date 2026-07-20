import { createAuthContext } from './createAuthContext';
import { renterSession } from '../api/renterApi';

export const { AuthProvider: RenterAuthProvider, useAuth: useRenterAuth } = createAuthContext(renterSession);
