package com.cpen321.usermanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.ui.components.BackActionButton
import com.cpen321.usermanagement.ui.navigation.FeatureActions
import com.cpen321.usermanagement.ui.viewmodels.CreateWsUiStateE
import com.cpen321.usermanagement.ui.viewmodels.WsCreationViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkspaceScreen(
    featureActions: FeatureActions,
    onBackClick: ()->Unit,
    wsCreationViewModel: WsCreationViewModel
) {
    val uiState by wsCreationViewModel.uiState.collectAsState()
    var text by remember { mutableStateOf("") }

    LaunchedEffect(uiState.stateEnum) {
        if (uiState.stateEnum == CreateWsUiStateE.AFTER){
            featureActions.navigateToWsProfileManager(uiState.newWsId)
            wsCreationViewModel.resetUIStateEnum()
        }
    }

    if (uiState.stateEnum == CreateWsUiStateE.BEFORE)
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.create_workspace_title)) }
                )
            },
            bottomBar = {
                BackActionButton(onBackClick)
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { 
                        text = it
                        wsCreationViewModel.clearError()
                    },
                    label = { Text(stringResource(R.string.pick_workspace_name)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, false),
                    isError = uiState.errorMessage != null
                )

                // Display error message if it exists
                uiState.errorMessage?.let { errorMessage ->
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))


                Button(
                    onClick = { wsCreationViewModel.createWorkspace(text) },
                    enabled = text.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.create_workspace))
                }
            }

        }
    else
        Box(modifier = Modifier, contentAlignment = Alignment.Center){
            CircularProgressIndicator()}

}