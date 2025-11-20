package com.cpen321.usermanagement

import android.util.Log
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import java.lang.Thread.sleep
import androidx.compose.ui.test.performClick

/*
* Please log in to the below account on your emulator:
* thing4g@gmail.com
* Passwords as in attachments.
* Fill in the bio's of the account (with anything, just not empty).
*
* To run the test, make sure that you are signed out of the application.
*
* It might happen the UI Automator picks the wrong button on sign in. In this case:
* 1) Invalidate cache
* 2) run the app regularly, sign in to any account
* 3) run the test again (this time should work)
* */
@HiltAndroidTest
class TestNotes {

    companion object {
        const val ACCT_NAME:String = "Thing4G"      
        // Pre-existing workspaces (must exist before test)
        const val WORKSPACE_1 = "Workspace1"
        const val WORKSPACE_2 = "Workspace2"

        // Error/Success messages
        const val emptyLabelErrorString = "All fields must have a label"

        // Test data
        const val testTag = "important"
        const val testFieldLabel = "Notes"
        const val testFieldContent = "Test content for note"
        const val updatedTag = "updated"
        const val updatedContent = "Updated content"
    }

    

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun waitForVm(millis: Long) {
        composeRule.waitForIdle()
        sleep(millis)
        composeRule.waitForIdle()
    }



    private fun signIn(signInString: String, acctName: String) {
        composeRule.waitForIdle()
        composeRule.onNodeWithText(signInString).performClick()
        waitForVm(1000)
        uiAutomator {
            Log.d("TEST_SIGN_IN", "Before Sign in user selected")
            onElement { textAsString() == acctName }.click()
            Log.d("TEST_SIGN_IN", "Sign in user selected")
        }
        composeRule.waitForIdle()
    }

    @Test
    fun testNotes() {
        hiltRule.inject()
        val signInString = composeRule.activity.getString(R.string.sign_in_with_google)

        waitForVm(3000)

        uiAutomator { onElement { textAsString()=="Allow" }.click() }
        waitForVm(3000)

        try {
            if (composeRule.onAllNodesWithText(signInString).fetchSemanticsNodes().isNotEmpty()) {
                Log.d("TEST NOTES", "Signing in...")
                signIn(signInString, ACCT_NAME)
                waitForVm(3000)
            }
        } catch (e: Exception) {
            Log.d("TEST NOTES", "Already signed in")
        }



        // UI texts
        val wsIcString = composeRule.activity.getString(R.string.workspaces)
        val contentString = composeRule.activity.getString(R.string.content)
        val createString = composeRule.activity.getString(R.string.create)
        val addTagString = composeRule.activity.getString(R.string.add_tag)
        val enterTagString = composeRule.activity.getString(R.string.enter_tag_name)
        val addString = composeRule.activity.getString(R.string.add)
        val addFieldString = composeRule.activity.getString(R.string.add_field)
        val labelString = composeRule.activity.getString(R.string.label)
        val textContentString = composeRule.activity.getString(R.string.text_content)
        val createNoteString = composeRule.activity.getString(R.string.create)
        val fieldLabelString = composeRule.activity.getString(R.string.label)
        val saveString = composeRule.activity.getString(R.string.save)
        val shareString = composeRule.activity.getString(R.string.share)
        val shareNoteString = composeRule.activity.getString(R.string.share_note)
        val copyString = composeRule.activity.getString(R.string.copy)
        val copyNoteString = composeRule.activity.getString(R.string.copy_note)
        val deleteString = composeRule.activity.getString(R.string.delete)
        val deleteNoteString = composeRule.activity.getString(R.string.delete_note)
        val backString = composeRule.activity.getString(R.string.back_icon_description)

        Log.d("TEST NOTES", "Setting up workspaces")
        val createWsString = composeRule.activity.getString(R.string.create_new_workspace)
        val pickWsNameString = composeRule.activity.getString(R.string.pick_workspace_name)
        val createWsButtonString = composeRule.activity.getString(R.string.create_workspace)

        waitForVm(5000)

        composeRule.onNodeWithContentDescription(wsIcString).performClick()
        waitForVm(5000)

        // Create Workspace1 if doesn't exist
        if (composeRule.onAllNodesWithText(WORKSPACE_1).fetchSemanticsNodes().isEmpty()) {
            composeRule.onNodeWithText(createWsString).performClick()
            waitForVm(2000)
            composeRule.onNodeWithText(pickWsNameString).performTextInput(WORKSPACE_1)
            composeRule.onNodeWithText(createWsButtonString).performClick()
            waitForVm(2000)
            composeRule.onNodeWithContentDescription(backString).performClick()
            waitForVm(2000)
        }

        // Create Workspace2 if doesn't exist
        if (composeRule.onAllNodesWithText(WORKSPACE_2).fetchSemanticsNodes().isEmpty()) {
            composeRule.onNodeWithText(createWsString).performClick()
            waitForVm(2000)
            composeRule.onNodeWithText(pickWsNameString).performTextInput(WORKSPACE_2)
            composeRule.onNodeWithText(createWsButtonString).performClick()
            waitForVm(2000)
            composeRule.onNodeWithContentDescription(backString).performClick()
            waitForVm(3000)
        }

        Log.d("TEST NOTES", "Navigate to workspace")
        // Click the Notes/Content icon for Workspace1
        composeRule.onNodeWithContentDescription(contentString + WORKSPACE_1).performClick()
        waitForVm(2000)

        Log.d("TEST NOTES", "Create Note - No fields error test")
        // Click the pencil icon to create note
        composeRule.onNodeWithContentDescription(createString).performClick()
        waitForVm(2000)

        Log.d("TEST NOTES", "Create Note - Empty label error test")
        // Click Add Field button
        composeRule.onNodeWithText(addFieldString).performClick()
        waitForVm(2000)

        // Select TEXT field type from dialog
        composeRule.onNodeWithText("TEXT").performClick()
        waitForVm(1000)

        // Clear the default label "New Text Field"
        composeRule.onNodeWithText(fieldLabelString).performTextClearance()
        waitForVm(500)

        composeRule.onNodeWithText(createNoteString).performClick()
        waitForVm(2000)
        composeRule.onNodeWithText(emptyLabelErrorString).assertIsDisplayed()

        Log.d("TEST NOTES", "Create Note - Successful creation")
        // Add tag
        composeRule.onNodeWithText(addTagString).performClick()
        waitForVm(500)
        composeRule.onNodeWithText(enterTagString).performTextInput(testTag)
        composeRule.onNodeWithText(addString).performClick()
        waitForVm(500)

        // Set field label
        composeRule.onAllNodesWithText(labelString)[0].performTextInput(testFieldLabel)
        waitForVm(500)

        // Set field content
        composeRule.onNodeWithText(textContentString).performTextInput(testFieldContent)
        waitForVm(500)

        // Wait for the Create button to be enabled first
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodesWithText(createNoteString)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeRule.onNodeWithText(createNoteString).performClick()
        waitForVm(3000) // Increase wait time for creation to complete

        // Wait for the created note to appear first
        composeRule.waitUntil(timeoutMillis = 20000) {
            composeRule.onAllNodesWithText(testFieldContent)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.waitForIdle()

        // Verify note appears - look for the first field
        composeRule.onAllNodesWithText(testFieldContent)[0].assertIsDisplayed()

        Log.d("TEST NOTES", "Update Note test")
        // Click on the note to open it, NOTE: onNodeWithText FAILS when multiple matching choices
        composeRule.onAllNodesWithText(testFieldContent)[0].performClick()
        waitForVm(1500) //

        // Click edit icon
        composeRule.onAllNodes(hasClickAction())[1].performClick()
        waitForVm(1000)

        // Add new tag
        composeRule.onNodeWithText(addTagString).performClick()
        waitForVm(500)
        composeRule.onNodeWithText(enterTagString).performTextInput(updatedTag)
        composeRule.onNodeWithText(addString).performClick()
        waitForVm(500)

        // Remove existing tag by clicking the chip
        composeRule.onAllNodesWithText(testTag)[0].performClick()
        waitForVm(500)

        // Update field content
        composeRule.onNodeWithText(testFieldContent).performTextClearance()
        composeRule.onNodeWithText(textContentString).performTextInput(updatedContent)
        waitForVm(500)

        // Save changes
        composeRule.onNodeWithText(saveString).performClick()
        waitForVm(2000)

        // Verify changes reflected
        composeRule.waitUntil(timeoutMillis = 20000) {
            composeRule.onAllNodesWithText(updatedContent)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText(updatedContent)[0].assertIsDisplayed()

        Log.d("TEST NOTES", "Share Note test")
        // Go back to edit screen (currently on view screen after save)
        composeRule.onAllNodes(hasClickAction())[1].performClick()  // Click edit icon
        waitForVm(1000)
        composeRule.onAllNodes(hasClickAction())[1].performClick()  // Click edit icon


        // Click share icon (index 1 in edit screen: 0=back, 1=share, 2=copy)
        composeRule.waitUntil(timeoutMillis = 20000) {
            composeRule.onAllNodesWithText("Edit Note")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        waitForVm(1000)

        composeRule.onNodeWithContentDescription(shareString).performClick()

        // Verify share dialog
        composeRule.waitUntil(timeoutMillis = 20000) {
            composeRule.onAllNodesWithText(shareNoteString)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(shareNoteString).assertIsDisplayed()

        // Select target workspace
        composeRule.onNodeWithText(WORKSPACE_2).performClick()
        waitForVm(500)

        // Confirm share
        composeRule.onNodeWithText(shareString).performClick()
        waitForVm(2000)

        // Navigate back to workspaces...
        composeRule.waitUntil(timeoutMillis = 20000) {
            composeRule.onAllNodesWithText(updatedContent)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(backString).performClick()
        waitForVm(1000)
        composeRule.onNodeWithContentDescription(wsIcString).performClick()
        waitForVm(1000)
        composeRule.waitUntil(timeoutMillis = 20000) {
            composeRule.onAllNodesWithContentDescription(contentString + WORKSPACE_2)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithContentDescription(contentString + WORKSPACE_2).performClick()
        waitForVm(2000)
        composeRule.waitUntil(timeoutMillis = 20000) {
            composeRule.onAllNodesWithText(updatedContent)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Verify not in workspace 1
        composeRule.onNodeWithContentDescription(wsIcString).performClick()
        waitForVm(1000)
        composeRule.waitUntil(timeoutMillis = 20000) {
            composeRule.onAllNodesWithContentDescription(contentString + WORKSPACE_1)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithContentDescription(contentString + WORKSPACE_1).performClick()
        waitForVm(2000)

        // Should not find the note tag in workspace 1 anymore
        val nodesInWs1 = composeRule.onAllNodesWithText(updatedContent).fetchSemanticsNodes()
        assert(nodesInWs1.isEmpty()) { "Note should not be in Workspace 1 after sharing" }

        Log.d("TEST NOTES", "Copy Note test")
        // Go back to workspace 2
        composeRule.onNodeWithContentDescription(wsIcString).performClick()
        waitForVm(1000)
        composeRule.waitUntil(timeoutMillis = 20000) {
            composeRule.onAllNodesWithContentDescription(contentString + WORKSPACE_2)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithContentDescription(contentString + WORKSPACE_2).performClick()
        waitForVm(2000)

        // Open note and go to edit
        composeRule.waitUntil(timeoutMillis = 20000) {
            composeRule.onAllNodesWithText(updatedContent)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onAllNodesWithText(updatedContent)[0].performClick()
        waitForVm(1500)
        composeRule.onAllNodes(hasClickAction())[1].performClick()  // Click edit icon
        waitForVm(1000)

        // Click copy icon
        composeRule.onNodeWithContentDescription(copyString).performClick()
        waitForVm(1000)

        // Verify copy dialog
        composeRule.onNodeWithText(copyNoteString).assertIsDisplayed()

        // Select workspace 1
        composeRule.onNodeWithText(WORKSPACE_1).performClick()
        waitForVm(500)

        // Confirm copy
        composeRule.onNodeWithText(copyString).performClick()
        waitForVm(2000)

        // Navigate and verify note in both workspaces
        composeRule.onNodeWithContentDescription(backString).performClick()
        waitForVm(1000)
        composeRule.waitUntil(timeoutMillis = 20000) {
            composeRule.onAllNodesWithText(updatedContent)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeRule.onNodeWithContentDescription(wsIcString).performClick()
        waitForVm(1000)
        composeRule.waitUntil(timeoutMillis = 20000) {
            composeRule.onAllNodesWithText(WORKSPACE_1)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithContentDescription(contentString + WORKSPACE_1).performClick()
        waitForVm(2000)
        composeRule.waitUntil(timeoutMillis = 20000) {
            composeRule.onAllNodesWithText(updatedContent)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        Log.d("TEST NOTES", "Delete Note test")
        // Open note
        composeRule.onAllNodesWithText(updatedContent)[0].performClick()
        waitForVm(1500)

        // Click delete icon
        composeRule.onNodeWithContentDescription(deleteString).performClick()
        waitForVm(1000)

        // Verify confirmation dialog
        composeRule.onNodeWithText(deleteNoteString).assertIsDisplayed()

        // Confirm deletion
        composeRule.onNodeWithText("Delete").performClick()
        waitForVm(2000)

        // Verify note removed from workspace 1
        val nodesAfterDelete = composeRule.onAllNodesWithText(updatedContent).fetchSemanticsNodes()
        assert(nodesAfterDelete.isEmpty()) { "Note should be deleted from Workspace 1" }

        Log.d("TEST NOTES", "Test completed successfully!")
    }
}