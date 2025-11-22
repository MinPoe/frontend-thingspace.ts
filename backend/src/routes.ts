import { Router } from 'express';

import { authenticateToken } from './authentication/auth.middleware';
import authRoutes from './authentication/auth.routes';
import mediaRoutes from './media/media.routes';
import usersRoutes from './users/user.routes';
import noteRoutes from './notes/notes.routes';
import workspaceRoutes from './workspaces/workspace.routes';
import { messageRouter } from './messages/message.routes';

const router = Router();

router.use('/auth', authRoutes);

router.use('/user', authenticateToken, usersRoutes);

router.use('/media', authenticateToken, mediaRoutes);

router.use('/notes', authenticateToken, noteRoutes);

router.use('/workspace', authenticateToken, workspaceRoutes);

router.use('/messages', authenticateToken, messageRouter);

export default router;
