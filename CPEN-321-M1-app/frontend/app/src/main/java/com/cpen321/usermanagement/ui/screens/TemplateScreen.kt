package com.cpen321.usermanagement.ui.screens

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

@Composable
fun TemplateScreen(
    onBackClick: () -> Unit
) {
    TemplateContent(
        onBackClick = onBackClick
    )
}

@Composable
private fun TemplateContent(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        bottomBar = {
            //TODO: change attributes later
            NoteBottomBar(
                onBackClick = onBackClick,
                modifier = modifier)
        }
    ) { paddingValues ->
        TemplateScreenBody(paddingValues = paddingValues)
    }
}

@Composable
private fun TemplateScreenBody( //TODO:for now copy of main, change to actual note adding
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        WelcomeMessage()
    }
}

@Composable //TODO: Replace with actual content
private fun WelcomeMessage(
    modifier: Modifier = Modifier
) {
    val fontSizes = LocalFontSizes.current

    Text(
        text = stringResource(R.string.bio_placeholder),
        style = MaterialTheme.typography.bodyLarge,
        fontSize = fontSizes.extraLarge3,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

@Composable
private fun NoteBottomBar(
    onBackClick: ()->Unit,
    modifier: Modifier = Modifier
){
    BottomAppBar(
        actions = {BackActionButton(onClick = onBackClick)},
        modifier = modifier
    )
}
