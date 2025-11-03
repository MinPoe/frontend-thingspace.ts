package com.cpen321.usermanagement

import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import com.cpen321.usermanagement.data.local.preferences.TokenManager
import com.cpen321.usermanagement.data.remote.api.UserInterface
import com.cpen321.usermanagement.data.repository.AuthRepository
import com.cpen321.usermanagement.data.repository.ProfileRepository
import com.cpen321.usermanagement.data.repository.ProfileRepositoryImpl
import com.cpen321.usermanagement.ui.viewmodels.ProfileViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.lang.Thread.sleep
import javax.inject.Inject

/*
* Make sure you have the google account that will carry the tests signed up in your emulator!!!
* */


@HiltAndroidTest
class TestCollaborate {

    companion object{
        const val ACCT_NAME="Friedrich van Aukstin"
        const val ACCT_WS="Friedrich van Aukstin's Personal Workspace"
        const val MEMBER_ACCT_NAME="Marricc Ammerk"
        const val MEMBER_ACCT_GMAIL="marricc7@gmail.com"
        const val MEMBER_ACCT_WS = "Marricc Ammerk's Personal Workspace"

        //Error/Success messages
        const val saveConfirmString = "Profile updated successfully!"
        const val failedCrWsString = "Failed to create workspace."
        const val invalidEmailString = "Could not retrieve the profile matching the given email!"
        const val addedAMemberString = "The user got added to the workspace."
        const val alreadyAMemberString = "The user is already a member!"

        //test workspace names and descriptions, etc.
        const val testWsName = "Test"
        const val studyWsName = "Studies"
        const val v2Name = "Studies v2"
        const val v2Bio = "Study group"
        const val invalidEmailSample = "invalidemail"
        const val chatMessage = "Hello team!"
    }

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun waitForVm(millis: Long){
        composeRule.waitForIdle()
        sleep(millis)
        composeRule.waitForIdle()
    }

    @Test
    fun testCollaborate() {
        hiltRule.inject()
        composeRule.activity.setContent {
            UserManagementApp()
        }

        //Ui texts
        val signInString = composeRule.activity.getString(R.string.sign_in_with_google)
        val createWsString = composeRule.activity.getString(R.string.create_new_workspace)
        val pickWsNameString = composeRule.activity.getString(R.string.pick_workspace_name)
        val crWsButtonString = composeRule.activity.getString(R.string.create_workspace)
        val manageWsPrString = composeRule.activity.getString(R.string.manage_workspace_profile)
        val wsIcString = composeRule.activity.getString(R.string.workspaces)
        val trashIcString = composeRule.activity.getString(R.string.delete)
        val wsDescriptionString = composeRule.activity.getString(R.string.bio)
        val saveButtonString = composeRule.activity.getString(R.string.save)
        val wsInviteString = composeRule.activity.getString(R.string.invite)
        val emailBoxString = composeRule.activity.getString(R.string.enter_invite_email)
        val wsInviteButtonString = composeRule.activity.getString(R.string.invite_to_workspace)
        val backIcString = composeRule.activity.getString(R.string.back_icon_description)
        val chatIcString = composeRule.activity.getString(R.string.chat)
        val chatTextBoxString = composeRule.activity.getString(R.string.type_message)
        val noMessagesString = composeRule.activity.getString(R.string.no_messages_yet)
        val profileIcString = composeRule.activity.getString(R.string.profile)
        val signOutString = composeRule.activity.getString(R.string.sign_out)
        val editWsIcString =  composeRule.activity.getString(R.string.edit)
        val leaveIcString = composeRule.activity.getString(R.string.leave)
        val membersIcString = composeRule.activity.getString(R.string.members)
        val banIcString = composeRule.activity.getString(R.string.ban)

        uiAutomator {
            onElement { textAsString() == "Allow" }.click()
        }
        composeRule.onNodeWithText(signInString).performClick()
        uiAutomator {
            onElement { textAsString() == ACCT_NAME }.click()
            sleep(5000)
        }
        composeRule.waitForIdle()

        Log.d("TEST COLLABORATE","Workspace Creation Tests")
        composeRule.onNodeWithContentDescription(wsIcString).performClick()
        waitForVm(5000)
        composeRule.onNodeWithText(createWsString).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(text = crWsButtonString).assertIsNotEnabled()
        Log.d("TEST COLLABORATE","Duplicate Name Workspace Creation")
        composeRule.onNodeWithText(pickWsNameString).performTextInput(testWsName)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(text = crWsButtonString).performClick()
        waitForVm(5000)
        composeRule.onNodeWithText(text = failedCrWsString).assertIsDisplayed()
        Log.d("TEST COLLABORATE","Successful Workspace Creation")
        composeRule.onNodeWithText(pickWsNameString).performTextReplacement(studyWsName)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(text = crWsButtonString).performClick()
        waitForVm(5000)
        composeRule.onNodeWithText(text = manageWsPrString).assertIsDisplayed()
        waitForVm(5000)
        composeRule.onNodeWithText(text = studyWsName).assertIsDisplayed()

        Log.d("TEST COLLABORATE","Update Workspace Tests")
        composeRule.onNodeWithText(text = studyWsName).performTextReplacement(v2Name)
        composeRule.onNodeWithText(text = wsDescriptionString).performTextInput(v2Bio)
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.pressBack() //hiding the keyboard so that the success message is unobstructed
        composeRule.onNodeWithText(text = saveButtonString).performClick()
        waitForVm(5000)
        composeRule.onNodeWithText(text = saveConfirmString).assertIsDisplayed()

        Log.d("TEST COLLABORATE","Workspace Invite Tests")
        composeRule.onNodeWithContentDescription(wsInviteString).performClick()
        waitForVm(5000)
        Log.d("TEST COLLABORATE","1) Inviting an invalid email")
        composeRule.onNodeWithText(emailBoxString).performTextInput(invalidEmailSample)
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack() //hiding the keyboard so that the success message is unobstructed
        composeRule.onNodeWithText(wsInviteButtonString).performClick()
        waitForVm(2000)
        composeRule.onNodeWithText(invalidEmailString).assertIsDisplayed()
        Log.d("TEST COLLABORATE","2) A valid invitation")
        composeRule.onNodeWithText(emailBoxString).performTextReplacement(MEMBER_ACCT_GMAIL)
        composeRule.onNodeWithText(wsInviteButtonString).performClick()
        waitForVm(2000)
        composeRule.onNodeWithText(addedAMemberString).assertIsDisplayed()
        Log.d("TEST COLLABORATE","3) Inviting already a member")
        composeRule.waitUntil(20000){
            composeRule
                .onAllNodesWithText(addedAMemberString)
                .fetchSemanticsNodes()
                .isEmpty()
        }
        composeRule.onNodeWithText(wsInviteButtonString).performClick()
        waitForVm(5000)
        composeRule.onNodeWithText(alreadyAMemberString).assertIsDisplayed()

        Log.d("TEST COLLABORATE","Moving to chat screen")
        composeRule.onNodeWithContentDescription(backIcString).performClick()
        waitForVm(5000)
        composeRule.onNodeWithContentDescription(backIcString).performClick()
        waitForVm(2000)
        composeRule.onNodeWithContentDescription(chatIcString+v2Name).performClick()
        waitForVm(5000)

        Log.d("TEST COLLABORATE","Chat tests")
        Log.d("TEST COLLABORATE","Sending a non-empty chat message")
        composeRule.onNodeWithContentDescription(chatTextBoxString).performTextInput(" ")
        composeRule.onNodeWithContentDescription(chatIcString).performClick()
        waitForVm(5000)
        composeRule.onNodeWithText(noMessagesString).assertIsDisplayed()
        Log.d("TEST COLLABORATE","Sending a non-empty chat message")
        composeRule.onNodeWithContentDescription(chatTextBoxString).performTextInput(chatMessage)
        composeRule.onNodeWithContentDescription(chatIcString).performClick()
        waitForVm(10000) //to give time for the message to arrive
        composeRule.onNodeWithText(noMessagesString).assertIsNotDisplayed()
        composeRule.onNodeWithText(chatMessage).assertIsDisplayed()

        Log.d("TEST COLLABORATE","Signing out and signing in as a regular Workspace Member")
        composeRule.onNodeWithContentDescription(profileIcString).performClick()
        waitForVm(5000)
        composeRule.onNodeWithText(signOutString).performClick()
        waitForVm(5000)
        composeRule.onNodeWithText(signInString).performClick()
        uiAutomator {
            onElement { textAsString() == MEMBER_ACCT_NAME }.click()
            sleep(5000)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(wsIcString).performClick()
        waitForVm(5000)

        Log.d("TEST COLLABORATE","Enter the Workspace and see profile blurred out")
        composeRule.onNodeWithContentDescription(editWsIcString+v2Name).performClick()
        waitForVm(5000)
        composeRule.onNodeWithText(wsDescriptionString).assertIsNotEnabled()
        composeRule.onNodeWithText(v2Name).assertIsNotEnabled()

        Log.d("TEST COLLABORATE","Test Leave Workspace")
        composeRule.onNodeWithContentDescription(leaveIcString).performClick()
        waitForVm(5000)
        composeRule.onNodeWithText(v2Name).assertIsNotDisplayed()

        Log.d("TEST COLLABORATE","Re-signing in as admin")
        composeRule.onNodeWithContentDescription(editWsIcString+MEMBER_ACCT_WS).performClick()
        waitForVm(5000)
        composeRule.onNodeWithText(signOutString).performClick()
        waitForVm(5000)
        composeRule.onNodeWithText(signInString).performClick()
        uiAutomator {
            onElement { textAsString() == ACCT_NAME }.click()
            sleep(5000)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(wsIcString).performClick()
        waitForVm(5000)
        composeRule.onNodeWithContentDescription(editWsIcString+v2Name).performClick()
        waitForVm(5000)
        composeRule.onNodeWithText(wsDescriptionString).assertIsEnabled()

        Log.d("TEST COLLABORATE","Inviting the member again to ban them")
        composeRule.onNodeWithContentDescription(wsInviteString).performClick()
        waitForVm(5000)
        composeRule.onNodeWithText(emailBoxString).performTextReplacement(MEMBER_ACCT_GMAIL)
        composeRule.onNodeWithText(wsInviteButtonString).performClick()
        waitForVm(2000)
        composeRule.onNodeWithText(addedAMemberString).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(backIcString).performClick()
        waitForVm(2000)

        Log.d("TEST COLLABORATE","Testing Ban")
        composeRule.onNodeWithContentDescription(membersIcString).performClick()
        waitForVm(5000)
        composeRule.onNodeWithContentDescription(banIcString).performClick()
        waitForVm(5000)
        composeRule.onNodeWithText(MEMBER_ACCT_NAME).assertIsNotDisplayed()

        Log.d("TEST COLLABORATE","Sign In as the banned member to confirm that you are out of the workspace")
        composeRule.onNodeWithContentDescription(backIcString).performClick()
        waitForVm(2000)
        composeRule.onNodeWithContentDescription(backIcString).performClick()
        waitForVm(5000)
        composeRule.onNodeWithContentDescription(editWsIcString+ACCT_WS).performClick()
        waitForVm(5000)
        composeRule.onNodeWithText(signOutString).performClick()
        waitForVm(5000)
        composeRule.onNodeWithText(signInString).performClick()
        uiAutomator {
            onElement { textAsString() == MEMBER_ACCT_NAME }.click()
            sleep(5000)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(wsIcString).performClick()
        waitForVm(5000)
        composeRule.onNodeWithText(v2Name).assertIsNotDisplayed()

        Log.d("TEST COLLABORATE","Final sign in as an admin - to delete the workspace")
        composeRule.onNodeWithContentDescription(editWsIcString+MEMBER_ACCT_WS).performClick()
        waitForVm(5000)
        composeRule.onNodeWithText(signOutString).performClick()
        waitForVm(5000)
        composeRule.onNodeWithText(signInString).performClick()
        uiAutomator {
            onElement { textAsString() == ACCT_NAME }.click()
            sleep(5000)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(wsIcString).performClick()
        waitForVm(5000)
        composeRule.onNodeWithContentDescription(editWsIcString+v2Name).performClick()
        waitForVm(5000)
        composeRule.onNodeWithText(wsDescriptionString).assertIsEnabled()

        Log.d("TEST COLLABORATE","Delete Test")
        //Also makes test re-runnable as the ws to be created is removed to be re-created next time
        composeRule.onNodeWithContentDescription(trashIcString).performClick()
        waitForVm(5000)
        composeRule.onNodeWithText(text = createWsString).assertIsDisplayed()
        composeRule.onNodeWithText(text = studyWsName).assertIsNotDisplayed()
    }
}


