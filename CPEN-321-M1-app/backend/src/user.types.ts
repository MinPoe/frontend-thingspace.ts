import mongoose, { Document } from 'mongoose';
import z from 'zod';
import { HOBBIES } from './hobbies';

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
  hobbies: string[];
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
  hobbies: z.array(z.string()).default([]),
});

export const updateProfileSchema = z.object({
  profile: z.object({
    imagePath: z.string().optional(),
    name: z.string().min(1).optional(),
    description: z.string().max(500).optional(),
  }).optional(),
  hobbies: z
    .array(z.string())
    .refine(val => val.length === 0 || val.every(v => HOBBIES.includes(v)), {
      message: 'Hobby must be in the available hobbies list',
    })
    .optional(),
});

// Request types
// ------------------------------------------------------------
export type GetProfileResponse = {
  message: string;
  data?: {
    user: IUser;
  };
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
