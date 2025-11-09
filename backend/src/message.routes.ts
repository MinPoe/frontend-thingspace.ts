import express from 'express';
import { authenticateToken } from './auth.middleware';;
import { messageModel } from './message.model';
import { workspaceModel } from './workspace.model';
import { createMessageSchema, getMessagesQuerySchema } from './message.types';
import mongoose from 'mongoose';

const router = express.Router();

// Get messages for a workspace
router.get('/workspace/:workspaceId', authenticateToken, async (req, res) => {
  try {
    const { workspaceId } = req.params;
    const userId = req.user!._id;

    // Validate query params
    const queryResult = getMessagesQuerySchema.safeParse(req.query);
    if (!queryResult.success) {
        return res.status(400).json({ error: queryResult.error.issues });
    }
    const { limit, before } = queryResult.data;

    // Check workspace exists and user is a member
    const workspace = await workspaceModel.findById(workspaceId);
    if (!workspace) {
      return res.status(404).json({ error: 'Workspace not found' });
    }

    if (!workspace.members.includes(userId)) {
      return res.status(403).json({ error: 'Not a member of this workspace' });
    }

    // Build query
    const query: any = { workspaceId: new mongoose.Types.ObjectId(workspaceId) };
    if (before) {
      query.createdAt = { $lt: new Date(before) };
    }

    // Fetch messages
    const messages = await messageModel
      .find(query)
      .sort({ createdAt: -1 })
      .limit(limit)
      .lean();

    res.json(messages);
  } catch (error) {
    console.error('Error fetching messages:', error);
    res.status(500).json({ error: 'Failed to fetch messages' });
  }
});

// Create a message
router.post('/workspace/:workspaceId', authenticateToken, async (req, res) => {
  try {
    const { workspaceId } = req.params;
    const userId = req.user!._id;

    // Validate request body
    const bodyResult = createMessageSchema.safeParse(req.body);
    if (!bodyResult.success) {
        return res.status(400).json({ error: bodyResult.error.issues });
    }
    const { content } = bodyResult.data;

    // Check workspace exists and user is a member
    const workspace = await workspaceModel.findById(workspaceId);
    if (!workspace) {
      return res.status(404).json({ error: 'Workspace not found' });
    }

    if (!workspace.members.includes(userId)) {
      return res.status(403).json({ error: 'Not a member of this workspace' });
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
});

// Delete a message (workspace owner only)
router.delete('/:messageId', authenticateToken, async (req, res) => {
  try {
    const { messageId } = req.params;
    const userId = req.user!._id;

    const message = await messageModel.findById(messageId);
    if (!message) {
      return res.status(404).json({ error: 'Message not found' });
    }

    // Check if user is workspace owner
    const workspace = await workspaceModel.findById(message.workspaceId);
    if (!workspace) {
      return res.status(404).json({ error: 'Workspace not found' });
    }

    if (!workspace.ownerId.equals(userId)) {
      return res.status(403).json({ error: 'Only workspace owner can delete messages' });
    }

    await messageModel.findByIdAndDelete(messageId);
    res.json({ message: 'Message deleted successfully' });
  } catch (error) {
    console.error('Error deleting message:', error);
    res.status(500).json({ error: 'Failed to delete message' });
  }
});

export const messageRouter = router;