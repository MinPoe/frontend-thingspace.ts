import { Router } from 'express';

import { asyncHandler } from './asyncHandler.util';
import { authenticateToken } from './auth.middleware';
import { MessageController } from './message.controller';

const router = Router();
const messageController = new MessageController();

// Get messages for a workspace
router.get(
  '/workspace/:workspaceId',
  asyncHandler(authenticateToken),
  asyncHandler(messageController.getMessages.bind(messageController))
);

// Create a message
router.post(
  '/workspace/:workspaceId',
  asyncHandler(authenticateToken),
  asyncHandler(messageController.createMessage.bind(messageController))
);

// Delete a message (workspace owner only)
router.delete(
  '/:messageId',
  asyncHandler(authenticateToken),
  asyncHandler(messageController.deleteMessage.bind(messageController))
);

export const messageRouter = router;