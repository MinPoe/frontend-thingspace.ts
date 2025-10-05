package com.cpen321.usermanagement.ui.screens

import Button
import Icon
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.ui.theme.LocalFontSizes
import com.cpen321.usermanagement.ui.theme.LocalSpacing
import com.cpen321.usermanagement.ui.components.BackActionButton
import androidx.compose.foundation.layout.Column

@Composable
fun WorkspaceListScreen(
    onBackClick: () -> Unit
) {
    WorkspaceListContent(
        onBackClick = onBackClick
    )
}

@Composable
private fun WorkspaceListContent(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        bottomBar = {
            //TODO: change attributes later
            WorkspaceListBottomBar(
                onBackClick = onBackClick,
                modifier = modifier)
        }
    ) { paddingValues ->
        WorkspaceListScreenBody(paddingValues = paddingValues, onBackClick = onBackClick)
    }
}

@Composable
private fun WorkspaceListScreenBody( //TODO:for now copy of main, change to actual note adding
    paddingValues: PaddingValues,
    onBackClick: ()->Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            fullWidth = true,
            enabled = true,
        onClick = onBackClick, //TODO: Change later
        ){
            val fontSizes = LocalFontSizes.current
            Text(
                text=stringResource(R.string.workspace_dummy_1),
                style = MaterialTheme.typography.bodyLarge,
                fontSize = fontSizes.extraLarge3,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = modifier
            )
        }
        Button(
            fullWidth = true,
            enabled = true,
            onClick = onBackClick, //TODO: Change later
        ){
            val fontSizes = LocalFontSizes.current
            Text(
                text=stringResource(R.string.workspace_dummy_2),
                style = MaterialTheme.typography.bodyLarge,
                fontSize = fontSizes.extraLarge3,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = modifier
            )
        }
    }
}


@Composable
private fun WorkspaceListBottomBar(
    onBackClick: ()->Unit,
    modifier: Modifier = Modifier
){
    BottomAppBar(
        actions = {BackActionButton(onClick = onBackClick)},
        modifier = modifier
    )
}
