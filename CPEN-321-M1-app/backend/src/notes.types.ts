import { z } from 'zod';

export interface Note {
  _id: string;
  title: string;
  content: string;
  userId: string;
  createdAt: Date;
  updatedAt: Date;
  tags?: string[];
  workspaceId?: string;
}

export interface CreateNoteRequest {
  title: string;
  content: string;
  tags?: string[];
  workspaceId?: string;
}

export interface UpdateNoteRequest {
  title?: string;
  content?: string;
  tags?: string[];
  workspaceId?: string;
}

// Flexible schemas - only validate what we know, allow anything else
export const createNoteSchema = z.object({
  title: z.string().min(1, 'Title is required').max(100, 'Title too long'),
  content: z.string().min(1, 'Content is required').max(5000, 'Content too long'),
}).passthrough(); // This allows any additional fields

export const updateNoteSchema = z.object({
  title: z.string().min(1, 'Title is required').max(100, 'Title too long').optional(),
  content: z.string().min(1, 'Content is required').max(5000, 'Content too long').optional(),
}).passthrough(); // This allows any additional fields

// Alternative: Very minimal validation
export const minimalCreateNoteSchema = z.object({
  title: z.string().min(1, 'Title is required'),
  content: z.string().min(1, 'Content is required'),
}).passthrough();

export const minimalUpdateNoteSchema = z.record(z.any()); // Accepts any object
