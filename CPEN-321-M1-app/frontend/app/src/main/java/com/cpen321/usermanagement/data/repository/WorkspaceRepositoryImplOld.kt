package com.cpen321.usermanagement.data.repository

import android.util.Log
import androidx.core.os.requestProfiling
import com.cpen321.usermanagement.data.remote.api.WorkspaceInterface
import com.cpen321.usermanagement.data.remote.dto.Workspace
import com.cpen321.usermanagement.data.remote.dto.User
import com.cpen321.usermanagement.data.remote.dto.Profile
import com.cpen321.usermanagement.data.remote.api.RetrofitClient

/**
 * !!! MOCK IMPL 4 NOW !!!
 * **/
import javax.inject.Singleton
import javax.inject.Inject

@Singleton
class WorkspaceRepositoryImplOld @Inject constructor(
    val workspaceInterface: WorkspaceInterface
) : WorkspaceRepository {

    override suspend fun getWorkspace(workspaceId: String): Result<Workspace> {
        return Result.success(
            Workspace(
                _id = workspaceId,
//                name = "Workspace $workspaceId",
                profile = Profile(
                    imagePath = "picture_$workspaceId.png",
                    name = "Workspace $workspaceId",
                    description = "Description for workspace $workspaceId"
                ),
//                ownerId = "owner_$workspaceId",
//                members = listOf("owner_$workspaceId", "member1_$workspaceId")
            )
        )
    }

    override suspend fun getWorkspacesForUser(): Result<List<Workspace>> {
//        val workspaces = (1..3).map {
//            Workspace(
//                _id = "ws_${userId}_$it",
////                name = "Workspace $it for user $userId",
//                profile = Profile(
//                    imagePath = "pic_user_${userId}_$it.png",
//                    name = "Workspace $it for user $userId",
//                    description = "Mock workspace number $it for user $userId"
//                ),
////                ownerId = userId,
////                members = listOf(userId, "member1_$it")
//            )

//        }
        val workspaceRequest = workspaceInterface.getWorkspacesForUser("")
        if (workspaceRequest.isSuccessful)
        {
            val workspaces = workspaceRequest.body()!!.data!!.workspaces
            return Result.success(workspaces)
        }
        else{
            return  Result.failure(error(workspaceRequest.body()!!.error.toString()))
        }
    }

    override suspend fun getWorkspaceMembers(workspaceId: String): Result<List<User>> {
        val members = (1..4).map {
            User(
                _id = "u_${workspaceId}_$it",
                email = "user$it@${workspaceId}.com",
                createdAt = null,
                updatedAt = null,
                profile = Profile(
                    imagePath = "profile_${workspaceId}_$it.png",
                    name = "User $it of $workspaceId",
                    description = "Bio of user $it in workspace $workspaceId"
                )
            )
        }
        return Result.success(members)
    }

    override suspend fun createWorkspace(
        managerId: String,
        workspaceName: String,
        workspaceProfilePicture: String,
        workspaceDescription: String
    ): Result<String> {
        val profile = Profile(workspaceProfilePicture, workspaceName, workspaceDescription)
        val response = workspaceInterface.createWorkspace("", profile)
        if (response.isSuccessful){
            Log.d("d", response.body().toString())
            return Result.success(response.body()!!.data!!.workspaceId)
        }
        else{
            return Result.failure(error(response.body()!!.error.toString()))
        }

    }

    override suspend fun updateWorkspaceProfile(
        workspaceId: String,
        workspaceName: String,
        workspaceDescription: String
    ): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun updateWorkspacePicture(
        workspaceId: String,
        workspaceProfilePicture: String
    ): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun deleteWorkspace(workspaceId: String): Result<Unit> {
        val request = workspaceInterface.deleteWorkspace("", workspaceId)
        if (request.isSuccessful){
            return Result.success(Unit)
        }
        else{
            return Result.failure(error("Failed to Delete Workspace..."))
        }
    }

    override suspend fun addMember(userId: String, workspaceId: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun banMember(userId: String, workspaceId: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun getMembershipStatus(userId: String, workspaceId: String): Result<WsMembershipStatus> {
        val status = when (userId.length % 4) {
            0 -> WsMembershipStatus.MEMBER
            1 -> WsMembershipStatus.MANAGER
            2 -> WsMembershipStatus.NONMEMBER
            else -> WsMembershipStatus.BANNED
        }
        return Result.success(WsMembershipStatus.MANAGER)//4 testing
    }

    override suspend fun getAllTags(workspaceId: String): Result<List<String>> {
        val tags = listOf("tag1_$workspaceId", "tag2_$workspaceId", "tag3_$workspaceId")
        return Result.success(tags)
    }

    override suspend fun leave(userId: String, workspaceId: String): Result<Unit> {
        return Result.success(Unit)
    }
}
