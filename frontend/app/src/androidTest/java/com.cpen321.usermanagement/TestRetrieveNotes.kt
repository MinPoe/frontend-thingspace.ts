package com.cpen321.usermanagement

import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import java.lang.Thread.sleep


/*
* Please log in to the below account on your emulator:
* thing4g@gmail.com
* Passwords as in attachments.
* Fill in the bio's of the account (with anything, just not empty).
*
*
* It might happen the UI Automator picks the wrong button on sign in. In this case:
* 1) Invalidate cache
* 2) run the app regularly, sign in to any account
* 3) run the test again (this time should work)
* */
@HiltAndroidTest
class TestRetrieveNotes {
    companion object{
        const val ACCT_NAME:String = "Thing4G"
        const val TAG1:String = "tag1"
        const val TAG2:String = "tag2"
        const val TAG3:String = "tag3"
        const val DISPLAY1: String = "note1"
        const val DISPLAY2: String = "note2"
        const val DISPLAY3: String = "3"
        const val DISPLAY4: String = "note4"
        const val QUERY: String = "1"
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

    private fun waitForSearch(searchButtonString:String){
        composeRule.waitUntil(20000){
            composeRule //Waiting for the success message to disappear
                .onAllNodesWithTag(searchButtonString)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun assessOrder(first:String, other:List<String>){
        val bounds1 = composeRule.onNodeWithText(first).fetchSemanticsNode().boundsInRoot
        for (item in other){
            val boundsO = composeRule.onNodeWithText(item).fetchSemanticsNode().boundsInRoot
            //the android coordinate is downwards positive, so this means if first is not first
            if (bounds1.bottom>boundsO.top) {
                Log.d("TEST RETRIEVE NOTES", "${bounds1.bottom},${boundsO.top}")
                assert(false)
            }
        }
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
        val signInString = composeRule.activity.getString(R.string.sign_in_with_google)

        try{uiAutomator { onElement { textAsString()=="Allow" }.click() }}catch(e:Exception){}
        waitForVm(1000)
        if(composeRule.onNodeWithText(signInString).isDisplayed()) signIn(signInString = signInString, ACCT_NAME)

        Log.d("TEST RETRIEVE NOTES", "Testing Search")
        waitForSearch(searchButtonString)
        composeRule.onNodeWithTag(searchButtonString).performClick()
        waitForSearch(searchButtonString)
        composeRule.onNodeWithText(DISPLAY1).assertIsDisplayed()
        composeRule.onNodeWithText(DISPLAY2).assertIsDisplayed()
        composeRule.onNodeWithText(DISPLAY3).assertIsDisplayed()
        composeRule.onNodeWithText(DISPLAY4).assertIsDisplayed()
        composeRule.onNodeWithTag(searchTextboxString).performTextInput(QUERY)
        composeRule.onNodeWithTag(searchButtonString).performClick()
        waitForSearch(searchButtonString)
        //the search results should be '3' first most similar to '1' according to the API
        //Of course, one would have to adjust this if one is to use other inputs for notes
        assessOrder(DISPLAY3, listOf(DISPLAY1, DISPLAY2, DISPLAY4))

        Log.d("TEST RETRIEVE NOTES", "Testing Filter - All Checkbox")
        composeRule.onNodeWithTag(filterIcString).performClick()
        waitForVm(1000)
        //by default, all tags should be selected, so that's where we start
        composeRule.onNodeWithTag(allString).assertIsOn()
        composeRule.onNodeWithTag(TAG1).assertIsOn()
        composeRule.onNodeWithTag(TAG2).assertIsOn()
        composeRule.onNodeWithTag(TAG3).assertIsOn()
        composeRule.onNodeWithTag(allString).performClick()
        waitForVm(1000)
        composeRule.onNodeWithTag(allString).assertIsOff()
        composeRule.onNodeWithTag(TAG1).assertIsOff()
        composeRule.onNodeWithTag(TAG2).assertIsOff()
        composeRule.onNodeWithTag(TAG3).assertIsOff()
        composeRule.onNodeWithTag(allString).performClick()
        waitForVm(1000)
        composeRule.onNodeWithTag(allString).assertIsOn()
        composeRule.onNodeWithTag(TAG1).assertIsOn()
        composeRule.onNodeWithTag(TAG2).assertIsOn()
        composeRule.onNodeWithTag(TAG3).assertIsOn()
        Log.d("TEST RETRIEVE NOTES", "Testing Filter - Note Selection by Tag")
        composeRule.onNodeWithTag(TAG2).performClick()
        composeRule.onNodeWithTag(allString).assertIsOn()
        composeRule.onNodeWithTag(TAG1).assertIsOn()
        composeRule.onNodeWithTag(TAG2).assertIsOff()
        composeRule.onNodeWithTag(TAG3).assertIsOn()
        composeRule.onNodeWithContentDescription(backIcString).performClick()
        waitForSearch(searchButtonString)
        composeRule.onNodeWithText(DISPLAY1).assertIsDisplayed()
        composeRule.onNodeWithText(DISPLAY2).assertIsNotDisplayed()
        composeRule.onNodeWithText(DISPLAY3).assertIsDisplayed()
        composeRule.onNodeWithText(DISPLAY4).assertIsDisplayed()
        assessOrder(DISPLAY3, listOf(DISPLAY1, DISPLAY4))
    }
}