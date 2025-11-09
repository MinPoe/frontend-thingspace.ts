import { Request, Response } from 'express';
import mongoose from 'mongoose';

import { messageModel } from './message.model';
import { workspaceModel } from './workspace.model';
import { createMessageSchema, getMessagesQuerySchema } from './message.types';

export class MessageController {
  async getMessages(req: Request, res: Response): Promise<void> {
    try {
      const { workspaceId } = req.params;
      const userId = req.user?._id;
      if (!userId) {
        res.status(401).json({ error: 'User not authenticated' });
        return;
      }

      // Validate query params
      const queryResult = getMessagesQuerySchema.safeParse(req.query);
      if (!queryResult.success) {
          res.status(400).json({ error: queryResult.error.issues });
          return;
      }
      const { limit, before } = queryResult.data;

      // Check workspace exists and user is a member
      const workspace = await workspaceModel.findById(workspaceId);
      if (!workspace) {
        res.status(404).json({ error: 'Workspace not found' });
        return;
      }

      if (!workspace.members.includes(userId)) {
        res.status(403).json({ error: 'Not a member of this workspace' });
        return;
      }

      // Build query
      const query: { workspaceId: mongoose.Types.ObjectId; createdAt?: { $lt: Date } } = { 
        workspaceId: new mongoose.Types.ObjectId(workspaceId) 
      };
      if (before) {
        query.createdAt = { $lt: new Date(before as string) };
      }

      // Fetch messages
      const messages = await messageModel
        .find(query)
        .sort({ createdAt: -1 })
        .limit(limit as number)
        .lean();

      res.json(messages);
    } catch (error) {
      console.error('Error fetching messages:', error);
      res.status(500).json({ error: 'Failed to fetch messages' });
    }
  }

  async createMessage(req: Request, res: Response): Promise<void> {
    try {
      const { workspaceId } = req.params;
      const userId = req.user?._id;
      if (!userId) {
        res.status(401).json({ error: 'User not authenticated' });
        return;
      }

      // Validate request body
      const bodyResult = createMessageSchema.safeParse(req.body);
      if (!bodyResult.success) {
          res.status(400).json({ error: bodyResult.error.issues });
          return;
      }
      const { content } = bodyResult.data;

      // Check workspace exists and user is a member
      const workspace = await workspaceModel.findById(workspaceId);
      if (!workspace) {
        res.status(404).json({ error: 'Workspace not found' });
        return;
      }

      if (!workspace.members.includes(userId)) {
        res.status(403).json({ error: 'Not a member of this workspace' });
        return;
      }

      // Create message
      const message = await messageModel.create({
        workspaceId: new mongoose.Types.ObjectId(workspaceId),
        authorId: userId,
        content
      });

      // Update workspace's latest message timestamp
      await workspaceModel.findByIdAndUpdate(workspaceId, {
        latestChatMessageTimestamp: message.createdAt
      });

      res.status(201).json(message);
    } catch (error) {
      console.error('Error creating message:', error);
      res.status(500).json({ error: 'Failed to create message' });
    }
  }

  async deleteMessage(req: Request, res: Response): Promise<void> {
    try {
      const { messageId } = req.params;
      const userId = req.user?._id;
      if (!userId) {
        res.status(401).json({ error: 'User not authenticated' });
        return;
      }

      const message = await messageModel.findById(messageId);
      if (!message) {
        res.status(404).json({ error: 'Message not found' });
        return;
      }

      // Check if user is workspace owner
      const workspace = await workspaceModel.findById(message.workspaceId);
      if (!workspace) {
        res.status(404).json({ error: 'Workspace not found' });
        return;
      }

      if (!workspace.ownerId.equals(userId)) {
        res.status(403).json({ error: 'Only workspace owner can delete messages' });
        return;
      }

      await messageModel.findByIdAndDelete(messageId);
      res.json({ message: 'Message deleted successfully' });
    } catch (error) {
      console.error('Error deleting message:', error);
      res.status(500).json({ error: 'Failed to delete message' });
    }
  }
}

