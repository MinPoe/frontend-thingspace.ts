package com.cpen321.usermanagement.data.repository

import android.util.Log
import com.cpen321.usermanagement.data.remote.api.MessageInterface
import com.cpen321.usermanagement.data.remote.api.SendMessageRequest
import com.cpen321.usermanagement.data.remote.dto.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.HttpException

@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val messageInterface: MessageInterface
) : MessageRepository {

    companion object {
        private const val TAG = "MessageRepositoryImpl"
    }

    override suspend fun getMessages(
        workspaceId: String,
        limit: Int,
        before: Date?
    ): Result<List<Message>> {
        return try {
            val beforeString = before?.let { formatDate(it) }
            val response = messageInterface.getMessages("", workspaceId, limit, beforeString)

            if (response.isSuccessful) {
                val messages = response.body() ?: emptyList()
                Result.success(messages)
            } else {
                val errorMsg = "Failed to fetch messages: ${response.code()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: SocketTimeoutException) { return handleException("getMessages", e) }
        catch (e: UnknownHostException) { return handleException("getMessages", e) }
        catch (e: IOException) { return handleException("getMessages", e) }
        catch (e: HttpException) { return handleException("getMessages", e) }
    }

    override suspend fun sendMessage(
        workspaceId: String,
        content: String
    ): Result<Message> {
        return try {
            val request = SendMessageRequest(content)
            val response = messageInterface.sendMessage("", workspaceId, request)

            if (response.isSuccessful) {
                val message = response.body()
                if (message != null) {
                    Result.success(message)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMsg = "Failed to send message: ${response.code()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: SocketTimeoutException) { return handleException("sendMessage", e) }
        catch (e: UnknownHostException) { return handleException("sendMessage", e) }
        catch (e: IOException) { return handleException("sendMessage", e) }
        catch (e: HttpException) { return handleException("sendMessage", e) }
    }

    override suspend fun deleteMessage(messageId: String): Result<Unit> {
        return try {
            val response = messageInterface.deleteMessage("", messageId)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = "Failed to delete message: ${response.code()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: SocketTimeoutException) { return handleException("deleteMessage", e) }
        catch (e: UnknownHostException) { return handleException("deleteMessage", e) }
        catch (e: IOException) { return handleException("deleteMessage", e) }
        catch (e: HttpException) { return handleException("deleteMessage", e) }
    }

    private fun formatDate(date: Date): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(date)
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