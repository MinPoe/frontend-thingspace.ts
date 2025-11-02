package com.cpen321.usermanagement

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
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
        const val ACCT_GMAIL="vanaukstinfriedrich@gmail.com"
        const val MEMBER_ACCT_NAME="Marricc Ammerk"
        const val MEMBER_ACCT_GMAIL="marricc7@gmail.com"

        //Error/Success messages
        const val saveConfirmString = "Profile updated successfully!"
        const val failedCrWsString = "Failed to create workspace."
        const val invalidEmailString = "Could not retrieve the profile matching the given email!"
        const val addedAMemberString = "The user got added to the workspace."
        const val alreadyAMemberString = "The user is already a member!"

        //test workspace names and descriptions
        const val testWsName = "Test"
        const val studyWsName = "Studies"
        const val v2Name = "Studies v2"
        const val v2Bio = "Study group"
        const val invalidEmailSample = "invalidemail"


    }


    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun showsWelcomeAfterContinue() {
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

        uiAutomator {
            onElement { textAsString() == "Allow" }.click()
        }
        composeRule.onNodeWithText(signInString).performClick()
        uiAutomator {
            onElement { textAsString() == ACCT_NAME }.click()
            sleep(5000)
        }
        composeRule.waitForIdle()

        //Workspace Creation Tests
        composeRule.onNodeWithContentDescription(wsIcString).performClick()
        composeRule.waitForIdle()
        sleep(5000)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(createWsString).performClick()
        //sleep(5000)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(text = crWsButtonString).assertIsNotEnabled()
        //1) Duplicate name workspace creation
        composeRule.onNodeWithText(pickWsNameString).performTextInput(testWsName)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(text = crWsButtonString).performClick()
        composeRule.waitForIdle()
        uiAutomator { sleep(5000) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(text = failedCrWsString).assertIsDisplayed()
        //2) Successful Workspace Creation
        composeRule.onNodeWithText(pickWsNameString).performTextReplacement(studyWsName)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(text = crWsButtonString).performClick()
        composeRule.waitForIdle()
        uiAutomator { sleep(5000) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(text = manageWsPrString).assertIsDisplayed()
        composeRule.waitForIdle()
        uiAutomator { sleep(5000) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(text = studyWsName).assertIsDisplayed()

        //Testing Update Workspace
        composeRule.onNodeWithText(text = studyWsName).performTextReplacement(v2Name)
        composeRule.onNodeWithText(text = wsDescriptionString).performTextInput(v2Bio)
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.pressBack() //hiding the keyboard so that the success message is unobstructed
        composeRule.onNodeWithText(text = saveButtonString).performClick()
        composeRule.waitForIdle()
        uiAutomator { sleep(5000) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(text = saveConfirmString).assertIsDisplayed()

        //Testing invite to workspace
        composeRule.onNodeWithContentDescription(wsInviteString).performClick()
        composeRule.waitForIdle()
        uiAutomator { sleep(5000) }
        composeRule.waitForIdle()
        //1) Invalid email
        composeRule.onNodeWithText(emailBoxString).performTextInput(invalidEmailSample)
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack() //hiding the keyboard so that the success message is unobstructed
        composeRule.onNodeWithText(wsInviteButtonString).performClick()
        composeRule.waitForIdle()
        uiAutomator { sleep(2000) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(invalidEmailString).assertIsDisplayed()
        //2) Valid invitation
        composeRule.onNodeWithText(emailBoxString).performTextReplacement(MEMBER_ACCT_GMAIL)
        composeRule.onNodeWithText(wsInviteButtonString).performClick()
        composeRule.waitForIdle()
        uiAutomator { sleep(2000) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(addedAMemberString).assertIsDisplayed()
        //3) Inviting already a member
        composeRule.waitUntil(20000){
            composeRule
                .onAllNodesWithText(addedAMemberString)
                .fetchSemanticsNodes()
                .isEmpty()
        }
        composeRule.onNodeWithText(wsInviteButtonString).performClick()
        composeRule.waitForIdle()
        uiAutomator { sleep(2000) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(alreadyAMemberString).assertIsDisplayed()

        //going back to workspace profile screen to execute other tests
        composeRule.onNodeWithContentDescription(backIcString).performClick()
        composeRule.waitForIdle()
        uiAutomator { sleep(2000) }
        composeRule.waitForIdle()

        //Testing Delete
        //Also makes test re-runnable as the ws to be created is removed to be re-created next time
        composeRule.onNodeWithContentDescription(trashIcString).performClick()
        composeRule.waitForIdle()
        uiAutomator { sleep(5000) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(text = createWsString).assertIsDisplayed()
        composeRule.onNodeWithText(text = studyWsName).assertIsNotDisplayed()
    }
}


