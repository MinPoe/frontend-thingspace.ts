import { NextFunction, Request, Response } from 'express';
import mongoose from 'mongoose';

import { GetProfileResponse, UpdateProfileRequest, updateFcmTokenSchema } from './user.types';
import logger from './logger.util';
import { MediaService } from './media.service';
import { userModel } from './user.model';
import { workspaceModel } from './workspace.model';
import { noteModel } from './note.model';

export class UserController {
  getProfile(req: Request, res: Response<GetProfileResponse>) {
    const user = req.user!;

    res.status(200).json({
      message: 'Profile fetched successfully',
      data: { user },
    });
  }

  async updateProfile(
    req: Request<unknown, unknown, UpdateProfileRequest>,
    res: Response<GetProfileResponse>,
    next: NextFunction
  ) {
    try {
      const user = req.user!;

      const updatedUser = await userModel.update(user._id, req.body);

      if (!updatedUser) {
        return res.status(404).json({
          message: 'User not found',
        });
      }

      res.status(200).json({
        message: 'User info updated successfully',
        data: { user: updatedUser },
      });
    } catch (error) {
      logger.error('Failed to update user info:', error);

      if (error instanceof Error) {
        return res.status(500).json({
          message: error.message || 'Failed to update user info',
        });
      }

      next(error);
    }
  }

  async deleteProfile(req: Request, res: Response, next: NextFunction) {
    try {
      const user = req.user!;

      const ownedWorkspaces = await workspaceModel.find({ ownerId: user._id });

      for (const workspace of ownedWorkspaces) {
        await noteModel.deleteMany({ workspaceId: workspace._id.toString() });
        await workspaceModel.findByIdAndDelete(workspace._id);
        logger.info(`Deleted workspace ${workspace._id} for user: ${user._id}`);
      }

      await workspaceModel.updateMany(
        { members: user._id },
        { $pull: { members: user._id } }
      );
      logger.info(`Removed user ${user._id} from all member workspaces`);

      await MediaService.deleteAllUserImages(user._id.toString());

      await userModel.delete(user._id);

      res.status(200).json({
        message: 'User deleted successfully',
        data: {},
      });
    } catch (error) {
      logger.error('Failed to delete user:', error);

      if (error instanceof Error) {
        return res.status(500).json({
          message: error.message || 'Failed to delete user',
        });
      }

      next(error);
    }
  }

  async updateFcmToken(req: Request, res: Response, next: NextFunction) {
    try {
      const user = req.user!;
      const validatedData = updateFcmTokenSchema.parse(req.body);

      const updatedUser = await userModel.updateFcmToken(
        user._id,
        validatedData.fcmToken as string
      );

      if (!updatedUser) {
        return res.status(404).json({ message: 'User not found' });
      }

      logger.info(`FCM token updated for user: ${user._id}`);
      res.status(200).json({ 
        message: 'FCM token updated successfully',
        data: { user: updatedUser }
      });
    } catch (error) {
      logger.error('Error updating FCM token:', error);

      if (error instanceof Error) {
        return res.status(400).json({ 
          message: error.message || 'Failed to update FCM token' 
        });
      }

      next(error);
    }
  }

  async getUserById(req: Request, res: Response<GetProfileResponse>, next: NextFunction) {
    try {
      const { id } = req.params;
      
      if (!mongoose.Types.ObjectId.isValid(id)) {
        return res.status(400).json({
          message: 'Invalid user ID format',
        });
      }

      const user = await userModel.findById(new mongoose.Types.ObjectId(id));

      if (!user) {
        return res.status(404).json({
          message: 'User not found',
        });
      }

      res.status(200).json({
        message: 'User fetched successfully',
        data: { user },
      });
    } catch (error) {
      logger.error('Failed to get user by ID:', error);

      if (error instanceof Error) {
        return res.status(500).json({
          message: error.message || 'Failed to get user',
        });
      }

      next(error);
    }
  }

  async getUserByEmail(req: Request, res: Response<GetProfileResponse>, next: NextFunction) {
    try {
      const { email } = req.params;

      if (!email) {
        return res.status(400).json({
          message: 'Invalid email',
        });
      }

      const user = await userModel.findByEmail(email);

      if (!user) {
        return res.status(404).json({
          message: 'User not found',
        });
      }

      res.status(200).json({
        message: 'User fetched successfully',
        data: { user },
      });
    } catch (error) {
      logger.error('Failed to get user by email:', error);

      if (error instanceof Error) {
        return res.status(500).json({
          message: error.message || 'Failed to get user',
        });
      }

      next(error);
    }
  }
}
