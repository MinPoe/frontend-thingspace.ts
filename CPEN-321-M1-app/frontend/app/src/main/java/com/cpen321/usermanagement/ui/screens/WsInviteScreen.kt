package com.cpen321.usermanagement.ui.screens

import Button
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.cpen321.usermanagement.ui.components.BackActionButton
import com.cpen321.usermanagement.ui.components.MessageSnackbar
import com.cpen321.usermanagement.ui.components.MessageSnackbarState
import com.cpen321.usermanagement.ui.navigation.FeatureActions
import com.cpen321.usermanagement.ui.viewmodels.InviteUiState
import com.cpen321.usermanagement.ui.viewmodels.InviteViewModel
import kotlinx.coroutines.flow.StateFlow

@Composable
fun WsInviteScreen(
    wsInviteViewModel: InviteViewModel,
    featureActions: FeatureActions,
    onBackClick: ()->Unit,
){
    val uiState by wsInviteViewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }

    val onInviteClick = { typedEmail:String ->
        wsInviteViewModel.onInviteClick(typedEmail)
    }
    val onBackClickHandler ={
        wsInviteViewModel.clearError()
        wsInviteViewModel.clearSuccessMessage()
        onBackClick()
    }

    WsInviteContent(
        onBackClick = onBackClickHandler,
        onInviteClick = onInviteClick,
        uiState = uiState,
        snackBarHostState = snackBarHostState,
        onSuccessMessageShown = wsInviteViewModel::clearSuccessMessage,
        onErrorMessageShown = wsInviteViewModel::clearError
    )
}

@Composable
private fun WsInviteContent(
    onBackClick: ()->Unit,
    onInviteClick: (String)->Unit,
    uiState: InviteUiState,
    snackBarHostState: SnackbarHostState,
    onSuccessMessageShown: () -> Unit,
    onErrorMessageShown: ()-> Unit,
    modifier: Modifier = Modifier
){
    Scaffold(
        bottomBar = { BackActionButton(onClick = onBackClick, modifier = modifier) },
        snackbarHost = { InviteSnackbarHost(hostState = snackBarHostState,
            uiState = uiState,
            onSuccessMessageShown = onSuccessMessageShown,
            onErrorMessageShown = onErrorMessageShown,
            modifier = modifier) }
    ) { paddingValues ->
        InviteWsBody(
            onInviteClick = onInviteClick,
            uiState = uiState,
            paddingValues = paddingValues
        )
    }
}

@Composable
private fun InviteSnackbarHost(
    hostState: SnackbarHostState,
    uiState: InviteUiState,
    onSuccessMessageShown: () -> Unit,
    onErrorMessageShown: ()->Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> {
            MessageSnackbar(
                hostState = hostState,
                messageState = MessageSnackbarState(
                    successMessage = null,
                    errorMessage = "Loading...",
                    onSuccessMessageShown = onSuccessMessageShown,
                    onErrorMessageShown = onErrorMessageShown
                ),
                modifier = modifier
            )
        }
        else -> {
            MessageSnackbar(
                hostState = hostState,
                messageState = MessageSnackbarState(
                    successMessage = uiState.successMessage,
                    errorMessage = uiState.errorMessage,
                    onSuccessMessageShown = onSuccessMessageShown,
                    onErrorMessageShown = onErrorMessageShown
                ),
                modifier = modifier
            )
        }
    }

}

@Composable
private fun InviteWsBody(
    onInviteClick: (String)->Unit,
    uiState: InviteUiState,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
){
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize()){
        var textValue by remember { mutableStateOf(uiState.typedEmail) }
        OutlinedTextField(
            value = textValue,
            onValueChange = { newText -> textValue = newText },
            label = { Text("Enter the email of the user to invite") },
            modifier = modifier
        )
        Button(onClick = {onInviteClick(textValue)}){
            Text("Invite to the workspace")
        }
    }
}