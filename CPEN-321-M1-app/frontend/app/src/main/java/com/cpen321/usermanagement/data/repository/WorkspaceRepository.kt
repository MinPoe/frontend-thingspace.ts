package com.cpen321.usermanagement.data.repository

import com.cpen321.usermanagement.data.remote.dto.User
import com.cpen321.usermanagement.data.remote.dto.Workspace

interface WorkspaceRepository {
    fun getWorkspace(workspaceId:String): Result<Workspace>
    fun getWorkspacesForUser(userId:String): Result<List<Workspace>>
    fun getWorkspaceMembers(workspaceId:String): Result<List<User>>
    fun createWorkspace(managerId:String,
                        workspaceName:String,
                        workspaceProfilePicture:String,
                        workspaceDescription:String): Result<Unit>
    fun updateWorkspaceProfile(workspaceId: String,
                        workspaceName:String,
                        workspaceDescription:String): Result<Unit>
    fun updateWorkspacePicture(workspaceId: String,
                               workspaceProfilePicture:String):Result<Unit>
    fun deleteWorkspace(workspaceId: String): Result<Unit>
    fun addMember(userId:String, workspaceId: String): Result<Unit>
    fun banMember(userId:String, workspaceId: String): Result<Unit>
    fun getMembershipStatus(userId:String): Result<WsMembershipStatus>
    fun getAllTags(workspaceId: String): Result<List<String>>
}

enum class WsMembershipStatus{
    MEMBER, MANAGER, NONMEMBER, BANNED
}