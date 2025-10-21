package com.cpen321.usermanagement.data.repository

import com.cpen321.usermanagement.data.remote.dto.User
import com.cpen321.usermanagement.data.remote.dto.Workspace

interface WorkspaceRepository {
    suspend fun getWorkspace(workspaceId:String): Result<Workspace>
    suspend fun getWorkspacesForUser(userId:String): Result<List<Workspace>>
    suspend fun getWorkspaceMembers(workspaceId:String): Result<List<User>>
    suspend fun createWorkspace(managerId:String,
                        workspaceName:String,
                        workspaceProfilePicture:String,
                        workspaceDescription:String): Result<String>
    suspend fun updateWorkspaceProfile(workspaceId: String,
                        workspaceName:String,
                        workspaceDescription:String): Result<Unit>
    suspend fun updateWorkspacePicture(workspaceId: String,
                               workspaceProfilePicture:String):Result<Unit>
    suspend fun deleteWorkspace(workspaceId: String): Result<Unit>
    suspend fun addMember(userId:String, workspaceId: String): Result<Unit>
    suspend fun leave(userId:String, workspaceId: String): Result<Unit>
    suspend fun banMember(userId:String, workspaceId: String): Result<Unit>
    suspend fun getMembershipStatus(userId:String, workspaceId: String): Result<WsMembershipStatus>
    suspend fun getAllTags(workspaceId: String): Result<List<String>>
}

enum class WsMembershipStatus{
    MEMBER, MANAGER, NONMEMBER, BANNED
}