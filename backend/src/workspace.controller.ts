import { Request, Response } from 'express';
import { workspaceService } from './workspace.service';
import { CreateWorkspaceRequest, UpdateWorkspaceProfileRequest, UpdateWorkspacePictureRequest } from './workspace.types';

export class WorkspaceController {
    async createWorkspace(req: Request, res: Response): Promise<void> {
        try {
            const userId = req.user!._id;

            const workspace = await workspaceService.createWorkspace(userId, req.body as CreateWorkspaceRequest);

            res.status(201).json({
                message: 'Workspace created successfully',
                data: {workspaceId: workspace._id}
            });
        } catch (error) {
            console.error('Error creating workspace:', error);
            
            if (error instanceof Error) {
                if (error.message === 'Workspace name already in use') {
                    res.status(409).json({ error: 'Workspace name already in use' });
                    return;
                }
            }
            
            res.status(500).json({ error: error instanceof Error ? error.message : 'Failed to create workspace' });
        }
    }

    async getPersonalWorkspace(req: Request, res: Response): Promise<void> {
        try {
            const userId = req.user!._id;

            const workspace = await workspaceService.getPersonalWorkspaceForUser(userId);

            res.status(200).json({
                message: 'Personal workspace retrieved successfully',
                data: { workspace },
            });
        } catch (error) {
            console.error('Error retrieving personal workspace:', error);
            
            if (error instanceof Error) {
                if (error.message.includes('User does not have a personal workspace') || 
                    error.message.includes('Personal workspace not found')) {
                    res.status(404).json({ error: error.message });
                    return;
                }
                
                if (error.message.includes('User not found')) {
                    res.status(404).json({ error: error.message });
                    return;
                }
            }
            
            res.status(500).json({ error: error instanceof Error ? error.message : 'Failed to retrieve personal workspace' });
        }
    }

    async getWorkspacesForUser(req: Request, res: Response): Promise<void> {
        try {
            const user = req.user!;

            const workspaces = await workspaceService.getWorkspacesForUser(user._id, user.personalWorkspaceId);

            res.status(200).json({
                message: 'Workspaces retrieved successfully',
                data: { workspaces },
            });
        } catch (error) {
            console.error('Error retrieving workspaces:', error);
            res.status(500).json({ error: error instanceof Error ? error.message : 'Failed to retrieve workspaces' });
        }
    }

    async getWorkspace(req: Request, res: Response): Promise<void> {
        try {
            const userId = req.user!._id;

            const workspaceId = req.params.id;
            const workspace = await workspaceService.getWorkspace(workspaceId, userId);

            res.status(200).json({
                message: 'Workspace retrieved successfully',
                data: { workspace },
            });
        } catch (error) {
            console.error('Error retrieving workspace:', error);
            
            if (error instanceof Error) {
                if (error.message.includes('Access denied')) {
                    res.status(403).json({ error: error.message });
                    return;
                }
                
                if (error.message.includes('Workspace not found')) {
                    res.status(404).json({ error: error.message });
                    return;
                }
            }
            
            res.status(500).json({ error: error instanceof Error ? error.message : 'Failed to retrieve workspace' });
        }
    }

    async getWorkspaceMembers(req: Request, res: Response): Promise<void> {
        try {
            const userId = req.user!._id;

            const workspaceId = req.params.id;
            const members = await workspaceService.getWorkspaceMembers(workspaceId, userId);

            res.status(200).json({
                message: 'Members retrieved successfully',
                data: { members },
            });
        } catch (error) {
            console.error('Error retrieving members:', error);
            
            if (error instanceof Error) {
                if (error.message.includes('Access denied')) {
                    res.status(403).json({ error: error.message });
                    return;
                }
                if (error.message === 'Workspace not found') {
                    res.status(404).json({ error: 'Workspace not found' });
                    return;
                }
            }
            
            res.status(500).json({ error: error instanceof Error ? error.message : 'Failed to retrieve members' });
        }
    }

    async getAllTags(req: Request, res: Response): Promise<void> {
        try {
            const userId = req.user!._id;

            const workspaceId = req.params.id;
            const tags = await workspaceService.getAllTags(workspaceId, userId);

            res.status(200).json({
                message: 'Tags retrieved successfully',
                data: { tags },
            });
        } catch (error) {
            console.error('Error retrieving tags:', error);
            
            if (error instanceof Error) {
                if (error.message.includes('Access denied')) {
                    res.status(403).json({ error: error.message });
                    return;
                }
                if (error.message === 'Workspace not found') {
                    res.status(404).json({ error: 'Workspace not found' });
                    return;
                }
            }
            
            res.status(500).json({ error: error instanceof Error ? error.message : 'Failed to retrieve tags' });
        }
    }

    async getMembershipStatus(req: Request, res: Response): Promise<void> {
        try {
            const workspaceId = req.params.id;
            const checkUserId = req.params.userId;

            const status = await workspaceService.getMembershipStatus(workspaceId, checkUserId);

            res.status(200).json({
                message: 'Membership status retrieved successfully',
                data: { status },
            });
        } catch (error) {
            console.error('Error retrieving membership status:', error);
            
            if (error instanceof Error && error.message === 'Workspace not found') {
                res.status(404).json({ error: 'Workspace not found' });
                return;
            }
            
            res.status(500).json({ error: error instanceof Error ? error.message : 'Failed to retrieve membership status' });
        }
    }

    async inviteMember(req: Request, res: Response): Promise<void> {
        try {
            const requestingUserId = req.user!._id;

            const workspaceId = req.params.id;
            const { userId } = req.body as { userId?: string };

            if (!userId) {
                res.status(400).json({ error: 'userId is required' });
                return;
            }

            const workspace = await workspaceService.inviteMember(workspaceId, requestingUserId, userId);

            res.status(200).json({
                message: 'Member added successfully',
                data: { workspace },
            });
        } catch (error) {
            console.error('Error adding member:', error);
            
            if (error instanceof Error) {
                if (error.message.includes('Only workspace owner')) {
                    res.status(403).json({ error: error.message });
                    return;
                }
                if (error.message.includes('Access denied')) {
                    res.status(403).json({ error: error.message });
                    return;
                }
                if (error.message.includes('Cannot invite members to personal workspace')) {
                    res.status(403).json({ error: error.message });
                    return;
                }
                if (error.message.includes('already a member')) {
                    res.status(400).json({ error: error.message });
                    return;
                }
                if (error.message.includes('banned from this workspace')) {
                    res.status(403).json({ error: error.message });
                    return;
                }
                if (error.message === 'User not found') {
                    res.status(404).json({ error: 'User to add not found' });
                    return;
                }
                if (error.message === 'Workspace not found') {
                    res.status(404).json({ error: 'Workspace not found' });
                    return;
                }
            }
            
            res.status(500).json({ error: error instanceof Error ? error.message : 'Failed to add member' });
        }
    }

    async leaveWorkspace(req: Request, res: Response): Promise<void> {
        try {
            const userId = req.user!._id;

            const workspaceId = req.params.id;

            const workspace = await workspaceService.leaveWorkspace(workspaceId, userId);

            res.status(200).json({
                message: 'Successfully left the workspace',
                data: { workspace },
            });
        } catch (error) {
            console.error('Error leaving workspace:', error);
            
            if (error instanceof Error) {
                if (error.message.includes('Cannot leave your personal workspace')) {
                    res.status(403).json({ error: error.message });
                    return;
                }
                if (error.message.includes('Owner cannot leave')) {
                    res.status(403).json({ error: error.message });
                    return;
                }
                if (error.message === 'You are not a member of this workspace') {
                    res.status(400).json({ error: error.message });
                    return;
                }
                if (error.message === 'Workspace not found') {
                    res.status(404).json({ error: 'Workspace not found' });
                    return;
                }
            }
            
            res.status(500).json({ error: 'Failed to leave workspace' });
        }
    }

    async banMember(req: Request, res: Response): Promise<void> {
        try {
            const requestingUserId = req.user!._id;

            const workspaceId = req.params.id;
            const userIdToBan = req.params.userId;

            const workspace = await workspaceService.banMember(workspaceId, requestingUserId, userIdToBan);

            res.status(200).json({
                message: 'Member banned successfully',
                data: { workspace },
            });
        } catch (error) {
            console.error('Error banning member:', error);
            
            if (error instanceof Error) {
                if (error.message.includes('Only workspace owner')) {
                    res.status(403).json({ error: error.message });
                    return;
                }
                if (error.message.includes('Cannot ban members from personal workspace')) {
                    res.status(403).json({ error: error.message });
                    return;
                }
                if (error.message.includes('Cannot ban the workspace owner')) {
                    res.status(400).json({ error: error.message });
                    return;
                }
                if (error.message === 'User not found') {
                    res.status(404).json({ error: 'User to ban not found' });
                    return;
                }
                if (error.message === 'Workspace not found') {
                    res.status(404).json({ error: 'Workspace not found' });
                    return;
                }
            }
            
            res.status(500).json({ error: error instanceof Error ? error.message : 'Failed to ban member' });
        }
    }

    async updateWorkspaceProfile(req: Request, res: Response): Promise<void> {
        try {
            const requestingUserId = req.user!._id;
            const workspaceId = req.params.id;

            const workspace = await workspaceService.updateWorkspaceProfile(
                workspaceId, 
                requestingUserId, 
                req.body as UpdateWorkspaceProfileRequest
            );

            res.status(200).json({
                message: 'Workspace profile updated successfully',
                data: { workspace },
            });
        } catch (error) {
            console.error('Error updating workspace profile:', error);
            
            if (error instanceof Error) {
                if (error.message.includes('Only workspace owner')) {
                    res.status(403).json({ error: error.message });
                    return;
                }
                if (error.message === 'Workspace not found') {
                    res.status(404).json({ error: 'Workspace not found' });
                    return;
                }
            }
            
            res.status(500).json({ error: error instanceof Error ? error.message : 'Failed to update workspace profile' });
        }
    }

    async updateWorkspacePicture(req: Request, res: Response): Promise<void> {
        try {
            const requestingUserId = req.user!._id;

            const workspaceId = req.params.id;

            const workspace = await workspaceService.updateWorkspacePicture(
                workspaceId, 
                requestingUserId, 
                (req.body as UpdateWorkspacePictureRequest).profilePicture
            );

            res.status(200).json({
                message: 'Workspace picture updated successfully',
                data: { workspace },
            });
        } catch (error) {
            console.error('Error updating workspace picture:', error);
            
            if (error instanceof Error) {
                if (error.message.includes('Only workspace owner')) {
                    res.status(403).json({ error: error.message });
                    return;
                }
                if (error.message === 'Workspace not found') {
                    res.status(404).json({ error: 'Workspace not found' });
                    return;
                }
            }
            
            res.status(500).json({ error: error instanceof Error ? error.message : 'Failed to update workspace picture' });
        }
    }

    async deleteWorkspace(req: Request, res: Response): Promise<void> {
        try {
            const requestingUserId = req.user!._id;

            const workspaceId = req.params.id;

            const workspace = await workspaceService.deleteWorkspace(workspaceId, requestingUserId);

            res.status(200).json({
                message: 'Workspace and all its notes deleted successfully',
                data: { workspace },
            });
        } catch (error) {
            console.error('Error deleting workspace:', error);
            
            if (error instanceof Error) {
                if (error.message.includes('Only workspace owner')) {
                    res.status(403).json({ error: error.message });
                    return;
                }
                if (error.message.includes('Cannot delete your personal workspace')) {
                    res.status(403).json({ error: error.message });
                    return;
                }
                if (error.message === 'Workspace not found') {
                    res.status(404).json({ error: 'Workspace not found' });
                    return;
                }
            }
            
            res.status(500).json({ error: error instanceof Error ? error.message : 'Failed to delete workspace' });
        }
    }

    async pollForNewMessages(req: Request, res: Response): Promise<void> {
        try {
            const userId = req.user!._id;

            const workspaceId = req.params.id;
            const hasNewMessages = await workspaceService.checkForNewChatMessages(workspaceId);

            res.status(200).json({
                message: 'Polling check completed',
                data: { hasNewMessages },
            });
        } catch (error) {
            console.error('Error polling for new messages:', error);
            
            if (error instanceof Error) {
                if (error.message === 'Workspace not found') {
                    res.status(404).json({ error: 'Workspace not found' });
                    return;
                }
            }
            
            res.status(500).json({ error: error instanceof Error ? error.message : 'Failed to poll for new messages' });
        }
    }
}
