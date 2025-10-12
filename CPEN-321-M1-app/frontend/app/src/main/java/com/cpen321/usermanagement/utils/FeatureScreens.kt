package com.cpen321.usermanagement.utils

interface IFeatureActions {
    // --- Common navigation state getters ---
    fun getWorkspaceId(): String
    fun getOtherUserId(): String
    fun getNoteType(): NoteType
    fun getNoteId(): String
    fun getSelectedTags(): List<String>
    fun getAllTagsSelected(): Boolean
    fun getSearchQuery(): String

    // --- Navigation methods ---

    fun navigateToChat(
        workspaceId: String,
        selectedTags: List<String> = emptyList(),
        allTagsSelected: Boolean = true,
        searchQuery: String = ""
    )

    fun navigateToCopy()

    fun navigateToFields()

    fun navigateToFilter(
        workspaceId: String,
        selectedTags: List<String>,
        allTagsSelected: Boolean
    )

    fun navigateToInvite()

    fun navigateToMembersManager()

    fun navigateToMembers()

    fun navigateToNote(noteId: String)

    fun navigateToOtherProfile(otherUserId: String)

    fun navigateToSharing()

    fun navigateToTemplate(
        workspaceId: String,
        selectedTags: List<String> = emptyList(),
        allTagsSelected: Boolean = true,
        searchQuery: String = ""
    )

    fun navigateToWsCreation()

    fun navigateToWsProfileManager(workspaceId: String)

    fun navigateToWsProfile(workspaceId: String)

    fun navigateToMainWithContext(
        workspaceId: String,
        selectedTags: List<String> = emptyList(),
        allTagsSelected: Boolean = true,
        searchQuery: String = ""
    )

    fun navigateToWsSelect()
}

enum class NoteType{
    TEMPLATE, CONTENT, CHAT
}