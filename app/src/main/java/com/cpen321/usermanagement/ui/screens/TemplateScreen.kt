package com.cpen321.usermanagement.ui.screens

import Button
import Icon
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.data.remote.dto.Note
import com.cpen321.usermanagement.ui.components.MessageSnackbar
import com.cpen321.usermanagement.ui.components.MessageSnackbarState
import com.cpen321.usermanagement.ui.viewmodels.MainUiState
import com.cpen321.usermanagement.ui.viewmodels.TemplateViewModel
import com.cpen321.usermanagement.ui.theme.LocalFontSizes
import com.cpen321.usermanagement.ui.theme.LocalSpacing
import com.cpen321.usermanagement.ui.components.MainBottomBar
import com.cpen321.usermanagement.ui.components.NoteDisplayList
import com.cpen321.usermanagement.ui.components.SearchBar
import com.cpen321.usermanagement.ui.navigation.FeatureActions
import com.cpen321.usermanagement.utils.IFeatureActions

@Composable
fun TemplateScreen(
    templateViewModel: TemplateViewModel,
    onProfileClick: () -> Unit,
    featureActions: IFeatureActions
) {

    templateViewModel.onLoad()

    TemplateContent(
        onProfileClick = onProfileClick,
        onNoteClick = { noteId:String -> featureActions.navigateToNote(noteId) }, //TODO: for now
        onContentClick = {  featureActions.navigateToMainWithContext(
            featureActions.getWorkspaceId()) },
        onWorkspaceClick = { featureActions.navigateToWsSelect() },
        onFilterClick = { featureActions.navigateToFilter(
            workspaceId = featureActions.getWorkspaceId(),
            selectedTags = featureActions.getSelectedTags(),
            allTagsSelected = featureActions.getAllTagsSelected()
        )},
        onChatClick={
            featureActions.navigateToChat(
                featureActions.getWorkspaceId()
            )
        },
        onSearchClick = { featureActions.navigateToTemplate(
            workspaceId = featureActions.getWorkspaceId(),
            selectedTags = featureActions.getSelectedTags(),
            allTagsSelected = featureActions.getAllTagsSelected(),
            searchQuery = featureActions.getSearchQuery()
        ) },
        onQueryChange = {query:String -> featureActions.setSearchQuery(query)},
        onCreateNoteClick = { featureActions.navigateToNote("") },
        notes = templateViewModel.getNotesTitlesFound(0),
        query = featureActions.getSearchQuery() // TODO: BAD
    )
}

@Composable
private fun TemplateContent(
    onProfileClick: () -> Unit,
    onNoteClick: (String)-> Unit,
    onCreateNoteClick: ()-> Unit,
    notes:List<Note>,
    onContentClick: ()->Unit,
    onChatClick: ()-> Unit,
    onWorkspaceClick: () -> Unit,
    onFilterClick: () -> Unit,
    query:String,
    onSearchClick: ()->Unit,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            MainTopBar(onProfileClick = onProfileClick)
        },
        bottomBar = {
            MainBottomBar(
                onCreateNoteClick = onCreateNoteClick,
                onWorkspacesClick = onWorkspaceClick,
                onTemplatesClick = {  },
                onContentClick = onContentClick,
                onChatClick = onChatClick,
                modifier = modifier)
        }
    ) { paddingValues ->
        MainBody(
            paddingValues = paddingValues,
            onFilterClick = onFilterClick,
            onSearchClick = onSearchClick,
            onQueryChange = onQueryChange,
            onNoteClick = onNoteClick,
            notes = notes,
            query = query)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopBar(
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = {
            AppTitle()
        },
        actions = {
            ProfileActionButton(onClick = onProfileClick)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun AppTitle(
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(R.string.app_name),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Medium,
        modifier = modifier
    )
}

@Composable
private fun ProfileActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        ProfileIcon()
    }
}

@Composable
private fun ProfileIcon() {
    Icon(
        name = R.drawable.ic_account_circle,
    )
}

@Composable
private fun MainBody(
    paddingValues: PaddingValues,
    onFilterClick: () -> Unit,
    query: String,
    onSearchClick: ()->Unit,
    onQueryChange: (String)->Unit,
    onNoteClick: (String)-> Unit,
    notes:List<Note>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WelcomeMessage()
        SearchBar(
            onSearchClick = onSearchClick,//TODO: for now
            onFilterClick = onFilterClick,
            onQueryChange = onQueryChange,
            query = query
        )
        NoteDisplayList(
            onNoteClick = onNoteClick,
            notes = notes
        )
    }
}
@Composable
private fun WelcomeMessage(
    modifier: Modifier = Modifier
) {
    val fontSizes = LocalFontSizes.current

    Text(
        text = stringResource(R.string.welcome),
        style = MaterialTheme.typography.bodyLarge,
        fontSize = fontSizes.extraLarge3,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

