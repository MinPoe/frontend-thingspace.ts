package com.cpen321.usermanagement.ui.screens

import Button
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
import com.cpen321.usermanagement.ui.components.BackActionButton
import com.cpen321.usermanagement.ui.components.WorkspaceMembersManagerRow
import com.cpen321.usermanagement.ui.components.WorkspaceMembersRow
import com.cpen321.usermanagement.ui.components.WorkspaceRow
import com.cpen321.usermanagement.ui.theme.LocalFontSizes
import com.cpen321.usermanagement.ui.viewmodels.MembersManagerViewModel
import com.cpen321.usermanagement.ui.viewmodels.MembersViewModel
import com.cpen321.usermanagement.ui.viewmodels.WsSelectViewModel
import com.cpen321.usermanagement.utils.IFeatureActions


@Composable
fun WsMembersManagerScreen(
    membersManagerViewModel: MembersManagerViewModel,
    onBackClick: () -> Unit,
    onPersonalProfileClick: () -> Unit,
    featureActions: IFeatureActions
){
    val uiState by membersManagerViewModel.uiState.collectAsState()
    val isLoading = uiState.isLoading
    when {
        isLoading -> {
            Box(modifier = Modifier, contentAlignment = Alignment.Center){
                CircularProgressIndicator()}
        }
        else -> {
            val userAndOthers =membersManagerViewModel.getUsers()
            val otherUsers = userAndOthers.second
            val otherUsersNames= otherUsers.map { it.profile.name }
            val user = userAndOthers.first

            val onPersonalProfileClick = onPersonalProfileClick
            val onOtherProfileClick = {index:Int -> featureActions.navigateToWsProfile(
                otherUsers[index]._id) }
            val onBanClick = {index:Int -> membersManagerViewModel.ban(
                otherUsers[index]._id)}

            MembersManagerContent(
                onBackClick = onBackClick,
                otherUsers = otherUsersNames,
                username = user.profile.name,
                onBanClick = onBanClick,
                onOtherProfileClick = onOtherProfileClick,
                onPersonalProfileClick = onPersonalProfileClick,
            )
        }
    }
}
@Composable
private fun MembersManagerContent(
    onBackClick: ()->Unit,
    otherUsers: List<String>,
    username:String,
    onBanClick: (Int)->Unit,
    onOtherProfileClick: (Int)->Unit,
    onPersonalProfileClick: ()->Unit,
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
        MemebersBody(
            username = username,
            otherUsers = otherUsers,
            paddingValues = paddingValues,
            onOtherProfileClick = onOtherProfileClick,
            onBanClick = onBanClick,
            onPersonalProfileClick = onPersonalProfileClick,
            )
    }
}

@Composable
private fun MemebersBody(
    otherUsers: List<String>,
    username: String,
    onBanClick: (Int)->Unit,
    paddingValues: PaddingValues,
    onOtherProfileClick: (Int) -> Unit,
    onPersonalProfileClick: ()->Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WorkspaceMembersRow(
            profileName = username, //TODO: for now
            onProfileClick = onPersonalProfileClick,
        )
        for (i in 0..(otherUsers.size-1))
            WorkspaceMembersManagerRow(
                profileName = otherUsers[i],
                onProfileClick = {onOtherProfileClick(i)},//TODO: for Now,
                onBanClick = {onBanClick(i)}
            )
    }
}
