import { Router } from 'express';

import { UserController } from './user.controller';
import { UpdateProfileRequest, updateProfileSchema, updateFcmTokenSchema } from './user.types';
import { validateBody } from './validation.middleware';

const router = Router();
const userController = new UserController();

router.get('/profile', userController.getProfile);

router.put(
  '/profile',
  validateBody<UpdateProfileRequest>(updateProfileSchema),
  userController.updateProfile
);

router.delete('/profile', userController.deleteProfile);

router.post(
  '/fcm-token',
  validateBody(updateFcmTokenSchema),
  userController.updateFcmToken.bind(userController)
);

export default router;
