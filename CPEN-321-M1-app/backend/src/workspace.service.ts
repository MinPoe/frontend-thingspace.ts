import mongoose from 'mongoose';
import { Workspace, WsMembershipStatus, CreateWorkspaceRequest } from './workspace.types';
import { workspaceModel } from './workspace.model';
import { noteModel } from './note.model';
import { userModel } from './user.model';
import { IUser } from './user.types';

export class WorkspaceService {
    async createWorkspace(userId: mongoose.Types.ObjectId, data: CreateWorkspaceRequest): Promise<Workspace> {
        const newWorkspace = await workspaceModel.create({
            name: data.name,
            profile: {
                imagePath: data.profilePicture || '',
                name: data.name,
                description: data.description || ''
            },
            ownerId: userId,
            members: [userId] // Owner is automatically a member
        });

        return {
            _id: newWorkspace._id.toString(),
            name: newWorkspace.name,
            profile: newWorkspace.profile,
            ownerId: newWorkspace.ownerId.toString(),
            members: newWorkspace.members.map(id => id.toString()),
            latestChatMessageTimestamp: newWorkspace.latestChatMessageTimestamp,
            createdAt: newWorkspace.createdAt,
            updatedAt: newWorkspace.updatedAt,
        };
    }

    async getWorkspace(workspaceId: string, userId: mongoose.Types.ObjectId): Promise<Workspace | null> {
        const workspace = await workspaceModel.findById(workspaceId);
        
        if (!workspace) {
            return null;
        }

        // Check if user is a member
        const isMember = workspace.members.some(memberId => memberId.toString() === userId.toString());

        if (!isMember) {
            throw new Error('Access denied: You are not a member of this workspace');
        }

        return {
            _id: workspace._id.toString(),
            name: workspace.name,
            profile: workspace.profile,
            ownerId: workspace.ownerId.toString(),
            members: workspace.members.map(id => id.toString()),
            latestChatMessageTimestamp: workspace.latestChatMessageTimestamp,
            createdAt: workspace.createdAt,
            updatedAt: workspace.updatedAt,
        };
    }

    async getWorkspacesForUser(userId: mongoose.Types.ObjectId): Promise<Workspace[]> {
        // Find workspaces where user is a member (includes owners)
        const workspaces = await workspaceModel.find({
            members: userId
        }).sort({ updatedAt: -1 });

        return workspaces.map(workspace => ({
            _id: workspace._id.toString(),
            name: workspace.name,
            profile: workspace.profile,
            ownerId: workspace.ownerId.toString(),
            members: workspace.members.map(id => id.toString()),
            latestChatMessageTimestamp: workspace.latestChatMessageTimestamp,
            createdAt: workspace.createdAt,
            updatedAt: workspace.updatedAt,
        }));
    }

    async getWorkspaceMembers(workspaceId: string, userId: mongoose.Types.ObjectId): Promise<IUser[]> {
        // 1. Fetch workspace
        const workspace = await workspaceModel.findById(workspaceId);
        
        if (!workspace) {
            throw new Error('Workspace not found');
        }
        // 3. Get list of user IDs from members array
        const memberIds = workspace.members;

        // 4. Fetch list of users
        const users = await userModel.findByIds(memberIds);

        return users;
    }

    async getAllTags(workspaceId: string, userId: mongoose.Types.ObjectId): Promise<string[]> {
        const workspace = await workspaceModel.findById(workspaceId);
        
        if (!workspace) {
            throw new Error('Workspace not found');
        }

        // Check if user is a member
        const isMember = workspace.members.some(memberId => memberId.toString() === userId.toString());

        if (!isMember) {
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

        // Check if requesting user is a member
        const isMember = workspace.members.some(memberId => memberId.toString() === requestingUserId.toString());
        if (!isMember) {
            throw new Error('Access denied: You are not a member of this workspace');
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
            latestChatMessageTimestamp: workspace.latestChatMessageTimestamp,
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
        const alreadyBanned = workspace.bannedMembers.some(id => id.toString() === userIdToBan);
        if (!alreadyBanned) {
            workspace.bannedMembers.push(new mongoose.Types.ObjectId(userIdToBan));
        }

        await workspace.save();

        return {
            _id: workspace._id.toString(),
            name: workspace.name,
            profile: workspace.profile,
            ownerId: workspace.ownerId.toString(),
            members: workspace.members.map(id => id.toString()),
            latestChatMessageTimestamp: workspace.latestChatMessageTimestamp,
            createdAt: workspace.createdAt,
            updatedAt: workspace.updatedAt,
        };
    }

    async updateWorkspaceProfile(workspaceId: string, requestingUserId: mongoose.Types.ObjectId, updateData: { name?: string; description?: string }): Promise<Workspace> {
        const workspace = await workspaceModel.findById(workspaceId);
        
        if (!workspace) {
            throw new Error('Workspace not found');
        }

        // Only owner can update workspace profile
        if (workspace.ownerId.toString() !== requestingUserId.toString()) {
            throw new Error('Only workspace owner can update workspace profile');
        }

        // Update profile fields
        if (updateData.name !== undefined) {
            workspace.profile.name = updateData.name;
            workspace.name = updateData.name; // Also update the top-level name field
        }
        if (updateData.description !== undefined) {
            workspace.profile.description = updateData.description;
        }

        await workspace.save();

        return {
            _id: workspace._id.toString(),
            name: workspace.name,
            profile: workspace.profile,
            ownerId: workspace.ownerId.toString(),
            members: workspace.members.map(id => id.toString()),
            latestChatMessageTimestamp: workspace.latestChatMessageTimestamp,
            createdAt: workspace.createdAt,
            updatedAt: workspace.updatedAt,
        };
    }

    async updateWorkspacePicture(workspaceId: string, requestingUserId: mongoose.Types.ObjectId, profilePicture: string): Promise<Workspace> {
        const workspace = await workspaceModel.findById(workspaceId);
        
        if (!workspace) {
            throw new Error('Workspace not found');
        }

        // Only owner can update workspace picture
        if (workspace.ownerId.toString() !== requestingUserId.toString()) {
            throw new Error('Only workspace owner can update workspace picture');
        }

        // Update profile picture
        workspace.profile.imagePath = profilePicture;
        await workspace.save();

        return {
            _id: workspace._id.toString(),
            name: workspace.name,
            profile: workspace.profile,
            ownerId: workspace.ownerId.toString(),
            members: workspace.members.map(id => id.toString()),
            latestChatMessageTimestamp: workspace.latestChatMessageTimestamp,
            createdAt: workspace.createdAt,
            updatedAt: workspace.updatedAt,
        };
    }

    async deleteWorkspace(workspaceId: string, requestingUserId: mongoose.Types.ObjectId): Promise<Workspace> {
        const workspace = await workspaceModel.findById(workspaceId);
        
        if (!workspace) {
            throw new Error('Workspace not found');
        }

        // Only owner can delete workspace
        if (workspace.ownerId.toString() !== requestingUserId.toString()) {
            throw new Error('Only workspace owner can delete the workspace');
        }

        // Delete all notes in the workspace
        await noteModel.deleteMany({ workspaceId });

        // Delete the workspace
        await workspaceModel.findByIdAndDelete(workspaceId);

        return {
            _id: workspace._id.toString(),
            name: workspace.name,
            profile: workspace.profile,
            ownerId: workspace.ownerId.toString(),
            members: workspace.members.map(id => id.toString()),
            latestChatMessageTimestamp: workspace.latestChatMessageTimestamp,
            createdAt: workspace.createdAt,
            updatedAt: workspace.updatedAt,
        };
    }

    async checkForNewChatMessages(workspaceId: string): Promise<boolean> {
        const workspace = await workspaceModel.findById(workspaceId);
        
        if (!workspace) {
            throw new Error('Workspace not found');
        }
        
        // Check if there are new messages within the polling interval (0.8 seconds for latency)
        const currentTime = new Date();
        const pollingInterval = 5000; // 1.3 seconds in milliseconds
        const timeThreshold = new Date(workspace.latestChatMessageTimestamp.getTime() + pollingInterval);
        
        return currentTime <= timeThreshold;
    }

    async updateLatestChatMessageTimestamp(workspaceId: string): Promise<void> {
        await workspaceModel.findByIdAndUpdate(
            workspaceId,
            { latestChatMessageTimestamp: new Date() }
        );
    }
}

export const workspaceService = new WorkspaceService();

