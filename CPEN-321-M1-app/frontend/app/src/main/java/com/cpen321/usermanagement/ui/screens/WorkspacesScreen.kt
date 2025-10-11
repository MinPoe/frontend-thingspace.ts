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

    WsContent(onWsMainClick = onWsMainClick,
        onBackClick = onBackClick,
        availableWs = availableWs)
}
@Composable
private fun WsContent(
    onWsMainClick: (Int)-> Unit,
    onBackClick: ()->Unit,
    availableWs: List<String>,
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
            onWsMainClick = onWsMainClick)
    }
}

@Composable
private fun WsBody(
    availableWs: List<String>,
    paddingValues: PaddingValues,
    onWsMainClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (i in 0..(availableWs.size-1))
        Button(
            fullWidth = true,
            enabled = true,
            //TODO: Make Nicer Later, the point is we need a way to return to main somehow
            onClick = { onWsMainClick(i) },
        ) {
            val fontSizes = LocalFontSizes.current
            Text(
                text = availableWs[i],
                style = MaterialTheme.typography.bodyLarge,
                fontSize = fontSizes.extraLarge3,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = modifier
            )
        }
    }
}
