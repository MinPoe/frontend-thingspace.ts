package com.cpen321.usermanagement

import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
* 1) run the app regularly, sign in to any account
* 2) run the test again (this time should work)
* */

@HiltAndroidTest
class TestReachWithTwoClicks {
    companion object{
        const val ACCT_NAME:String = "Thing4G"
        const val PERSONAL_WS_NAME:String = "Thing4G's Personal Workspace"
        const val WS_NAME:String = "test2clicks"
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

    private fun assessRoute(toClick:List<String>, toAssert:String){
        var clicks:Int = 0
        for (contentD in toClick){
            composeRule.onNodeWithContentDescription(contentD).performClick()
            clicks+=1
            waitForVm(5000)
        }
        composeRule.onNodeWithText(toAssert).assertIsDisplayed()
        assert(clicks<=2) //as per the nonfunctional requirement
    }

    @Test
    fun testReach() {
        hiltRule.inject()
        composeRule.activity.setContent {
            UserManagementApp()
        }

        val contentPlusString = composeRule.activity.getString(R.string.plusContent)
        val templatePlusString = composeRule.activity.getString(R.string.plusTemplates)
        val contentString = composeRule.activity.getString(R.string.content)
        val templateString = composeRule.activity.getString(R.string.templates)
        val chatString = composeRule.activity.getString(R.string.chat)
        val wsIcString = composeRule.activity.getString(R.string.workspaces)
        val signInString = composeRule.activity.getString(R.string.sign_in_with_google)
        val backIcString = composeRule.activity.getString(R.string.back_icon_description)

        try{uiAutomator { onElement { textAsString()=="Allow" }.click() }}catch(e:Exception){}
        waitForVm(1000)
        if(composeRule.onNodeWithText(signInString).isDisplayed()) signIn(signInString = signInString, ACCT_NAME)
        waitForVm(5000)

        Log.d("TEST REACH W\\2", "Moving the same workspace")
        assessRoute(listOf(templateString), PERSONAL_WS_NAME+templatePlusString)
        assessRoute(listOf(chatString), chatString)
        composeRule.onNodeWithContentDescription(backIcString).performClick()
        waitForVm(5000)

        Log.d("TEST REACH W\\2", "Moving To a different workspace")
        assessRoute(listOf(wsIcString, templateString+WS_NAME), WS_NAME+templatePlusString)
        assessRoute(listOf(wsIcString, contentString+WS_NAME), WS_NAME+contentPlusString)
        assessRoute(listOf(wsIcString, chatString+WS_NAME), chatString)
    }
}