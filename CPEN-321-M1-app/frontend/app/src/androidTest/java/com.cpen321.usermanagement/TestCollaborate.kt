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


   @Before
   fun setUp(){
       hiltRule.inject()
       composeRule.activity.setContent {
           UserManagementApp()
       }

       uiAutomator {
           onElement { textAsString() == "Allow" }.click()
       }
       composeRule.onNodeWithText("Sign in with Google").performClick()
       uiAutomator {
           onElement { textAsString() == ACCT_NAME }.click()
           //waitForAppToBeVisible("com.cpen321.usermanagement")
           //onElement { textAsString() == "Search" }.click() //TODO: wait until loads
           val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
           Log.d("TEST", "Current package: ${device.currentPackageName}")
           device.wait(Until.hasObject(By.pkg("your.package.name")), 5000)
           Log.d("TEST", "Current package: ${device.currentPackageName}")
       }
       composeRule.waitForIdle()
   }

    @Test
    fun showsWelcomeAfterContinue() {
        composeRule.onNodeWithContentDescription("Workspaces").performClick()
        composeRule.waitForIdle()
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.wait(Until.hasObject(By.pkg("your.package.name")), 5000)
        composeRule.waitForIdle()
    }
}


