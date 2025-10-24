package com.cpen321.usermanagement.data.remote.api

import com.cpen321.usermanagement.data.remote.dto.ApiResponse
import com.cpen321.usermanagement.data.remote.dto.Note
import retrofit2.Response
import retrofit2.http.*

data class ShareNoteRequest(
    val workspaceId: String
)

data class CopyNoteRequest(
    val workspaceId: String
)

data class GetNoteData(
    val note: Note
)

data class ShareNoteData(
    val note: Note
)

data class CopyNoteData(
    val note: Note
)

interface NoteInterface {
    @GET("notes/{id}")
    suspend fun getNote(
        @Header("Authorization") authHeader: String,
        @Path("id") noteId: String
    ): Response<ApiResponse<GetNoteData>>

    @POST("notes/{id}/share")
    suspend fun shareNoteToWorkspace(
        @Header("Authorization") authHeader: String,
        @Path("id") noteId: String,
        @Body request: ShareNoteRequest
    ): Response<ApiResponse<ShareNoteData>>

    @POST("notes/{id}/copy")
    suspend fun copyNoteToWorkspace(
        @Header("Authorization") authHeader: String,
        @Path("id") noteId: String,
        @Body request: CopyNoteRequest
    ): Response<ApiResponse<CopyNoteData>>
}