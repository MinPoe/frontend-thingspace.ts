import { Router } from 'express';

import { AuthController } from './auth.controller';
import { AuthenticateUserRequest, authenticateUserSchema } from './auth.types';
import { validateBody } from './validation.middleware';
import { asyncHandler } from './asyncHandler.util';

const router = Router();
const authController = new AuthController();

router.post(
  '/signup',
  validateBody<AuthenticateUserRequest>(authenticateUserSchema),
  asyncHandler(authController.signUp.bind(authController))
);

router.post(
  '/signin',
  validateBody(authenticateUserSchema),
  asyncHandler(authController.signIn.bind(authController))
);

// DEV ONLY - Remove in production!
router.post(
  '/dev-login',
  asyncHandler(authController.devLogin.bind(authController))
);

export default router;
