import { Router } from 'express';

import { authenticateToken } from './auth.middleware';
import authRoutes from './auth.routes';
import hobbiesRoutes from './hobbies.routes';
import mediaRoutes from './media.routes';
import usersRoutes from './user.routes';
import noteRoutes from './notes.routes';
import workspaceRoutes from './workspace.routes';

const router = Router();

router.use('/auth', authRoutes);

router.use('/hobbies', authenticateToken, hobbiesRoutes);

router.use('/user', authenticateToken, usersRoutes);

router.use('/media', authenticateToken, mediaRoutes);

router.use('/notes', authenticateToken, noteRoutes);

router.use('/workspace', authenticateToken, workspaceRoutes);

export default router;
