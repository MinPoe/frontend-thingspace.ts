import { Router, RequestHandler } from 'express';

import { upload } from '../utils/storage';
import { authenticateToken } from '../authentication/auth.middleware';
import { MediaController } from './media.controller';
import { asyncHandler } from '../utils/asyncHandler.util';

const router = Router();
const mediaController = new MediaController();

router.post(
  '/upload',
  asyncHandler(authenticateToken),
  upload.single('media') as RequestHandler,
  asyncHandler(mediaController.uploadImage.bind(mediaController))
);

export default router;
