import { Router } from 'express';

import { asyncHandler } from './asyncHandler.util';
import { authenticateToken } from './auth.middleware';
import { MessageController } from './message.controller';

const router = Router();
const messageController = new MessageController();

// Get messages for a workspace
router.get(
  '/workspace/:workspaceId',
  authenticateToken,
  asyncHandler(messageController.getMessages.bind(messageController))
);

// Create a message
router.post(
  '/workspace/:workspaceId',
  authenticateToken,
  asyncHandler(messageController.createMessage.bind(messageController))
);

// Delete a message (workspace owner only)
router.delete(
  '/:messageId',
  authenticateToken,
  asyncHandler(messageController.deleteMessage.bind(messageController))
);

export const messageRouter = router;