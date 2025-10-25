import { Router } from 'express';

import { authenticateToken } from './auth.middleware';
import authRoutes from './auth.routes';
import mediaRoutes from './media.routes';
import usersRoutes from './user.routes';
import noteRoutes from './notes.routes';
import workspaceRoutes from './workspace.routes';
import { messageRouter } from './message.routes';

const router = Router();

router.use('/auth', authRoutes);

router.use('/user', authenticateToken, usersRoutes);

router.use('/media', authenticateToken, mediaRoutes);

router.use('/notes', authenticateToken, noteRoutes);

router.use('/workspace', authenticateToken, workspaceRoutes);

router.use('/messages', authenticateToken, messageRouter);

export default router;
