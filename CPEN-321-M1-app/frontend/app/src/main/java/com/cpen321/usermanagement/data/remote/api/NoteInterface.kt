package com.cpen321.usermanagement.data.remote.api

import com.cpen321.usermanagement.data.remote.dto.Field
import com.cpen321.usermanagement.data.remote.dto.Note
import com.cpen321.usermanagement.data.remote.dto.NoteType
import retrofit2.http.*
import retrofit2.Response
import com.cpen321.usermanagement.data.remote.dto.ApiResponse
import com.cpen321.usermanagement.data.remote.dto.Workspace

data class NoteResult(val note: Note)
data class SearchResult(val notes: List<Note>)
data class WorkspaceResult(val workspace: Workspace)
data class CreateNoteRequest(
    val tags: List<String>,
    val fields: List<Field>,
    val noteType: NoteType,
    val workspaceId: String
)
data class UpdateNoteRequest(
    val tags: String,
    val fields: List<Field>,
    val workspaceId: String
)

interface NoteInterface {

    // POST /notes
    @POST("notes")
    suspend fun createNote(
        @Header("Authorization") authHeader: String,
        @Body body: CreateNoteRequest
    ): Response<ApiResponse<NoteResult>>

    // PUT /notes/{id}
    @PUT("notes/{id}")
    suspend fun updateNote(
        @Header("Authorization") authHeader: String,
        @Path("id") noteId: String,
        @Body body: UpdateNoteRequest
    ): Response<ApiResponse<NoteResult>>

    // DELETE /notes/{id}
    @DELETE("notes/{id}")
    suspend fun deleteNote(
        @Header("Authorization") authHeader: String,
        @Path("id") noteId: String
    ): Response<ApiResponse<NoteResult>>

    // GET /notes/{id}
    @GET("notes/{id}")
    suspend fun getNote(
        @Header("Authorization") authHeader: String,
        @Path("id") noteId: String
    ): Response<ApiResponse<NoteResult>>

    // GET /notes
    @GET("notes")
    suspend fun findNotes(
        @Header("Authorization") authHeader: String,
        @Query("search") search: String? = null // if backend supports query filters
    ): Response<ApiResponse<SearchResult>>

    // GET /notes/{id}/workspaces
    @GET("notes/{id}/workspaces")
    suspend fun getWorkspacesForNote(
        @Header("Authorization") authHeader: String,
        @Path("id") noteId: String
    ): Response<ApiResponse<WorkspaceResult>> // Replace with actual Workspace DTO if needed

    // POST /notes/{id}/share
    @POST("notes/{id}/share")
    suspend fun shareNoteToWorkspace(
        @Header("Authorization") authHeader: String,
        @Path("id") noteId: String,
        @Query("workspaceId") workspaceId: String // or use @Body if backend expects a body
    ): Response<ApiResponse<NoteResult>>
}
