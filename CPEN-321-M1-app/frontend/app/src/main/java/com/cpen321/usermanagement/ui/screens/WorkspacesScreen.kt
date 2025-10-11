package com.cpen321.usermanagement.ui.screens

import Button
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.cpen321.usermanagement.ui.components.BackActionButton
import com.cpen321.usermanagement.ui.components.WorkspaceRow
import com.cpen321.usermanagement.ui.theme.LocalFontSizes
import com.cpen321.usermanagement.ui.viewmodels.WsSelectViewModel
import com.cpen321.usermanagement.utils.FeatureContext
import com.cpen321.usermanagement.utils.IFeatureActions

@Composable
fun WorkspacesScreen(
    workspacesViewModel: WsSelectViewModel,
    onBackClick: () -> Unit,
    featureActions: IFeatureActions
){
    val availableWs=listOf("ws1", "ws2") // TODO: will be obtained via the ViewModel
    val onWsMainClick = {index:Int ->
        featureActions.navigateToMainWithContext(FeatureContext(workspaceId = availableWs[index]))}
    val onWsChatClick = {index:Int ->
        featureActions.navigateToChat(FeatureContext(workspaceId = availableWs[index]))}
    val onWsTemplateClick = {index:Int ->
        featureActions.navigateToTemplate(FeatureContext(workspaceId = availableWs[index]))}
    val onPersonalProfileClick = {} //TODO: for now before the situation with profile clarifies
    val onPersonalChatClick={featureActions.navigateToChat(context= FeatureContext())}
    val onPersonalContentClick={featureActions.navigateToMainWithContext(context= FeatureContext())}
    val onPersonalTemplateClick={featureActions.navigateToTemplate(context= FeatureContext())}

    WsContent(onWsMainClick = onWsMainClick,
        onBackClick = onBackClick,
        availableWs = availableWs,
        onWsChatClick= onWsChatClick,
        onWsTemplateClick = onWsTemplateClick,
        onPersonalProfileClick = onPersonalProfileClick,
        onPersonalContentClick = onPersonalContentClick,
        onPersonalChatClick = onPersonalChatClick,
        onPersonalTemplateClick = onPersonalTemplateClick
    )
}
@Composable
private fun WsContent(
    onWsMainClick: (Int)-> Unit,
    onBackClick: ()->Unit,
    availableWs: List<String>,
    onWsTemplateClick: (Int)->Unit,
    onWsChatClick: (Int)->Unit,
    onPersonalProfileClick: ()->Unit,
    onPersonalContentClick: ()->Unit,
    onPersonalChatClick: ()->Unit,
    onPersonalTemplateClick: ()->Unit,
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
            availableWs = availableWs,
            paddingValues = paddingValues,
            onWsMainClick = onWsMainClick,
            onWsChatClick = onWsChatClick,
            onWsTemplateClick = onWsTemplateClick,
            onPersonalProfileClick = onPersonalProfileClick,
            onPersonalContentClick = onPersonalContentClick,
            onPersonalChatClick = onPersonalChatClick,
            onPersonalTemplateClick = onPersonalTemplateClick
            )
    }
}

@Composable
private fun WsBody(
    availableWs: List<String>,
    paddingValues: PaddingValues,
    onWsMainClick: (Int) -> Unit,
    onWsChatClick: (Int) -> Unit,
    onWsTemplateClick: (Int) -> Unit,
    onPersonalProfileClick: ()->Unit,
    onPersonalContentClick: ()->Unit,
    onPersonalChatClick: ()->Unit,
    onPersonalTemplateClick: ()->Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WorkspaceRow(
            workspaceName = "personal", //TODO: for now
            onContentClick = onPersonalContentClick,
            onTemplatesClick = onPersonalTemplateClick,
            onProfileClick = onPersonalProfileClick,
            onChatClick = onPersonalChatClick
        )
        for (i in 0..(availableWs.size-1))
            WorkspaceRow(
                workspaceName = availableWs[i],
                onContentClick = {onWsMainClick(i)},
                onProfileClick = {},//TODO: for Now,
                onChatClick = {onWsChatClick(i)},
                onTemplatesClick = {onWsTemplateClick(i)}
            )
    }
}
