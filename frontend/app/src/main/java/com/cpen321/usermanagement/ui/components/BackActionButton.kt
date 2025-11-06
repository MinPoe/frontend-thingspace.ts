package com.cpen321.usermanagement.ui.components

import Icon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.ui.theme.LocalSpacing

//import Icon
//import androidx.compose.foundation.layout.size
//import androidx.compose.material3.IconButton
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import com.cpen321.usermanagement.R
//import com.cpen321.usermanagement.ui.theme.LocalSpacing
//
//@Composable
//fun BackActionButton(
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    val spacing = LocalSpacing.current
//
//    IconButton(
//        onClick = onClick,
//        modifier = modifier.size(spacing.extraLarge2)
//    ) {
//        BackIcon()
//    }
//}
//@Composable
//private fun BackIcon() {
//    Icon(
//        name = R.drawable.ic_arrow_back,
//    )
//}

@Composable
fun BackActionButton(
    onClick: ()->Unit,
    modifier: Modifier = Modifier
){
    BottomAppBar(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Absolute.Left,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ContentNoteActionButton(onClick = onClick)
        }
    }
}

@Composable
private fun ContentNoteActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        ContentIcon()
    }
}

@Composable
private fun ContentIcon() {
    Icon(
        name = R.drawable.ic_arrow_back,
        contentDescription = stringResource(R.string.back_icon_description)
    )
}