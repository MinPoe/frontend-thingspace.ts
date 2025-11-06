import mongoose, { Document } from 'mongoose';
import z from 'zod';

// User model
// ------------------------------------------------------------
export interface IUser extends Document {
  _id: mongoose.Types.ObjectId;
  googleId: string;
  email: string;
  profile: {
    imagePath?: string;
    name: string;
    description?: string;
  };
  fcmToken?: string;
  personalWorkspaceId?: mongoose.Types.ObjectId;
  createdAt: Date;
  updatedAt: Date;
}

// Zod schemas
// ------------------------------------------------------------
export const createUserSchema = z.object({
  email: z.string().email(),
  googleId: z.string().min(1),
  profile: z.object({
    imagePath: z.string().optional(),
    name: z.string().min(1),
    description: z.string().max(500).optional(),
  }),
});

export const updateProfileSchema = z.object({
  profile: z.object({
    imagePath: z.string().optional(),
    name: z.string().min(1).optional(),
    description: z.string().max(500).optional(),
  }).optional(),
});

export const updateFcmTokenSchema = z.object({
  fcmToken: z.string().min(1),
});

// Request types
// ------------------------------------------------------------
export type GetProfileResponse = {
  message?: string;
  data?: {
    user: IUser;
  };
  error?: string;
};

export type UpdateProfileRequest = z.infer<typeof updateProfileSchema>;

// Generic types
// ------------------------------------------------------------
export type GoogleUserInfo = {
  googleId: string;
  email: string;
  name: string;
  profilePicture?: string;
};
