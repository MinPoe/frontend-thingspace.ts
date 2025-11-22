import mongoose, { Document, Schema } from 'mongoose';
import { NoteType } from './notes.types';

export interface INoteDocument extends Document {
  _id: mongoose.Types.ObjectId;
  userId: mongoose.Types.ObjectId;
  workspaceId: string;
  fields: unknown[];
  noteType: NoteType;
  tags: string[];
  vectorData: number[];
  createdAt: Date;
  updatedAt: Date;
}

const noteSchema = new Schema<INoteDocument>(
  {
    userId: { 
      type: Schema.Types.ObjectId, 
      ref: 'User',
      required: true,
      index: true
    },
    workspaceId: { 
      type: String, 
      required: true,
      index: true
    },
    fields: [{ type: Schema.Types.Mixed }],
    noteType: { 
      type: String, 
      enum: Object.values(NoteType),
      default: NoteType.CONTENT
    },
    tags: [{ type: String }],
    vectorData: [{ type: Number }],
  },
  { 
    timestamps: true
  }
);

// Index for faster queries
noteSchema.index({ userId: 1, workspaceId: 1 });
noteSchema.index({ userId: 1, createdAt: -1 });

export const noteModel = mongoose.model<INoteDocument>('Note', noteSchema);

