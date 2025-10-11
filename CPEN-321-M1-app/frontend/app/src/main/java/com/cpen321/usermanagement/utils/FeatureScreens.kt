package com.cpen321.usermanagement.utils

//context of the note-workspace-user system
data class FeatureContext(
    val noteId: String? = null,
    val userId: String? = null,
    val workspaceId: String? = null,
    val noteType: String? = null
)

interface IFeatureActions {
    fun navigateToChat(context: FeatureContext)
    fun navigateToCopy(context: FeatureContext)
    fun navigateToFields(context: FeatureContext)
    fun navigateToFilter(context: FeatureContext)
    fun navigateToInvite(context: FeatureContext)
    fun navigateToMembersManager(context: FeatureContext)
    fun navigateToMembers(context: FeatureContext)
    fun navigateToMainWithContext(context: FeatureContext)
    fun navigateToNote(context: FeatureContext)
    fun navigateToOtherProfile(context: FeatureContext)
    fun navigateToSharing(context: FeatureContext)
    fun navigateToTemplate(context: FeatureContext)
    fun navigateToWsCreation(context: FeatureContext)
    fun navigateToWsProfileManager(context: FeatureContext)
    fun navigateToWsProfile(context: FeatureContext)
}