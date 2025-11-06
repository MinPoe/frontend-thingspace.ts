package com.cpen321.usermanagement.ui.screens

import Button
import android.widget.Button
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.ui.components.BackActionButton
import com.cpen321.usermanagement.ui.components.WorkspaceRow
import com.cpen321.usermanagement.ui.theme.LocalFontSizes
import com.cpen321.usermanagement.ui.viewmodels.WsSelectUIStateE
import com.cpen321.usermanagement.ui.viewmodels.WsSelectViewModel
import com.cpen321.usermanagement.utils.FeatureActions

data class WsMenuActions(
    val onWsMainClick: (Int) -> Unit,
    val onWsTemplateClick: (Int) -> Unit,
    val onWsChatClick: (Int) -> Unit,
    val onWsProfileClick: (Int) -> Unit,
    val onPersonalContentClick: () -> Unit,
    val onPersonalTemplateClick: () -> Unit,
    val onPersonalChatClick: () -> Unit,
    val onPersonalProfileClick:()-> Unit,
)

@Composable
fun WorkspacesScreen(
    workspacesViewModel: WsSelectViewModel,
    onBackClick: () -> Unit,
    onPersonalProfileClick: () -> Unit,
    featureActions: FeatureActions
){
    val uiState by workspacesViewModel.uiState.collectAsState()
    when (uiState.state){
        WsSelectUIStateE.TO_UPDATE->{
            workspacesViewModel.loadUserAndWorkspaces()
        }
        WsSelectUIStateE.LOADING -> {
            Box(modifier = Modifier, contentAlignment = Alignment.Center){
                CircularProgressIndicator()}
        }
        WsSelectUIStateE.DISPLAYING -> {
            val availableWs = uiState.workspaces
            val availableWsNames=availableWs.map { it.profile.name }
            val personalWs = uiState.personalWs!!

            val onWsMainClick = {index:Int ->
                featureActions.navs.navigateToMainTagReset(availableWs[index]._id)}
            val onWsChatClick = {index:Int ->
                featureActions.navs.navigateToChatTagReset(availableWs[index]._id)}
            val onWsTemplateClick = {index:Int ->
                featureActions.navs.navigateToTemplateTagReset(availableWs[index]._id)}
            val onPersonalProfileClick = onPersonalProfileClick
            val onPersonalChatClick={ featureActions.navs.navigateToMainTagReset(
                personalWs._id) }
            val onPersonalContentClick={ featureActions.navs.navigateToChatTagReset(
                personalWs._id) }
            val onPersonalTemplateClick={ featureActions.navs.navigateToTemplateTagReset(
                personalWs._id) }
            val onWsProfileClick = {index:Int ->
                if (uiState.workspaceManager != null){
                    val wsMan = uiState.workspaceManager!!
                    if (wsMan[index]){
                        featureActions.ws.navigateToWsProfileManager(availableWs[index]._id)
                    }
                    else{
                        featureActions.ws.navigateToWsProfile(availableWs[index]._id)
                    }
                }
                else featureActions.ws.navigateToWsProfile(availableWs[index]._id) }
            val onCreateClick = {featureActions.ws.navigateToWsCreation()}

            val wsMenuActions = WsMenuActions(
                onWsMainClick = onWsMainClick,
                onWsTemplateClick = onWsTemplateClick,
                onWsChatClick = onWsChatClick,
                onWsProfileClick = onWsProfileClick,
                onPersonalContentClick = onPersonalContentClick,
                onPersonalTemplateClick = onPersonalTemplateClick,
                onPersonalChatClick = onPersonalChatClick,
                onPersonalProfileClick = onPersonalProfileClick
            )


            WsContent(
                onBackClick = onBackClick,
                availableWs = availableWsNames,
                username = personalWs.profile.name,
                wsMenuActions = wsMenuActions,
                onCreateClick = onCreateClick
            )
        }
    }
}
@Composable
private fun WsContent(
    wsMenuActions: WsMenuActions,
    onBackClick: ()->Unit,
    availableWs: List<String>,
    username:String,
    onCreateClick: ()->Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        bottomBar = {
            BackActionButton(
                onBackClick,
                modifier = modifier)
        }
    ) { paddingValues ->
        WsBody(
            username = username,
            availableWs = availableWs,
            paddingValues = paddingValues,
            wsMenuActions = wsMenuActions,
            onCreateClick = onCreateClick
            )
    }
}

@Composable
private fun WsBody(
    availableWs: List<String>,
    username: String,
    paddingValues: PaddingValues,
    wsMenuActions: WsMenuActions,
    onCreateClick: ()->Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WorkspaceRow(
                workspaceName = username, //TODO: for now
                onContentClick = wsMenuActions.onPersonalContentClick,
                onTemplatesClick = wsMenuActions.onPersonalTemplateClick,
                onProfileClick = wsMenuActions.onPersonalProfileClick,
                onChatClick = wsMenuActions.onPersonalChatClick
            )
            for (i in 0..(availableWs.size-1))
                WorkspaceRow(
                    workspaceName = availableWs[i],
                    onContentClick = {wsMenuActions.onWsMainClick(i)},
                    onProfileClick = {wsMenuActions.onWsProfileClick(i)},//TODO: for Now,
                    onChatClick = {wsMenuActions.onWsChatClick(i)},
                    onTemplatesClick = {wsMenuActions.onWsTemplateClick(i)}
                )
        }

        // Create button at bottom with proper padding
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(com.cpen321.usermanagement.ui.theme.LocalSpacing.current.medium)
        ) {
            Button(onClick = onCreateClick) {
                Text(stringResource(R.string.create_new_workspace))
            }
        }
    }
}
