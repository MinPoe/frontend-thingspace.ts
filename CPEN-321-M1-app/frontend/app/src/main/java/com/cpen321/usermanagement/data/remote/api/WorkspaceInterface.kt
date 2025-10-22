package com.cpen321.usermanagement.data.remote.api

import com.cpen321.usermanagement.data.remote.dto.ApiResponse
import com.cpen321.usermanagement.data.remote.dto.Profile
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Header

data class CreateWorkspaceData(
    val workspaceId: String
)

interface WorkspaceInterface {
    @POST("workspace")
    suspend fun createWorkspace(@Header("Authorization") authHeader: String,
                                @Body profile: Profile): Response<ApiResponse<CreateWorkspaceData>>

}