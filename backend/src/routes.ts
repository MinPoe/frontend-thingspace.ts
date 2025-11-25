import { Router } from 'express';

import { authenticateToken } from './authentication/auth.middleware';
import { asyncHandler } from './utils/asyncHandler.util';
import authRoutes from './authentication/auth.routes';
import mediaRoutes from './media/media.routes';
import usersRoutes from './users/user.routes';
import noteRoutes from './notes/notes.routes';
import workspaceRoutes from './workspaces/workspace.routes';
import { messageRouter } from './messages/message.routes';

const router = Router();

router.use('/auth', authRoutes);

router.use('/user', asyncHandler(authenticateToken), usersRoutes);

router.use('/media', asyncHandler(authenticateToken), mediaRoutes);

router.use('/notes', asyncHandler(authenticateToken), noteRoutes);

router.use('/workspace', asyncHandler(authenticateToken), workspaceRoutes);

router.use('/messages', asyncHandler(authenticateToken), messageRouter);

export default router;
