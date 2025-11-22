import type { IUser } from './users/user.types';

// Express namespace augmentation for extending Request type
declare global {
  namespace Express {
    interface Request {
      user?: IUser;
    }
  }
}

