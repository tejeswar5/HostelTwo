import { createAuthContext } from './createAuthContext';
import { lessorSession } from '../api/lessorApi';

export const { AuthProvider: LessorAuthProvider, useAuth: useLessorAuth } = createAuthContext(lessorSession);
