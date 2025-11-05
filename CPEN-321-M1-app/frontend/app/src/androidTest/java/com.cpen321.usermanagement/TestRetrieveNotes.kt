package com.cpen321.usermanagement

import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import com.cpen321.usermanagement.TestCollaborate.Companion.addedAMemberString
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import java.lang.Thread.sleep


/*
* One needs four notes created:
* note 1 with TAG1 and displaying as DISPLAY1
* note 2 with TAG2 and displaying as DISPLAY2
* note 3 with TAG3 and displaying as DISPLAY3
* note 4 with TAG1 and TAG2 displaying as DISPLAY4
* */
@HiltAndroidTest
class TestRetrieveNotes {
    companion object{
        const val TAG1:String = "tag1"
        const val TAG2:String = "tag2"
        const val TAG3:String = "tag3"
        const val DISPLAY1: String = "note1"
        const val DISPLAY2: String = "note2"
        const val DISPLAY3: String = "3"
        const val DISPLAY4: String = "note4"
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

    private fun signIn(signInString:String, acctName:String){
        composeRule.waitForIdle()
        composeRule.onNodeWithText(signInString).performClick()
        uiAutomator {
            Log.d("TEST_SIGN_IN", "Before Sign in user selected")
            onElement { textAsString() == acctName }.click()
            Log.d("TEST_SIGN_IN", "Sign in user selected")
        }
        composeRule.waitForIdle()
    }

    @Test
    fun testNoteRetrieval() {
        hiltRule.inject()
        composeRule.activity.setContent {
            UserManagementApp()
        }

        //UI strings
        val filterIcString = composeRule.activity.getString(R.string.filter)
        val allString = composeRule.activity.getString(R.string.all)
        val searchButtonString = composeRule.activity.getString(R.string.search_button)
        val searchTextboxString = composeRule.activity.getString(R.string.search_textbox)
        val backIcString = composeRule.activity.getString(R.string.back_icon_description)

        Log.d("TEST RETRIEVE NOTES", "Testing Search")
        composeRule.waitUntil(20000){
            composeRule //Waiting for the success message to disappear
                .onAllNodesWithTag(searchButtonString)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithTag(searchButtonString).performClick()
        waitForVm(1000)
        //TODO assert all 4 notes are present
        composeRule.onNodeWithTag(searchTextboxString).performTextInput(DISPLAY1)
        composeRule.onNodeWithTag(searchButtonString).performClick()
        waitForVm(1000)
        //TODO asserts of order

        Log.d("TEST RETRIEVE NOTES", "Testing Filter")


    }
}