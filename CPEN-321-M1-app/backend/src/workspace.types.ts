import { z } from 'zod';
import mongoose from 'mongoose';

// Profile interface (embedded in workspace)
export interface Profile {
  imagePath: string;
  name: string;
  description: string;
}

// Workspace interface
export interface Workspace {
  _id: string;
  name: string;
  profile: Profile;
  ownerId: string;
  members: string[];
  createdAt: Date;
  updatedAt: Date;
}

// Request types
export interface CreateWorkspaceRequest {
  name: string;
  profilePicture?: string;
  description?: string;
}

export interface UpdateWorkspaceProfileRequest {
  name?: string;
  description?: string;
}

export interface UpdateWorkspacePictureRequest {
  profilePicture: string;
}

export interface AddMemberRequest {
  userId: string;
}

// Zod Schemas
export const createWorkspaceSchema = z.object({
  name: z.string().min(1, 'Workspace name is required'),
  profilePicture: z.string().optional(),
  description: z.string().optional(),
});

export const updateWorkspaceProfileSchema = z.object({
  name: z.string().min(1).optional(),
  description: z.string().optional(),
});

export const updateWorkspacePictureSchema = z.object({
  profilePicture: z.string().min(1, 'Profile picture URL is required'),
});

export const addMemberSchema = z.object({
  userId: z.string().min(1, 'User ID is required'),
});

// Membership status enum
export enum WsMembershipStatus {
  OWNER = 'OWNER',
  MEMBER = 'MEMBER',
  NOT_MEMBER = 'NOT_MEMBER',
  BANNED = 'BANNED'
}

