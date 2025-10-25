package com.cpen321.usermanagement.data.repository

import com.cpen321.usermanagement.data.remote.dto.Message
import java.util.Date

interface MessageRepository {
    suspend fun getMessages(
        workspaceId: String,
        limit: Int = 50,
        before: Date? = null
    ): Result<List<Message>>

    suspend fun sendMessage(
        workspaceId: String,
        content: String
    ): Result<Message>

    suspend fun deleteMessage(messageId: String): Result<Unit>
}