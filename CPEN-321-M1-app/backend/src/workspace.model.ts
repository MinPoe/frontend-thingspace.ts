import mongoose, { Document, Schema } from 'mongoose';
import { WsMembershipStatus } from './workspace.types';

export interface IWorkspaceDocument extends Document {
  _id: mongoose.Types.ObjectId;
  name: string;
  profile: {
    imagePath: string;
    name: string;
    description: string;
  };
  ownerId: mongoose.Types.ObjectId;
  members: mongoose.Types.ObjectId[];
  bannedMembers: mongoose.Types.ObjectId[];
  createdAt: Date;
  updatedAt: Date;
}

const profileSchema = new Schema({
  imagePath: { type: String, default: '' },
  name: { type: String, required: true },
  description: { type: String, default: '' },
}, { _id: false });

const workspaceSchema = new Schema<IWorkspaceDocument>(
  {
    name: { 
      type: String, 
      required: true,
      trim: true,
      index: true
    },
    profile: {
      type: profileSchema,
      required: true
    },
    ownerId: { 
      type: Schema.Types.ObjectId, 
      ref: 'User',
      required: true,
      index: true
    },
    members: [{ 
      type: Schema.Types.ObjectId, 
      ref: 'User',
      index: true
    }],
    bannedMembers: [{ 
      type: Schema.Types.ObjectId, 
      ref: 'User'
    }],
  },
  { 
    timestamps: true
  }
);

// Indexes for faster queries
workspaceSchema.index({ ownerId: 1, createdAt: -1 });
workspaceSchema.index({ members: 1 });

export const workspaceModel = mongoose.model<IWorkspaceDocument>('Workspace', workspaceSchema);

