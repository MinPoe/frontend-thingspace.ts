import { Router } from 'express';
import { authenticateToken } from './auth.middleware';
import { WorkspaceController } from './workspace.controller';
import { validateBody } from './validation.middleware';
import { CreateWorkspaceRequest, UpdateWorkspaceProfileRequest, UpdateWorkspacePictureRequest, createWorkspaceSchema, updateWorkspaceProfileSchema, updateWorkspacePictureSchema } from './workspace.types';
import { asyncHandler } from './asyncHandler.util';

const router = Router();
const workspaceController = new WorkspaceController();

// Get all workspaces for a user
router.get(
  '/user',
  authenticateToken,
  asyncHandler(workspaceController.getWorkspacesForUser.bind(workspaceController))
);

// Get a user's personal workspace
router.get(
  '/personal',
  authenticateToken,
  asyncHandler(workspaceController.getPersonalWorkspace.bind(workspaceController))
);

// Get members of a workspace
router.get(
  '/:id/members',
  authenticateToken,
  asyncHandler(workspaceController.getWorkspaceMembers.bind(workspaceController))
);

// Get all tags in a workspace, NOTE: Lowkey idk wut this one rly means, right now it jsut checks all teh tagged notes
router.get(
  '/:id/tags',
  authenticateToken,
  asyncHandler(workspaceController.getAllTags.bind(workspaceController))
);

// Get membership status for a user
router.get(
  '/:id/membership/:userId',
  authenticateToken,
  asyncHandler(workspaceController.getMembershipStatus.bind(workspaceController))
);

// Poll for new chat messages
router.get(
  '/:id/poll',
  authenticateToken,
  asyncHandler(workspaceController.pollForNewMessages.bind(workspaceController))
);

// Get a single workspace
router.get(
  '/:id',
  authenticateToken,
  asyncHandler(workspaceController.getWorkspace.bind(workspaceController))
);

// Create a new workspace
router.post(
  '/',
  authenticateToken,
  validateBody<CreateWorkspaceRequest>(createWorkspaceSchema),
  asyncHandler(workspaceController.createWorkspace.bind(workspaceController))
);

// Add member to workspace
router.post(
  '/:id/members',
  authenticateToken,
  asyncHandler(workspaceController.inviteMember.bind(workspaceController))
);

// Update workspace profile
router.put(
  '/:id',
  authenticateToken,
  validateBody<UpdateWorkspaceProfileRequest>(updateWorkspaceProfileSchema),
  asyncHandler(workspaceController.updateWorkspaceProfile.bind(workspaceController))
);

// Update workspace picture
router.put(
  '/:id/picture',
  authenticateToken,
  validateBody<UpdateWorkspacePictureRequest>(updateWorkspacePictureSchema),
  asyncHandler(workspaceController.updateWorkspacePicture.bind(workspaceController))
);

// Ban member from workspace
router.delete(
  '/:id/members/:userId',
  authenticateToken,
  asyncHandler(workspaceController.banMember.bind(workspaceController))
);

// Leave workspace (user removes themselves)
router.post(
  '/:id/leave',
  authenticateToken,
  asyncHandler(workspaceController.leaveWorkspace.bind(workspaceController))
);

// Delete workspace
router.delete(
  '/:id',
  authenticateToken,
  asyncHandler(workspaceController.deleteWorkspace.bind(workspaceController))
);

// Get a specific workspace by ID
router.get(
  '/:id',
  authenticateToken,
  asyncHandler(workspaceController.getWorkspace.bind(workspaceController))
);


export default router;

