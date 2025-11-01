package com.cpen321.usermanagement

import android.app.UiAutomation
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToLog
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import com.cpen321.usermanagement.ui.screens.MainScreen
import com.cpen321.usermanagement.ui.theme.UserManagementTheme
import com.cpen321.usermanagement.ui.viewmodels.MainUiState
import com.cpen321.usermanagement.ui.viewmodels.MainViewModel
import com.cpen321.usermanagement.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.lang.Thread.sleep

/*
* Make sure you have the google account that will carry the tests signed up in your emulator!!!
* */


@HiltAndroidTest
class TestCollaborate {

    companion object{
        val ACCT_NAME="Friedrich van Aukstin"
    }

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    // Don't auto-launch the activity
    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()


//   @Before
//   fun setUp(){
//
//   }

    @Test
    fun showsWelcomeAfterContinue() {
        hiltRule.inject()
        composeRule.activity.setContent {
            UserManagementApp()
        }
        val signInString = composeRule.activity.getString(R.string.sign_in_with_google)
        val createWsString = "Create a new workspace..." //TODO: use the right R string

        uiAutomator {
            onElement { textAsString() == "Allow" }.click()
        }
        composeRule.onNodeWithText(signInString).performClick()
        uiAutomator {
            onElement { textAsString() == "Marek Gryszka" }.click()
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            device.wait(Until.hasObject(By.pkg("gbhj")), 5000)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Workspaces").performClick()
        composeRule.waitForIdle()
        val device1 = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device1.wait(Until.hasObject(By.pkg("gbhj")), 5000)
        Log.d("TEST", "Current package: ${device1.currentPackageName}")
        composeRule.waitForIdle()
        Log.d("TEST", "Current package: ${device1.currentPackageName}")
        composeRule.onNodeWithText(createWsString).performClick()
        //Log.d("TEST", "Current package: ${device1.currentPackageName}")
        //composeRule.waitForIdle()
        sleep(5000)
        composeRule.waitForIdle()
    }
}


