package com.cpen321.usermanagement.data.remote.dto

data class UpdateProfileRequest(
    val profile: ProfileUpdate? = null
)

data class ProfileUpdate(
    val imagePath: String? = null,
    val name: String? = null,
    val description: String? = null
)

data class ProfileData(
    val user: User
)

data class Profile(
    val imagePath: String?,
    val name: String,
    val description: String?
)

data class User(
    val _id: String,
    val googleId: String? = null,
    val email: String,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val profile: Profile
)

data class UploadImageData(
    val image: String
)

data class UpdateFcmTokenRequest(
    val fcmToken: String
)