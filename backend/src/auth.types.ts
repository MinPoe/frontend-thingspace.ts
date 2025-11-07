import { z } from 'zod';

import { IUser } from './user.types';

// Zod schemas
// ------------------------------------------------------------
export const authenticateUserSchema = z.object({
  idToken: z.string().min(1, 'Google token is required'),
});

// Request types
// ------------------------------------------------------------
export type AuthenticateUserRequest = z.infer<typeof authenticateUserSchema>;

export interface AuthenticateUserResponse {
  message: string;
  data?: AuthResult;
}

// Generic types
// ------------------------------------------------------------
export interface AuthResult {
  token: string;
  user: IUser;
}

// Express namespace augmentation needed for type extension
// This is the only way to extend Express Request type in TypeScript
declare global {
  namespace Express {
    interface Request {
      user?: IUser;
    }
  }
}
