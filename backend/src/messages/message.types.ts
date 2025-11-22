import { z } from 'zod';

export interface Message {
  _id: string;
  workspaceId: string;
  authorId: string;
  content: string;
  createdAt: Date;
  updatedAt: Date;
}

export interface CreateMessageRequest {
  content: string;
}

export interface GetMessagesQuery {
  limit?: number;
  before?: string; // ISO date string for cursor-based pagination
}

export const createMessageSchema = z.object({
  content: z.string().min(1, 'Message content is required').max(5000, 'Message too long'),
});

export const getMessagesQuerySchema = z.object({
  limit: z.coerce.number().min(1).max(100).optional().default(50),
  before: z.string().datetime().optional(),
});

