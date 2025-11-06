import { NextFunction, Request, Response } from 'express';
import mongoose from 'mongoose';

import { authService } from './auth.service';
import {
  AuthenticateUserRequest,
  AuthenticateUserResponse,
} from './auth.types';
import logger from './logger.util';
import { workspaceService } from './workspace.service';
import { userModel } from './user.model';

export class AuthController {
  async signUp(
    req: Request<unknown, unknown, AuthenticateUserRequest>,
    res: Response<AuthenticateUserResponse>,
    next: NextFunction
  ) {
    try {
      const { idToken } = req.body;

      const data = await authService.signUpWithGoogle(idToken);
      const workspace_data = {
        name: `${data.user.profile.name}'s Personal Workspace`, 
        profilePicture: data.user.profile?.imagePath || '', 
        description: 'Your personal workspace for all your personal notes'
      }
      const personalWorkspace = await workspaceService.createWorkspace(
        data.user._id, 
        workspace_data
      );

      await userModel.updatePersonalWorkspace(
        data.user._id,
        new mongoose.Types.ObjectId(personalWorkspace._id)
      );

      return res.status(201).json({
        message: 'User signed up successfully',
        data,
      });
    } catch (error) {
      logger.error('Google sign up error:', error);

      if (error instanceof Error) {
        if (error.message === 'Invalid Google token') {
          return res.status(401).json({
            message: 'Invalid Google token',
          });
        }

        if (error.message === 'User already exists') {
          return res.status(409).json({
            message: 'User already exists, please sign in instead.',
          });
        }

        if (error.message === 'Failed to process user') {
          return res.status(500).json({
            message: error.message,
          });
        }
      }

      next(error);
    }
  }

  async signIn(
    req: Request<unknown, unknown, AuthenticateUserRequest>,
    res: Response<AuthenticateUserResponse>,
    next: NextFunction
  ) {
    try {
      const { idToken } = req.body;

      const data = await authService.signInWithGoogle(idToken);

      return res.status(200).json({
        message: 'User signed in successfully',
        data,
      });
    } catch (error) {
      logger.error('Google sign in error:', error);

      if (error instanceof Error) {
        if (error.message === 'Invalid Google token') {
          return res.status(401).json({
            message: 'Invalid Google token',
          });
        }

        if (error.message === 'User not found') {
          return res.status(404).json({
            message: 'User not found, please sign up first.',
          });
        }

        if (error.message === 'Failed to process user') {
          return res.status(500).json({
            message: error.message,
          });
        }
      }

      next(error);
    }
  }

  // DEV ONLY - Creates a test user and returns token
  async devLogin(req: Request, res: Response, next: NextFunction) {
    try {
      const email = req.body.email || 'test@example.com';
      
      const data = await authService.devLogin(email);

      return res.status(200).json({
        message: 'Dev login successful',
        data,
      });
    } catch (error) {
      logger.error('Dev login error:', error);
      return res.status(500).json({
        message: error instanceof Error ? error.message : 'Dev login failed',
      });
    }
  }
}
