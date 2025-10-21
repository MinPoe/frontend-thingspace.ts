package com.cpen321.usermanagement.data.remote.dto

data class UpdateProfileRequest(
    val name: String? = null,
    val bio: String? = null,
    val profilePicture: String? = null
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
    val email: String,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val profile: Profile
)

data class UploadImageData(
    val image: String
)