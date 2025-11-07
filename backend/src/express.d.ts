import { IUser } from './user.types';

// Express namespace augmentation for extending Request type
declare global {
  namespace Express {
    interface Request {
      user?: IUser;
    }
  }
}

