import { NextFunction, Request, Response } from 'express';
import jwt from 'jsonwebtoken';
import mongoose from 'mongoose';
import { userModel } from '../users/user.model';
import { IUser } from '../users/user.types';


export const authenticateToken = async (
  req: Request,
  res: Response,
  next: NextFunction
): Promise<void> => {
  try {
    const authHeader = req.headers.authorization;
    const token = authHeader?.split(' ')[1];

    if (!token) {
      res.status(401).json({
        error: 'Access denied',
        message: 'No token provided',
      });
      return;
    }

    const jwtSecret = process.env.JWT_SECRET;
    if (!jwtSecret) {
      res.status(500).json({
        error: 'Server configuration error',
        message: 'JWT_SECRET not configured',
      });
      return;
    }

    const decoded = jwt.verify(token, jwtSecret) as {
      id: mongoose.Types.ObjectId;
    };

    const user = await userModel.findById(decoded.id);

    if (!user) {
      res.status(401).json({
        error: 'User not found',
        message: 'Token is valid but user no longer exists',
      });
      return;
    }

    req.user = user;

    next();
  } catch (error) {
    if (error instanceof jwt.TokenExpiredError) {
      res.status(401).json({
        error: 'Token expired',
        message: 'Please login again',
      });
      return;
    }

    if (error instanceof jwt.JsonWebTokenError) {
      res.status(401).json({
        error: 'Invalid token',
        message: 'Token is malformed or expired',
      });
      return;
    }

    next(error);
  }
};

const moduleHadJwtSecret = process.env.JWT_SECRET !== undefined;

export const authMiddleware = (
  req: Request,
  res: Response,
  next: NextFunction
) => {
  try {
    const token = req.headers.authorization?.split(' ')[1];
    
    if (!token) {
      return res.status(401).json({ error: 'No token provided' });
    }

    if (!moduleHadJwtSecret) {
      next(new Error('JWT_SECRET not configured'));
      return;
    }

    const jwtSecret = process.env.JWT_SECRET;

    if (!jwtSecret) {
      next(new Error('JWT_SECRET not configured'));
      return;
    }

    const decoded = jwt.verify(token, jwtSecret);
    req.user = decoded as unknown as IUser;
    next();
    return;
  } catch (error) {
    // Handle JWT_SECRET configuration error
    // Configuration error should have been handled before try-catch, but keep fallback
    if (error instanceof Error && error.message === 'JWT_SECRET not configured') {
      next(error);
      return;
    }
    
    // Handle JWT-specific errors
    if (error instanceof jwt.TokenExpiredError) {
      return res.status(401).json({ error: 'Invalid token' });
    }
    
    if (error instanceof jwt.JsonWebTokenError) {
      return res.status(401).json({ error: 'Invalid token' });
    }
    
    // Handle any other errors (shouldn't happen for JWT verification, but just in case)
    return res.status(401).json({ error: 'Invalid token' });
  }
};