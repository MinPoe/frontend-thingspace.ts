package com.cpen321.usermanagement.data.repository

import android.util.Log
import com.cpen321.usermanagement.data.local.preferences.TokenManager
import com.cpen321.usermanagement.data.remote.api.AddMemberRequest
import com.cpen321.usermanagement.data.remote.api.GetWorkspaceData
import com.cpen321.usermanagement.data.remote.api.WorkspaceInterface
import com.cpen321.usermanagement.data.remote.dto.*
import com.cpen321.usermanagement.data.remote.api.UpdateWorkspacePictureRequest
import com.cpen321.usermanagement.data.remote.api.UpdateWorkspaceProfileRequest
import com.cpen321.usermanagement.utils.JsonUtils.parseErrorMessage
import jakarta.inject.Inject
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WorkspaceRepositoryImpl @Inject constructor(
    private val workspaceApi: WorkspaceInterface,
    private val tokenManager: TokenManager
) : WorkspaceRepository {

    companion object {
        private const val TAG = "WorkspaceRepository"
        private const val AUTH_HEADER_PLACEHOLDER = "" // Handled by Interceptor
    }

    override suspend fun getWorkspace(workspaceId: String): Result<Workspace> {
        return try {
            val response = workspaceApi.getWorkspace(AUTH_HEADER_PLACEHOLDER, workspaceId)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.workspace)
            } else {
                val errorMessage = parseErrorMessage(
                    response.errorBody()?.string(),
                    "Failed to fetch workspace."
                )
                Log.e(TAG, "getWorkspace error: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: SocketTimeoutException) { return handleException("getWorkspace", e) }
        catch (e: UnknownHostException) { return handleException("getWorkspace", e) }
        catch (e: IOException) { return handleException("getWorkspace", e) }
        catch (e: HttpException) { return handleException("getWorkspace", e) }
    }

    override suspend fun getPersonalWorkspace(): Result<Workspace> {
        return try {
            val response = workspaceApi.getPersonalWorkspace(AUTH_HEADER_PLACEHOLDER)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.workspace)
            } else {
                val errorMessage = parseErrorMessage(
                    response.errorBody()?.string(),
                    "Failed to fetch personal workspace."
                )
                Log.e(TAG, "getPersonalWorkspace error: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: SocketTimeoutException) { return handleException("getPersonalWorkspace", e) }
        catch (e: UnknownHostException) { return handleException("getPersonalWorkspace", e) }
        catch (e: IOException) { return handleException("getPersonalWorkspace", e) }
        catch (e: HttpException) { return handleException("getPersonalWorkspace", e) }
    }

    override suspend fun getWorkspacesForUser(): Result<List<Workspace>> {
        return try {
            val response = workspaceApi.getWorkspacesForUser(AUTH_HEADER_PLACEHOLDER)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.workspaces)
            } else {
                val errorMessage = parseErrorMessage(
                    response.errorBody()?.string(),
                    "Failed to load workspaces."
                )
                Log.e(TAG, "getWorkspacesForUser error: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: SocketTimeoutException) { return handleException("getWorkspacesForUser", e) }
        catch (e: UnknownHostException) { return handleException("getWorkspacesForUser", e) }
        catch (e: IOException) { return handleException("getWorkspacesForUser", e) }
        catch (e: HttpException) { return handleException("getWorkspacesForUser", e) }
    }

    override suspend fun getWorkspaceMembers(workspaceId: String): Result<List<User>> {
        return try {
            val response = workspaceApi.getWorkspaceMembers(AUTH_HEADER_PLACEHOLDER, workspaceId)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.members)
            } else {
                val errorMessage = parseErrorMessage(
                    response.errorBody()?.string(),
                    "Failed to load workspace members."
                )
                Log.e(TAG, "getWorkspaceMembers error: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: SocketTimeoutException) { return handleException("getWorkspaceMembers", e) }
        catch (e: UnknownHostException) { return handleException("getWorkspaceMembers", e) }
        catch (e: IOException) { return handleException("getWorkspaceMembers", e) }
        catch (e: HttpException) { return handleException("getWorkspaceMembers", e) }
    }

    override suspend fun createWorkspace(
        managerId: String,
        workspaceName: String,
        workspaceProfilePicture: String,
        workspaceDescription: String
    ): Result<String> {
        return try {
            val body = Profile(
                name = workspaceName,
                description = workspaceDescription,
                imagePath = workspaceProfilePicture,
            )
            val response = workspaceApi.createWorkspace(AUTH_HEADER_PLACEHOLDER, body)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.workspaceId)
            } else {
                val errorMessage = parseErrorMessage(
                    response.errorBody()?.string(),
                    "Failed to create workspace."
                )
                Log.e(TAG, "createWorkspace error: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: SocketTimeoutException) { return handleException("createWorkspace", e) }
        catch (e: UnknownHostException) { return handleException("createWorkspace", e) }
        catch (e: IOException) { return handleException("createWorkspace", e) }
        catch (e: HttpException) { return handleException("createWorkspace", e) }
    }

    override suspend fun updateWorkspaceProfile(
        workspaceId: String,
        workspaceName: String,
        workspaceDescription: String
    ): Result<Unit> {
        return try {
            val body = UpdateWorkspaceProfileRequest(workspaceName, workspaceDescription)
            val response = workspaceApi.updateWorkspaceProfile(AUTH_HEADER_PLACEHOLDER, workspaceId, body)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMessage = parseErrorMessage(
                    response.errorBody()?.string(),
                    "Failed to update workspace profile."
                )
                Result.failure(Exception(errorMessage))
            }
        } catch (e: SocketTimeoutException) { return handleException("updateWorkspaceProfile", e) }
        catch (e: UnknownHostException) { return handleException("updateWorkspaceProfile", e) }
        catch (e: IOException) { return handleException("updateWorkspaceProfile", e) }
        catch (e: HttpException) { return handleException("updateWorkspaceProfile", e) }
    }

    override suspend fun updateWorkspacePicture(workspaceId: String, workspaceProfilePicture: String): Result<Unit> {
        return try {
            val response = workspaceApi.updateWorkspacePicture(
                AUTH_HEADER_PLACEHOLDER,
                workspaceId,
                UpdateWorkspacePictureRequest(workspaceProfilePicture)
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val err = parseErrorMessage(response.errorBody()?.string(), "Failed to update picture.")
                Result.failure(Exception(err))
            }
        } catch (e: SocketTimeoutException) { return handleException("updateWorkspacePicture", e) }
        catch (e: UnknownHostException) { return handleException("updateWorkspacePicture", e) }
        catch (e: IOException) { return handleException("updateWorkspacePicture", e) }
        catch (e: HttpException) { return handleException("updateWorkspacePicture", e) }
    }

    override suspend fun deleteWorkspace(workspaceId: String): Result<Unit> {
        return try {
            val response = workspaceApi.deleteWorkspace(AUTH_HEADER_PLACEHOLDER, workspaceId)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(parseErrorMessage(response.errorBody()?.string(), "Failed to delete workspace.")))
        } catch (e: SocketTimeoutException) { return handleException("deleteWorkspace", e) }
        catch (e: UnknownHostException) { return handleException("deleteWorkspace", e) }
        catch (e: IOException) { return handleException("deleteWorkspace", e) }
        catch (e: HttpException) { return handleException("deleteWorkspace", e) }
    }

    override suspend fun addMember(userId: String, workspaceId: String): Result<Unit> {
        return try {
            val response = workspaceApi.addMemberToWorkspace(
                AUTH_HEADER_PLACEHOLDER, workspaceId, AddMemberRequest(userId)
            )
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(parseErrorMessage(response.errorBody()?.string(), "Failed to add member.")))
        } catch (e: SocketTimeoutException) { return handleException("addMember", e) }
        catch (e: UnknownHostException) { return handleException("addMember", e) }
        catch (e: IOException) { return handleException("addMember", e) }
        catch (e: HttpException) { return handleException("addMember", e) }
    }

    override suspend fun leave(userId: String, workspaceId: String): Result<Unit>{
        return try {
            val response = workspaceApi.leaveWorkspace(AUTH_HEADER_PLACEHOLDER, workspaceId)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(parseErrorMessage(response.errorBody()?.string(), "Failed to remove user.")))
        } catch (e: SocketTimeoutException) { return handleException("banMember", e) }
        catch (e: UnknownHostException) { return handleException("banMember", e) }
        catch (e: IOException) { return handleException("banMember", e) }
        catch (e: HttpException) { return handleException("banMember", e) }
    }

    override suspend fun banMember(userId: String, workspaceId: String): Result<Unit> {
        return try {
            val response = workspaceApi.banWorkspaceMember(AUTH_HEADER_PLACEHOLDER, workspaceId, userId)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(parseErrorMessage(response.errorBody()?.string(), "Failed to remove user.")))
        } catch (e: Exception) {
            handleException("banMember", e)
        }
    }

    override suspend fun getMembershipStatus(userId: String, workspaceId: String): Result<WsMembershipStatus> {
        return try {
            val response = workspaceApi.getMembershipStatus(AUTH_HEADER_PLACEHOLDER, workspaceId, userId)
            if (response.isSuccessful && response.body()?.data != null) {
                val status = when (response.body()!!.data!!.status) {
                    "MEMBER" -> WsMembershipStatus.MEMBER
                    "OWNER" -> WsMembershipStatus.MANAGER
                    "BANNED" -> WsMembershipStatus.BANNED
                    else -> WsMembershipStatus.NONMEMBER
                }
                Result.success(status)
            } else {
                Result.failure(Exception(parseErrorMessage(response.errorBody()?.string(), "Failed to fetch membership status.")))
            }
        } catch (e: SocketTimeoutException) { return handleException("getMembershipStatus", e) }
        catch (e: UnknownHostException) { return handleException("getMembershipStatus", e) }
        catch (e: IOException) { return handleException("getMembershipStatus", e) }
        catch (e: HttpException) { return handleException("getMembershipStatus", e) }
    }

    override suspend fun getAllTags(workspaceId: String): Result<List<String>> {
        return try {
            val response = workspaceApi.getWorkspaceTags(AUTH_HEADER_PLACEHOLDER, workspaceId)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.tags)
            } else {
                Result.failure(Exception(parseErrorMessage(response.errorBody()?.string(), "Failed to fetch tags.")))
            }
        } catch (e: SocketTimeoutException) { return handleException("getAllTags", e) }
        catch (e: UnknownHostException) { return handleException("getAllTags", e) }
        catch (e: IOException) { return handleException("getAllTags", e) }
        catch (e: HttpException) { return handleException("getAllTags", e) }
    }

    override suspend fun chatPoll(workspaceId: String): Result<Boolean>{
        return try {
            val response = workspaceApi.pollChat(AUTH_HEADER_PLACEHOLDER, workspaceId)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.changed)
            } else {
                Result.failure(Exception(parseErrorMessage(response.errorBody()?.string(), "Failed to check for chat updates.")))
            }
        } catch (e: SocketTimeoutException) { return handleException("chatPoll", e) }
        catch (e: UnknownHostException) { return handleException("chatPoll", e) }
        catch (e: IOException) { return handleException("chatPoll", e) }
        catch (e: HttpException) { return handleException("chatPoll", e) }
    }

    override suspend fun pollForNewMessages(workspaceId: String): Result<Boolean> {
        return try {
            val response = workspaceApi.pollForNewMessages(
                authHeader = AUTH_HEADER_PLACEHOLDER,
                workspaceId = workspaceId
            )
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.hasNewMessages)
            } else {
                Result.success(false)  // Don't fail on error, just return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Poll error (ignoring)", e)
            Result.success(false)  // Don't fail, just return false
        }
    }

    private fun <T> handleException(method: String, e: Exception): Result<T> {
        when (e) {
            is SocketTimeoutException -> Log.e(TAG, "Timeout in $method", e)
            is UnknownHostException -> Log.e(TAG, "No internet in $method", e)
            is IOException -> Log.e(TAG, "IO exception in $method", e)
            is HttpException -> Log.e(TAG, "HTTP error ${e.code()} in $method", e)
            else -> Log.e(TAG, "Unexpected error in $method", e)
        }
        return Result.failure(e)
    }
}
