package com.cpen321.usermanagement.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cpen321.usermanagement.data.local.preferences.TokenManager
import com.cpen321.usermanagement.data.remote.api.ImageInterface
import com.cpen321.usermanagement.data.remote.api.RetrofitClient
import com.cpen321.usermanagement.data.remote.api.UserInterface
import com.cpen321.usermanagement.data.remote.dto.UpdateFcmTokenRequest
import com.cpen321.usermanagement.data.remote.dto.UpdateProfileRequest
import com.cpen321.usermanagement.data.remote.dto.User
import com.cpen321.usermanagement.utils.JsonUtils.parseErrorMessage
import com.cpen321.usermanagement.utils.MediaUtils.uriToFile
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import javax.inject.Inject
import javax.inject.Singleton
import com.cpen321.usermanagement.data.remote.dto.ProfileUpdate
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userInterface: UserInterface,
    private val tokenManager: TokenManager
) : ProfileRepository {

    companion object {
        private const val TAG = "ProfileRepositoryImpl"
    }

    override suspend fun getProfile(): Result<User> {
        return try {
            val response = userInterface.getProfile("") // Auth header is handled by interceptor
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.user)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage =
                    parseErrorMessage(errorBodyString, "Failed to fetch user information.")
                Log.e(TAG, "Failed to get profile: $errorMessage")
                tokenManager.clearToken()
                RetrofitClient.setAuthToken(null)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: SocketTimeoutException) { return handleException("getProfile", e) }
        catch (e: UnknownHostException) { return handleException("getProfile", e) }
        catch (e: IOException) { return handleException("getProfile", e) }
        catch (e: HttpException) { return handleException("getProfile", e) }
    }

    override suspend fun updateProfile(name: String, bio: String): Result<User> {
        return try {
            val updateRequest = UpdateProfileRequest(
                profile = ProfileUpdate(name = name, description = bio)
            )
            val response = userInterface.updateProfile("", updateRequest) // Auth header is handled by interceptor
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.user)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBodyString, "Failed to update profile.")
                Log.e(TAG, "Failed to update profile: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: SocketTimeoutException) { return handleException("updateProfile", e) }
        catch (e: UnknownHostException) { return handleException("updateProfile", e) }
        catch (e: IOException) { return handleException("updateProfile", e) }
        catch (e: HttpException) { return handleException("updateProfile", e) }
    }

    override suspend fun updatePhoto(profilePicture:String): Result<User> {
        return try {
            val updateRequest = UpdateProfileRequest(
                profile = ProfileUpdate(imagePath = profilePicture)
            )
            val response = userInterface.updateProfile(
                "", // Auth header is handled by interceptor
                updateRequest
            )
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.user)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBodyString, "Failed to update the profile picture.")
                Log.e(TAG, "Failed to update the profile picture: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: SocketTimeoutException) { return handleException("updatePhoto", e) }
        catch (e: UnknownHostException) { return handleException("updatePhoto", e) }
        catch (e: IOException) { return handleException("updatePhoto", e) }
        catch (e: HttpException) { return handleException("updatePhoto", e) }
    }

    override suspend fun getOtherProfile(userId: String): Result<User> {
        return try {
            val response = userInterface.getProfileById("", userId) // Auth header is handled by interceptor
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.user)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage =
                    parseErrorMessage(errorBodyString, "Failed to fetch user information.")
                Log.e(TAG, "Failed to get profile: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: SocketTimeoutException) { return handleException("getOtherProfile", e) }
        catch (e: UnknownHostException) { return handleException("getOtherProfile", e) }
        catch (e: IOException) { return handleException("getOtherProfile", e) }
        catch (e: HttpException) { return handleException("getOtherProfile", e) }
    }

    override suspend fun getProfileByEmail(email: String): Result<User> {
        return try {
            val response = userInterface.getProfileByEmail("", email) // Auth header is handled by interceptor
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.user)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage =
                    parseErrorMessage(errorBodyString, "Failed to fetch user information.")
                Log.e(TAG, "Failed to get profile: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: SocketTimeoutException) { return handleException("getProfileByEmail", e) }
        catch (e: UnknownHostException) { return handleException("getProfileByEmail", e) }
        catch (e: IOException) { return handleException("getProfileByEmail", e) }
        catch (e: HttpException) { return handleException("getProfileByEmail", e) }
    }

    override suspend fun updateFcmToken(fcmToken: String): Result<User> {
        return try {
            val updateRequest = UpdateFcmTokenRequest(fcmToken = fcmToken)
            val response = userInterface.updateFcmToken("", updateRequest) // Auth header is handled by interceptor
            if (response.isSuccessful && response.body()?.data != null) {
                Log.d(TAG, "FCM token updated successfully")
                Result.success(response.body()!!.data!!.user)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBodyString, "Failed to update FCM token.")
                Log.e(TAG, "Failed to update FCM token: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: SocketTimeoutException) { return handleException("updateFcmToken", e) }
        catch (e: UnknownHostException) { return handleException("updateFcmToken", e) }
        catch (e: IOException) { return handleException("updateFcmToken", e) }
        catch (e: HttpException) { return handleException("updateFcmToken", e) }
    }

    override suspend fun getCurrentUserId(): String {
        val profileResult = getProfile()
        return if (profileResult.isSuccess) {
            profileResult.getOrNull()?._id ?: ""
        } else {
            ""
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
