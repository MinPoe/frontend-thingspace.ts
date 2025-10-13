package com.cpen321.usermanagement.data.repository

import com.cpen321.usermanagement.data.remote.dto.Workspace
import com.cpen321.usermanagement.data.remote.dto.User

/**
 * !!! MOCK IMPL 4 NOW !!!
 * **/
import javax.inject.Singleton
import javax.inject.Inject

@Singleton
class WorkspaceRepositoryImpl @Inject constructor() : WorkspaceRepository {

    override fun getWorkspace(workspaceId: String): Result<Workspace> {
        return Result.success(
            Workspace(
                _id = workspaceId,
                workspaceName = "Workspace $workspaceId",
                workspacePicture = "picture_$workspaceId.png",
                workspaceDescription = "Description for workspace $workspaceId"
            )
        )
    }

    override fun getWorkspacesForUser(userId: String): Result<List<Workspace>> {
        val workspaces = (1..3).map {
            Workspace(
                _id = "ws_${userId}_$it",
                workspaceName = "Workspace $it for user $userId",
                workspacePicture = "pic_user_${userId}_$it.png",
                workspaceDescription = "Mock workspace number $it for user $userId"
            )
        }
        return Result.success(workspaces)
    }

    override fun getWorkspaceMembers(workspaceId: String): Result<List<User>> {
        val members = (1..4).map {
            User(
                _id = "u_${workspaceId}_$it",
                email = "user$it@${workspaceId}.com",
                name = "User $it of $workspaceId",
                bio = "Bio of user $it in workspace $workspaceId",
                profilePicture = "profile_${workspaceId}_$it.png"
            )
        }
        return Result.success(members)
    }

    override fun createWorkspace(
        managerId: String,
        workspaceName: String,
        workspaceProfilePicture: String,
        workspaceDescription: String
    ): Result<Unit> {
        return Result.success(Unit)
    }

    override fun updateWorkspaceProfile(
        workspaceId: String,
        workspaceName: String,
        workspaceDescription: String
    ): Result<Unit> {
        return Result.success(Unit)
    }

    override fun updateWorkspacePicture(
        workspaceId: String,
        workspaceProfilePicture: String
    ): Result<Unit> {
        return Result.success(Unit)
    }

    override fun deleteWorkspace(workspaceId: String): Result<Unit> {
        return Result.success(Unit)
    }

    override fun addMember(userId: String, workspaceId: String): Result<Unit> {
        return Result.success(Unit)
    }

    override fun banMember(userId: String, workspaceId: String): Result<Unit> {
        return Result.success(Unit)
    }

    override fun getMembershipStatus(userId: String): Result<WsMembershipStatus> {
        val status = when (userId.hashCode() % 4) {
            0 -> WsMembershipStatus.MEMBER
            1 -> WsMembershipStatus.MANAGER
            2 -> WsMembershipStatus.NONMEMBER
            else -> WsMembershipStatus.BANNED
        }
        return Result.success(status)
    }

    override fun getAllTags(workspaceId: String): Result<List<String>> {
        val tags = listOf("tag1_$workspaceId", "tag2_$workspaceId", "tag3_$workspaceId")
        return Result.success(tags)
    }
}
