package com.cpen321.usermanagement.ui.screens

import Icon
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.cpen321.usermanagement.ui.viewmodels.MainViewModel
import com.cpen321.usermanagement.ui.theme.LocalFontSizes
import com.cpen321.usermanagement.ui.theme.LocalSpacing
import com.cpen321.usermanagement.ui.components.MainBottomBar
import com.cpen321.usermanagement.ui.components.NoteDisplayList
import com.cpen321.usermanagement.ui.components.SearchBar
import com.cpen321.usermanagement.utils.IFeatureActions
import kotlinx.coroutines.flow.compose

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    onProfileClick: () -> Unit,
    featureActions: IFeatureActions
) {
    val uiState by mainViewModel.uiState.collectAsState()
    val fetching by mainViewModel.fetching.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val wsname =  mainViewModel.getWorkspaceName()

    MainContent(
        uiState = uiState,
        snackBarHostState = snackBarHostState,
        onProfileClick = onProfileClick,
        onTemplateClick = {  featureActions.navigateToTemplateTagReset(
            featureActions.getWorkspaceId())},
        onWorkspaceClick = { featureActions.navigateToWsSelect()},
        onFilterClick = { featureActions.navigateToFilter(
            workspaceId = featureActions.getWorkspaceId(),
            selectedTags = featureActions.getSelectedTags(),
            allTagsSelected = featureActions.getAllTagsSelected()
        ) },
        onSearchClick = {featureActions.navigateToMainWithContext(
            workspaceId = featureActions.getWorkspaceId(),
            selectedTags = featureActions.getSelectedTags(),
            allTagsSelected = featureActions.getAllTagsSelected(),
            searchQuery = featureActions.getSearchQuery()
        )},
        onChatClick = { featureActions.navigateToChatTagReset(
            featureActions.getWorkspaceId()) },
        onQueryChange = {query:String -> featureActions.setSearchQuery(query)},
        workspaceName = wsname,
        query = featureActions.getSearchQuery(),
        onSuccessMessageShown = mainViewModel::clearSuccessMessage,
        onCreateNoteClick = { featureActions.navigateToNoteCreation() },
        onNoteClick = {noteId:String -> featureActions.navigateToNote(noteId)},
        fetching = fetching,
        notes = mainViewModel.getNotesTitlesFound(0) //TODO no pagination 4 now
    )
}

@Composable
private fun MainContent(
    uiState: MainUiState,
    snackBarHostState: SnackbarHostState,
    onProfileClick: () -> Unit,
    onTemplateClick: ()-> Unit,
    onWorkspaceClick: () -> Unit,
    onFilterClick: () -> Unit,
    onChatClick: ()->Unit,
    onSearchClick: ()-> Unit,
    onQueryChange: (String)-> Unit,
    query: String,
    onSuccessMessageShown: () -> Unit,
    workspaceName: String,
    onCreateNoteClick: ()-> Unit,
    onNoteClick: (String)->Unit,
    notes:List<Note>,
    fetching: Boolean,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            MainTopBar(onProfileClick = onProfileClick)
        },
        snackbarHost = {
            MainSnackbarHost(
                hostState = snackBarHostState,
                successMessage = uiState.successMessage,
                onSuccessMessageShown = onSuccessMessageShown
            )
        },
        bottomBar = {
            MainBottomBar(
                onCreateNoteClick = onCreateNoteClick,
                onWorkspacesClick = onWorkspaceClick,
                onTemplatesClick = onTemplateClick,
                onContentClick = {  },
                onChatClick = onChatClick,
                modifier = modifier)
        }
    ) { paddingValues ->
        if(!fetching) {
            MainBody(
                paddingValues = paddingValues,
                workspaceName = workspaceName,
                onFilterClick = onFilterClick,
                onSearchClick = onSearchClick,
                onQueryChange = onQueryChange,
                onNoteClick = onNoteClick,
                notes = notes,
                query = query
            )
        }
        else{
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {CircularProgressIndicator(modifier = modifier.align(Alignment.Center))}
        }
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
private fun MainSnackbarHost(
    hostState: SnackbarHostState,
    successMessage: String?,
    onSuccessMessageShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    MessageSnackbar(
        hostState = hostState,
        messageState = MessageSnackbarState(
            successMessage = successMessage,
            errorMessage = null,
            onSuccessMessageShown = onSuccessMessageShown,
            onErrorMessageShown = { }
        ),
        modifier = modifier
    )
}

@Composable
private fun MainBody(
    paddingValues: PaddingValues,
    onFilterClick: () -> Unit,
    workspaceName: String,
    query: String,
    onSearchClick: ()-> Unit,
    onQueryChange: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    notes:List<Note>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WorkspaceName(workspaceName)
        SearchBar(
            onSearchClick = onSearchClick,
            onFilterClick = onFilterClick,
            onQueryChange = onQueryChange,
            query = query
        )
        NoteDisplayList(
            onNoteClick = onNoteClick,
            notes = notes,
        )
    }
}
@Composable
private fun WorkspaceName(
    workspaceName: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = workspaceName + stringResource(R.string.plusContent),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.padding(horizontal = LocalSpacing.current.medium),
        maxLines = 2,
        softWrap = true
    )
}

