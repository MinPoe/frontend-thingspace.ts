import mongoose, { Schema } from 'mongoose';
import { z } from 'zod';

import {
  createUserSchema,
  GoogleUserInfo,
  IUser,
  updateProfileSchema,
  UpdateProfileRequest,
} from './user.types';
import logger from '../utils/logger.util';

const userSchema = new Schema<IUser>(
  {
    googleId: {
      type: String,
      required: true,
      unique: true,
      index: true,
    },
    email: {
      type: String,
      required: true,
      unique: true,
      lowercase: true,
      trim: true,
    },
    profile: {
      imagePath: {
        type: String,
        required: false,
        trim: true,
      },
      name: {
        type: String,
        required: true,
        trim: true,
      },
      description: {
        type: String,
        required: false,
        trim: true,
        maxlength: 500,
      },
    },
    fcmToken: {
      type: String,
      required: false,
    },
    personalWorkspaceId: {
      type: Schema.Types.ObjectId,
      ref: 'Workspace',
      required: false,
    },
  },
  {
    timestamps: true,
  }
);

export class UserModel {
  private user: mongoose.Model<IUser>;

  constructor() {
    this.user = mongoose.model<IUser>('User', userSchema);
  }

  async create(userInfo: GoogleUserInfo): Promise<IUser> {
    try {
      // Convert GoogleUserInfo to the new structure
      const userData = {
        email: userInfo.email,
        googleId: userInfo.googleId,
        profile: {
          imagePath: userInfo.profilePicture,
          name: userInfo.name,
          description: '',
        },
      };
      
      const validatedData = createUserSchema.parse(userData);

      return await this.user.create(validatedData);
    } catch (error) {
      if (error instanceof z.ZodError) {
        console.error('Validation error:', error.issues);
        throw new Error('Invalid update data');
      }
      console.error('Error updating user:', error);
      throw new Error('Failed to update user');
    }
  }

  async update(
    userId: mongoose.Types.ObjectId,
    updateProfileReq: UpdateProfileRequest
  ): Promise<IUser | null> {
    try {
      const validatedData = updateProfileSchema.parse(updateProfileReq);

      // Handle nested profile object for MongoDB update
      const updateData = validatedData.profile ? { profile: validatedData.profile } : {};

      const updatedUser = await this.user.findByIdAndUpdate(
        userId,
        updateData,
        {
          new: true,
        }
      );
      return updatedUser;
    } catch (error) {
      logger.error('Error updating user:', error);
      throw new Error('Failed to update user');
    }
  }

  async delete(userId: mongoose.Types.ObjectId): Promise<void> {
    try {
      await this.user.findByIdAndDelete(userId);
    } catch (error) {
      logger.error('Error deleting user:', error);
      throw new Error('Failed to delete user');
    }
  }

  async findById(_id: mongoose.Types.ObjectId): Promise<IUser | null> {
    try {
      const user = await this.user.findOne({ _id });

      if (!user) {
        return null;
      }

      return user;
    } catch (error) {
      console.error('Error finding user by Google ID:', error);
      throw new Error('Failed to find user');
    }
  }

  async findByIds(ids: mongoose.Types.ObjectId[]): Promise<IUser[]> {
    try {
      return await this.user.find({ _id: { $in: ids } });
    } catch (error) {
      console.error('Error finding users by IDs:', error);
      throw new Error('Failed to find users');
    }
  }   

  async findByGoogleId(googleId: string): Promise<IUser | null> {
    try {
      const user = await this.user.findOne({ googleId });

      if (!user) {
        return null;
      }

      return user;
    } catch (error) {
      console.error('Error finding user by Google ID:', error);
      throw new Error('Failed to find user');
    }
  }

  async findByEmail(email: string): Promise<IUser | null> {
    try {
      const user = await this.user.findOne({ email });
      return user;
    } catch (error) {
      logger.error('Error finding user by email:', error);
      throw new Error('Failed to find user');
    }
  }

  async updateFcmToken(
    userId: mongoose.Types.ObjectId,
    fcmToken: string
  ): Promise<IUser | null> {
    try {
      const updatedUser = await this.user.findByIdAndUpdate(
        userId,
        { fcmToken },
        { new: true }
      );
      return updatedUser;
    } catch (error) {
      logger.error('Error updating FCM token:', error);
      throw new Error('Failed to update FCM token');
    }
  }

  async updatePersonalWorkspace(
    userId: mongoose.Types.ObjectId,
    workspaceId: mongoose.Types.ObjectId
  ): Promise<IUser | null> {
    try {
      const updatedUser = await this.user.findByIdAndUpdate(
        userId,
        { personalWorkspaceId: workspaceId },
        { new: true }
      );
      return updatedUser;
    } catch (error) {
      logger.error('Error updating personal workspace:', error);
      throw new Error('Failed to update personal workspace');
    }
  }
}

export const userModel = new UserModel();

