import { Router } from 'express';

import { UserController } from './user.controller';
import { UpdateProfileRequest, updateProfileSchema, updateFcmTokenSchema } from './user.types';
import { validateBody } from './validation.middleware';
import { asyncHandler } from './asyncHandler.util';

const router = Router();
const userController = new UserController();

router.get('/profile', userController.getProfile);

router.put(
  '/profile',
  validateBody<UpdateProfileRequest>(updateProfileSchema),
  asyncHandler(userController.updateProfile.bind(userController))
);

router.delete('/profile', asyncHandler(userController.deleteProfile.bind(userController)));

router.post(
  '/fcm-token',
  validateBody(updateFcmTokenSchema),
  asyncHandler(userController.updateFcmToken.bind(userController))
);

router.get('/:id', asyncHandler(userController.getUserById.bind(userController)));
router.get('/email/:email', asyncHandler(userController.getUserByEmail.bind(userController)));

export default router;
