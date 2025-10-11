package com.cpen321.usermanagement.ui.components

import Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import androidx.compose.material3.TextField
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.ui.theme.LocalSpacing

@Composable
fun SearchBar(
    onQueryChange: (String)->Unit,
    onFilterClick: ()->Unit,
    modifier:Modifier = Modifier,
    query:String=""
){
    Row(
        verticalAlignment = Alignment.CenterVertically
    ){
        TextField(value=query, onValueChange=onQueryChange, modifier=modifier.weight(1f))
        FilterActionButton(
            onClick = onFilterClick, modifier=modifier
        )
    }
}

@Composable
private fun FilterActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        FilterIcon()
    }
}
@Composable
private fun FilterIcon() {
    Icon(
        name = R.drawable.ic_arrow_back,
    )
}