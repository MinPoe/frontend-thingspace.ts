package com.cpen321.usermanagement.data.remote.api

import com.cpen321.usermanagement.data.remote.dto.ApiResponse
import com.cpen321.usermanagement.data.remote.dto.Profile
import com.cpen321.usermanagement.data.remote.dto.User
import com.cpen321.usermanagement.data.remote.dto.Workspace
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Header
import retrofit2.http.Path

data class CreateWorkspaceData( val workspaceId: String )

data class GetWorkspacesForUserData(
    val workspaces: List<Workspace>
)

data class GetWorkspaceData(
    val workspace: Workspace
)

data class UpdateWorkspaceProfileRequest(
    val name: String,
    val description: String?
)

data class UpdateWorkspacePictureRequest(
    val pictureUrl: String
)

data class AddMemberRequest(
    val userId: String
)

data class MembershipStatusData(
    val status: String // e.g. "member", "admin", "pending", "none"
)

data class GetWorkspaceMembersData(
    val members: List<User>
)

data class GetTagsData(
    val tags:List<String>
)

data class GetPollData(
    val changed:Boolean
)

data class PollResponse(val hasNewMessages: Boolean)

// -------------------- RETROFIT INTERFACE --------------------

interface WorkspaceInterface {

    // Get all workspaces for a user
    @GET("workspace/user")
    suspend fun getWorkspacesForUser(
        @Header("Authorization") authHeader: String
    ): Response<ApiResponse<GetWorkspacesForUserData>>

    // Get a single workspace
    @GET("workspace/{id}")
    suspend fun getWorkspace(
        @Header("Authorization") authHeader: String,
        @Path("id") workspaceId: String
    ): Response<ApiResponse<GetWorkspaceData>>

    // Get members (users) of a workspace
    @GET("workspace/{id}/members")
    suspend fun getWorkspaceMembers(
        @Header("Authorization") authHeader: String,
        @Path("id") workspaceId: String
    ): Response<ApiResponse<GetWorkspaceMembersData>>

    // Get all tags (strings) in a workspace
    @GET("workspace/{id}/tags")
    suspend fun getWorkspaceTags(
        @Header("Authorization") authHeader: String,
        @Path("id") workspaceId: String
    ): Response<ApiResponse<GetTagsData>>

    // Get membership status for a user
    @GET("workspace/{id}/membership/{userId}")
    suspend fun getMembershipStatus(
        @Header("Authorization") authHeader: String,
        @Path("id") workspaceId: String,
        @Path("userId") userId: String
    ): Response<ApiResponse<MembershipStatusData>>

    // Create a new workspace
    @POST("workspace")
    suspend fun createWorkspace(
        @Header("Authorization") authHeader: String,
        @Body body: Profile
    ): Response<ApiResponse<CreateWorkspaceData>>

    // Add member to workspace
    @POST("workspace/{id}/members")
    suspend fun addMemberToWorkspace(
        @Header("Authorization") authHeader: String,
        @Path("id") workspaceId: String,
        @Body body: AddMemberRequest
    ): Response<ApiResponse<Unit>>

    // Update workspace profile
    @PUT("workspace/{id}")
    suspend fun updateWorkspaceProfile(
        @Header("Authorization") authHeader: String,
        @Path("id") workspaceId: String,
        @Body body: UpdateWorkspaceProfileRequest
    ): Response<ApiResponse<Unit>>

    // Update workspace picture
    @PUT("workspace/{id}/picture")
    suspend fun updateWorkspacePicture(
        @Header("Authorization") authHeader: String,
        @Path("id") workspaceId: String,
        @Body body: UpdateWorkspacePictureRequest
    ): Response<ApiResponse<Unit>>

    // Ban/remove a user from workspace
    @DELETE("workspace/{id}/members/{userId}")
    suspend fun banWorkspaceMember(
        @Header("Authorization") authHeader: String,
        @Path("id") workspaceId: String,
        @Path("userId") userId: String
    ): Response<ApiResponse<Unit>>

    // Delete workspace
    @DELETE("workspace/{id}")
    suspend fun deleteWorkspace(
        @Header("Authorization") authHeader: String,
        @Path("id") workspaceId: String
    ): Response<ApiResponse<Unit>>

    //chat polling
    @GET("workspace/{id}/poll")
    suspend fun pollChat(
    @Header("Authorization") authHeader: String,
    @Path("id") workspaceId: String): Response<ApiResponse<GetPollData>>

    @GET("workspace/{id}/poll")
    suspend fun pollForNewMessages(
        @Header("Authorization") authHeader: String,
        @Path("id") workspaceId: String
    ): Response<ApiResponse<PollResponse>>
}




