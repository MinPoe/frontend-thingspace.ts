import { Router } from 'express';
import { authenticateToken } from './auth.middleware';
import { WorkspaceController } from './workspace.controller';

const router = Router();
const workspaceController = new WorkspaceController();

// Get a single workspace
router.get(
  '/:id',
  authenticateToken,
  workspaceController.getWorkspace
);

// Get all workspaces for a user
router.get(
  '/user/:userId',
  authenticateToken,
  workspaceController.getWorkspacesForUser
);

// Get members of a workspace
router.get(
  '/:id/members',
  authenticateToken,
  workspaceController.getWorkspaceMembers
);

// Get all tags in a workspace
router.get(
  '/:id/tags',
  authenticateToken,
  workspaceController.getAllTags
);

// Get membership status for a user
router.get(
  '/:id/membership/:userId',
  authenticateToken,
  workspaceController.getMembershipStatus
);

// Create a new workspace
router.post(
  '/',
  authenticateToken,
  workspaceController.createWorkspace
);

// Add member to workspace
router.post(
  '/:id/members',
  authenticateToken,
  workspaceController.addMember
);

// Update workspace profile
router.put(
  '/:id',
  authenticateToken,
  workspaceController.updateWorkspaceProfile
);

// Update workspace picture
router.put(
  '/:id/picture',
  authenticateToken,
  workspaceController.updateWorkspacePicture
);

// Ban member from workspace
router.delete(
  '/:id/members/:userId',
  authenticateToken,
  workspaceController.banMember
);

// Delete workspace
router.delete(
  '/:id',
  authenticateToken,
  workspaceController.deleteWorkspace
);

export default router;

