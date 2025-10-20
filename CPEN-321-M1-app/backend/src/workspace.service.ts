import mongoose from 'mongoose';
import { Workspace, WsMembershipStatus } from './workspace.types';
import { workspaceModel } from './workspace.model';
import { noteModel } from './note.model';
import { userModel } from './user.model';

export class WorkspaceService {
    async getWorkspace(workspaceId: string, userId: mongoose.Types.ObjectId): Promise<Workspace | null> {
        const workspace = await workspaceModel.findById(workspaceId);
        
        if (!workspace) {
            return null;
        }

        // Check if user is owner or member
        const isMember = workspace.members.some(memberId => memberId.toString() === userId.toString());
        const isOwner = workspace.ownerId.toString() === userId.toString();

        if (!isMember && !isOwner) {
            throw new Error('Access denied: You are not a member of this workspace');
        }

        return {
            _id: workspace._id.toString(),
            name: workspace.name,
            profile: workspace.profile,
            ownerId: workspace.ownerId.toString(),
            members: workspace.members.map(id => id.toString()),
            createdAt: workspace.createdAt,
            updatedAt: workspace.updatedAt,
        };
    }

    async getWorkspacesForUser(userId: mongoose.Types.ObjectId): Promise<Workspace[]> {
        // Find workspaces where user is owner or member
        const workspaces = await workspaceModel.find({
            $or: [
                { ownerId: userId },
                { members: userId }
            ]
        }).sort({ createdAt: -1 });

        return workspaces.map(workspace => ({
            _id: workspace._id.toString(),
            name: workspace.name,
            profile: workspace.profile,
            ownerId: workspace.ownerId.toString(),
            members: workspace.members.map(id => id.toString()),
            createdAt: workspace.createdAt,
            updatedAt: workspace.updatedAt,
        }));
    }

    async getWorkspaceMembers(workspaceId: string, userId: mongoose.Types.ObjectId): Promise<any[]> {
        const workspace = await workspaceModel.findById(workspaceId).populate('members ownerId', 'name email profilePicture');
        
        if (!workspace) {
            throw new Error('Workspace not found');
        }

        // Check if user has access to this workspace
        const isMember = workspace.members.some((member: any) => member._id.toString() === userId.toString());
        const isOwner = workspace.ownerId.toString() === userId.toString();

        if (!isMember && !isOwner) {
            throw new Error('Access denied: You are not a member of this workspace');
        }

        return workspace.members as any[];
    }

    async getAllTags(workspaceId: string, userId: mongoose.Types.ObjectId): Promise<string[]> {
        const workspace = await workspaceModel.findById(workspaceId);
        
        if (!workspace) {
            throw new Error('Workspace not found');
        }

        // Check if user has access
        const isMember = workspace.members.some(memberId => memberId.toString() === userId.toString());
        const isOwner = workspace.ownerId.toString() === userId.toString();

        if (!isMember && !isOwner) {
            throw new Error('Access denied: You are not a member of this workspace');
        }

        // Get all notes in this workspace
        const notes = await noteModel.find({ workspaceId });

        // Extract all tags and get unique ones
        const allTags = notes.flatMap(note => note.tags || []);
        const uniqueTags = [...new Set(allTags)];

        return uniqueTags;
    }

    async getMembershipStatus(workspaceId: string, checkUserId: string): Promise<WsMembershipStatus> {
        const workspace = await workspaceModel.findById(workspaceId);
        
        if (!workspace) {
            throw new Error('Workspace not found');
        }

        // Check if user is owner
        if (workspace.ownerId.toString() === checkUserId) {
            return WsMembershipStatus.OWNER;
        }

        // Check if user is banned
        const isBanned = workspace.bannedMembers?.some(id => id.toString() === checkUserId);
        if (isBanned) {
            return WsMembershipStatus.BANNED;
        }

        // Check if user is member
        const isMember = workspace.members.some(id => id.toString() === checkUserId);
        if (isMember) {
            return WsMembershipStatus.MEMBER;
        }

        return WsMembershipStatus.NOT_MEMBER;
    }

    async addMember(workspaceId: string, requestingUserId: mongoose.Types.ObjectId, userIdToAdd: string): Promise<Workspace> {
        const workspace = await workspaceModel.findById(workspaceId);
        
        if (!workspace) {
            throw new Error('Workspace not found');
        }

        // Only owner can add members
        if (workspace.ownerId.toString() !== requestingUserId.toString()) {
            throw new Error('Access denied: Only workspace owner can add members');
        }

        // Check if user to add exists
        const userToAdd = await userModel.findById(new mongoose.Types.ObjectId(userIdToAdd));
        if (!userToAdd) {
            throw new Error('User not found');
        }

        // Check if user is banned
        const isBanned = workspace.bannedMembers?.some(id => id.toString() === userIdToAdd);
        if (isBanned) {
            throw new Error('User is banned from this workspace');
        }

        // Check if user is already a member
        const alreadyMember = workspace.members.some(id => id.toString() === userIdToAdd);
        if (alreadyMember) {
            throw new Error('User is already a member');
        }

        // Add member
        workspace.members.push(new mongoose.Types.ObjectId(userIdToAdd));
        await workspace.save();

        return {
            _id: workspace._id.toString(),
            name: workspace.name,
            profile: workspace.profile,
            ownerId: workspace.ownerId.toString(),
            members: workspace.members.map(id => id.toString()),
            createdAt: workspace.createdAt,
            updatedAt: workspace.updatedAt,
        };
    }

    async banMember(workspaceId: string, requestingUserId: mongoose.Types.ObjectId, userIdToBan: string): Promise<Workspace> {
        const workspace = await workspaceModel.findById(workspaceId);
        
        if (!workspace) {
            throw new Error('Workspace not found');
        }

        // Only owner can ban members
        if (workspace.ownerId.toString() !== requestingUserId.toString()) {
            throw new Error('Access denied: Only workspace owner can ban members');
        }

        // Can't ban the owner
        if (workspace.ownerId.toString() === userIdToBan) {
            throw new Error('Cannot ban the workspace owner');
        }

        // Check if user exists
        const userToBan = await userModel.findById(new mongoose.Types.ObjectId(userIdToBan));
        if (!userToBan) {
            throw new Error('User not found');
        }

        // Remove from members if present
        workspace.members = workspace.members.filter(id => id.toString() !== userIdToBan);

        // Add to banned list if not already banned
        const alreadyBanned = workspace.bannedMembers?.some(id => id.toString() === userIdToBan);
        if (!alreadyBanned) {
            if (!workspace.bannedMembers) {
                workspace.bannedMembers = [];
            }
            workspace.bannedMembers.push(new mongoose.Types.ObjectId(userIdToBan));
        }

        await workspace.save();

        return {
            _id: workspace._id.toString(),
            name: workspace.name,
            profile: workspace.profile,
            ownerId: workspace.ownerId.toString(),
            members: workspace.members.map(id => id.toString()),
            createdAt: workspace.createdAt,
            updatedAt: workspace.updatedAt,
        };
    }
}

export const workspaceService = new WorkspaceService();

