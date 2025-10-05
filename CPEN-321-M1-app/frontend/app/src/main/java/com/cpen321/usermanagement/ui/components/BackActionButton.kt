package com.cpen321.usermanagement.ui.components

import Icon
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.ui.theme.LocalSpacing

@Composable
fun BackActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        BackIcon()
    }
}
@Composable
private fun BackIcon() {
    Icon(
        name = R.drawable.ic_arrow_back,
    )
}