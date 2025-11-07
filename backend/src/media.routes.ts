import { Router, RequestHandler } from 'express';

import { upload } from './storage';
import { authenticateToken } from './auth.middleware';
import { MediaController } from './media.controller';
import { asyncHandler } from './asyncHandler.util';

const router = Router();
const mediaController = new MediaController();

router.post(
  '/upload',
  asyncHandler(authenticateToken),
  upload.single('media') as RequestHandler,
  asyncHandler(mediaController.uploadImage.bind(mediaController))
);

export default router;
