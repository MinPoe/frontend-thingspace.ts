package com.cpen321.usermanagement.data.remote.api

import com.cpen321.usermanagement.data.remote.dto.ApiResponse
import com.cpen321.usermanagement.data.remote.dto.Profile
import com.cpen321.usermanagement.data.remote.dto.Workspace
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Header

data class CreateWorkspaceData(
    val workspaceId: String
)

data class GetWorkspacesForUserData(
    val workspaces: List<Workspace>
)

interface WorkspaceInterface {
    @POST("workspace")
    suspend fun createWorkspace(@Header("Authorization") authHeader: String,
                                @Body profile: Profile): Response<ApiResponse<CreateWorkspaceData>>

    @GET("workspace/user")
    suspend fun getWorkspacesForUser(@Header("Authorization") authHeader: String):
            Response<ApiResponse<GetWorkspacesForUserData>>
}


