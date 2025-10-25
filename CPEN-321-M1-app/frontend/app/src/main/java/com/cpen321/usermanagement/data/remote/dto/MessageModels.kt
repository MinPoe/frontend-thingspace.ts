package com.cpen321.usermanagement.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Message(
    @SerializedName("_id")
    val id: String,

    @SerializedName("workspaceId")
    val workspaceId: String,

    @SerializedName("authorId")
    val authorId: String,

    @SerializedName("content")
    val content: String,

    @SerializedName("createdAt")
    val createdAt: Date,

    @SerializedName("updatedAt")
    val updatedAt: Date
)