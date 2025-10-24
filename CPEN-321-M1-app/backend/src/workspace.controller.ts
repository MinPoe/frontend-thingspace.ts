import { Request, Response } from 'express';
import { workspaceService } from './workspace.service';

export class WorkspaceController {
    async createWorkspace(req: Request, res: Response): Promise<void> {
        try {
            const userId = req.user?._id;
            if (!userId) {
                res.status(401).json({ error: 'User not authenticated' });
                return;
            }

            const workspace = await workspaceService.createWorkspace(userId, req.body);

            res.status(201).json({
                message: 'Workspace created successfully',
                data: {workspaceId: workspace._id}
            });
        } catch (error) {
            console.error('Error creating workspace:', error);
            res.status(500).json({ error: 'Failed to create workspace' });
        }
    }

    async getWorkspace(req: Request, res: Response): Promise<void> {
        try {
            const userId = req.user?._id;
            if (!userId) {
                res.status(401).json({ error: 'User not authenticated' });
                return;
            }

            const workspaceId = req.params.id;
            const workspace = await workspaceService.getWorkspace(workspaceId, userId);

            if (!workspace) {
                res.status(404).json({ error: 'Workspace not found' });
                return;
            }

            res.status(200).json({
                message: 'Workspace retrieved successfully',
                data: { workspace },
            });
        } catch (error) {
            console.error('Error retrieving workspace:', error);
            
            if (error instanceof Error && error.message.includes('Access denied')) {
                res.status(403).json({ error: error.message });
                return;
            }
            
            res.status(500).json({ error: 'Failed to retrieve workspace' });
        }
    }

    async getWorkspacesForUser(req: Request, res: Response): Promise<void> {
        try {
            const userId = req.user?._id;
            if (!userId) {
                res.status(401).json({ error: 'User not authenticated' });
                return;
            }

            const workspaces = await workspaceService.getWorkspacesForUser(userId);

            res.status(200).json({
                message: 'Workspaces retrieved successfully',
                data: { workspaces },
            });
        } catch (error) {
            console.error('Error retrieving workspaces:', error);
            res.status(500).json({ error: 'Failed to retrieve workspaces' });
        }
    }

    async getWorkspaceMembers(req: Request, res: Response): Promise<void> {
        try {
            const userId = req.user?._id;
            if (!userId) {
                res.status(401).json({ error: 'User not authenticated' });
                return;
            }

            const workspaceId = req.params.id;
            const members = await workspaceService.getWorkspaceMembers(workspaceId, userId);

            res.status(200).json({
                message: 'Members retrieved successfully',
                data: { members },
            });
        } catch (error) {
            console.error('Error retrieving members:', error);
            
            if (error instanceof Error && error.message.includes('Access denied')) {
                res.status(403).json({ error: error.message });
                return;
            }
            
            res.status(500).json({ error: 'Failed to retrieve members' });
        }
    }

    async getAllTags(req: Request, res: Response): Promise<void> {
        try {
            const userId = req.user?._id;
            if (!userId) {
                res.status(401).json({ error: 'User not authenticated' });
                return;
            }

            const workspaceId = req.params.id;
            const tags = await workspaceService.getAllTags(workspaceId, userId);

            res.status(200).json({
                message: 'Tags retrieved successfully',
                data: { tags },
            });
        } catch (error) {
            console.error('Error retrieving tags:', error);
            
            if (error instanceof Error && error.message.includes('Access denied')) {
                res.status(403).json({ error: error.message });
                return;
            }
            
            res.status(500).json({ error: 'Failed to retrieve tags' });
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
            res.status(500).json({ error: 'Failed to retrieve membership status' });
        }
    }

    async addMember(req: Request, res: Response): Promise<void> {
        try {
            const requestingUserId = req.user?._id;
            if (!requestingUserId) {
                res.status(401).json({ error: 'User not authenticated' });
                return;
            }

            const workspaceId = req.params.id;
            const { userId } = req.body;

            if (!userId) {
                res.status(400).json({ error: 'userId is required' });
                return;
            }

            const workspace = await workspaceService.addMember(workspaceId, requestingUserId, userId);

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
            
            res.status(500).json({ error: 'Failed to add member' });
        }
    }

    async banMember(req: Request, res: Response): Promise<void> {
        try {
            const requestingUserId = req.user?._id;
            if (!requestingUserId) {
                res.status(401).json({ error: 'User not authenticated' });
                return;
            }

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
            
            res.status(500).json({ error: 'Failed to ban member' });
        }
    }

    async updateWorkspaceProfile(req: Request, res: Response): Promise<void> {
        try {
            const requestingUserId = req.user?._id;
            if (!requestingUserId) {
                res.status(401).json({ error: 'User not authenticated' });
                return;
            }

            const workspaceId = req.params.id;

            const workspace = await workspaceService.updateWorkspaceProfile(
                workspaceId, 
                requestingUserId, 
                req.body
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
            
            res.status(500).json({ error: 'Failed to update workspace profile' });
        }
    }

    async updateWorkspacePicture(req: Request, res: Response): Promise<void> {
        try {
            const requestingUserId = req.user?._id;
            if (!requestingUserId) {
                res.status(401).json({ error: 'User not authenticated' });
                return;
            }

            const workspaceId = req.params.id;

            const workspace = await workspaceService.updateWorkspacePicture(
                workspaceId, 
                requestingUserId, 
                req.body.profilePicture
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
            
            res.status(500).json({ error: 'Failed to update workspace picture' });
        }
    }

    async deleteWorkspace(req: Request, res: Response): Promise<void> {
        try {
            const requestingUserId = req.user?._id;
            if (!requestingUserId) {
                res.status(401).json({ error: 'User not authenticated' });
                return;
            }

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
                if (error.message === 'Workspace not found') {
                    res.status(404).json({ error: 'Workspace not found' });
                    return;
                }
            }
            
            res.status(500).json({ error: 'Failed to delete workspace' });
        }
    }

    async pollForNewMessages(req: Request, res: Response): Promise<void> {
        try {
            const userId = req.user?._id;
            if (!userId) {
                res.status(401).json({ error: 'User not authenticated' });
                return;
            }

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
            
            res.status(500).json({ error: 'Failed to poll for new messages' });
        }
    }
}
