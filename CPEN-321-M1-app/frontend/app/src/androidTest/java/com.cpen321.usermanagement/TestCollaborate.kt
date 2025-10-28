package com.cpen321.usermanagement

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cpen321.usermanagement.ui.screens.MainScreen
import com.cpen321.usermanagement.ui.theme.UserManagementTheme
import com.cpen321.usermanagement.ui.viewmodels.MainUiState
import com.cpen321.usermanagement.ui.viewmodels.MainViewModel
import com.cpen321.usermanagement.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TestCollaborate {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        composeRule.activity.setContent {
            UserManagementTheme {
                UserManagementApp() // hiltViewModel() works here
            }
        }
    }

    @Test
    fun showsWelcomeAfterContinue() {
        composeRule.onNodeWithText("Continue").performClick()
        composeRule.onNodeWithText("Welcome").assertIsDisplayed()
    }
}
