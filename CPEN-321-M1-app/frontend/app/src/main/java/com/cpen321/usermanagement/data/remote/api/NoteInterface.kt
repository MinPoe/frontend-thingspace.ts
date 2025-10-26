package com.cpen321.usermanagement.data.remote.api

import com.cpen321.usermanagement.data.remote.dto.ApiResponse
import com.cpen321.usermanagement.data.remote.dto.Note
import com.cpen321.usermanagement.data.remote.dto.User
import com.cpen321.usermanagement.data.remote.dto.Workspace
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

data class FindNotesData(
    val notes: List<Note>
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

    @POST("notes")
    suspend fun createNote(
        @Header("Authorization") authHeader: String,
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): Response<Unit>

    @PUT("notes/{id}")
    suspend fun updateNote(
        @Header("Authorization") authHeader: String,
        @Path("id") noteId: String,
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): Response<Unit>

    @GET("notes")
    suspend fun findNotes(
        @Header("Authorization") authHeader: String,
        @Query("workspaceId") workspaceId: String,
        @Query("noteType") noteType: String,
        @Query("tags") tags: List<String>,
        @Query("query") query: String
    ): Response<ApiResponse<FindNotesData>>

    @DELETE("notes/{id}")
    suspend fun deleteNote(
        @Header("Authorization") authHeader: String,
        @Path("id") noteId: String
    ): Response<Unit>
}