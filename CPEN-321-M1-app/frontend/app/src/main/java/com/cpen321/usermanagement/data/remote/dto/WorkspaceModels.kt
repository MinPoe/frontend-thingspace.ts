package com.cpen321.usermanagement.data.remote.dto

/*
 * Workspace data class
 */

/*
 * Workspace Data Class:
 *     workspacePicture - nullable (can be empty)
 *     workSpaceDescription - nullable (can be empty)
 */
data class Workspace(
    val _id: String,
    val workspaceName: String,
    val workspacePicture: String? = null,
    val workspaceDescription: String? = null
)

