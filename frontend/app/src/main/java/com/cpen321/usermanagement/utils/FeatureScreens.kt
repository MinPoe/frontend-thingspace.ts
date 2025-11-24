package com.cpen321.usermanagement.utils
import com.cpen321.usermanagement.data.remote.dto.NoteType

interface IWorkspaceActions{
    fun navigateToInvite()
    fun navigateToMembersManager()
    fun navigateToMembers()
    fun navigateToWsCreation()
    fun navigateToWsProfileManager(workspaceId: String)
    fun navigateToWsProfile(workspaceId: String)
    fun navigateToWsSelect()
}

interface IStateWrapper{
    fun getWorkspaceId(): String
    fun getOtherUserId(): String
    fun getNoteType(): NoteType
    fun getNoteId(): String
    fun getSelectedTags(): List<String>
    fun getAllTagsSelected(): Boolean
    fun getSearchQuery(): String
    fun setSearchQuery(query: String): Unit
    fun updateTagSelection(selectedTags:List<String>, allTagsSelected: Boolean): Unit
}

interface INavigationActions {
    // --- Navigation methods ---

    fun navigateToChat(
        workspaceId: String
    )

    fun navigateToCopy()

    fun navigateToFields()

    fun navigateToFilter(
        workspaceId: String,
        selectedTags: List<String>,
        allTagsSelected: Boolean
    )

    fun navigateToNoteCreation(noteType: NoteType = NoteType.CONTENT, noteId: String? = null)
    fun navigateToNoteEdit(noteId: String)

    fun navigateToOtherProfile(otherUserId: String)

    fun navigateToSharing()

    fun navigateToTemplate(
        workspaceId: String,
        selectedTags: List<String> = emptyList(),
        allTagsSelected: Boolean = true,
        searchQuery: String = ""
    )



    fun navigateToMainWithContext(
        workspaceId: String,
        selectedTags: List<String> = emptyList(),
        allTagsSelected: Boolean = true,
        searchQuery: String = ""
    )
    fun navigateToMainTagReset(workspaceId: String)
    fun navigateToTemplateTagReset(workspaceId: String)
}

class FeatureActions(
    val state: IStateWrapper,
    val ws: IWorkspaceActions,
    val navs: INavigationActions
)