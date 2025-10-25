import mongoose, { Document, Schema } from 'mongoose';

export interface IMessageDocument extends Document {
  _id: mongoose.Types.ObjectId;
  workspaceId: mongoose.Types.ObjectId;
  authorId: mongoose.Types.ObjectId;
  content: string;
  createdAt: Date;
  updatedAt: Date;
}

const messageSchema = new Schema<IMessageDocument>(
  {
    workspaceId: { 
      type: Schema.Types.ObjectId, 
      ref: 'Workspace',
      required: true,
      index: true
    },
    authorId: { 
      type: Schema.Types.ObjectId, 
      ref: 'User',
      required: true,
      index: true
    },
    content: { 
      type: String, 
      required: true,
      trim: true
    }
  },
  { 
    timestamps: true
  }
);

// Compound index for efficient workspace message queries
messageSchema.index({ workspaceId: 1, createdAt: -1 });

export const messageModel = mongoose.model<IMessageDocument>('Message', messageSchema);