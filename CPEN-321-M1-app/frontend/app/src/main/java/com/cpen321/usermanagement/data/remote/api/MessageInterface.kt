package com.cpen321.usermanagement.data.remote.api

import com.cpen321.usermanagement.data.remote.dto.ApiResponse
import com.cpen321.usermanagement.data.remote.dto.Message
import retrofit2.Response
import retrofit2.http.*

data class SendMessageRequest(
    val content: String
)

data class DeleteMessageResponse(
    val message: String
)

data class GetMessagesData(
    val messages: List<Message>
)

data class SendMessageData(
    val message: Message
)

interface MessageInterface {

    @GET("messages/workspace/{workspaceId}")
    suspend fun getMessages(
        @Header("Authorization") authHeader: String,
        @Path("workspaceId") workspaceId: String,
        @Query("limit") limit: Int = 50,
        @Query("before") before: String? = null
    ): Response<List<Message>>

    @POST("messages/workspace/{workspaceId}")
    suspend fun sendMessage(
        @Header("Authorization") authHeader: String,
        @Path("workspaceId") workspaceId: String,
        @Body request: SendMessageRequest
    ): Response<Message>

    @DELETE("messages/{messageId}")
    suspend fun deleteMessage(
        @Header("Authorization") authHeader: String,
        @Path("messageId") messageId: String
    ): Response<DeleteMessageResponse>
}