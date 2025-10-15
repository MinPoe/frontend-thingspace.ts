package com.cpen321.usermanagement.ui.components

import Icon
import androidx.compose.foundation.focusable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.ui.theme.LocalSpacing
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import com.cpen321.usermanagement.ui.navigation.FeatureActions

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
        var _query by remember { mutableStateOf(query) }
        OutlinedTextField(
            value = _query,
            onValueChange = {newVal:String->_query=newVal},
            label = { Text(stringResource(R.string.bio)) },
            placeholder = { Text(stringResource(R.string.bio_placeholder)) },
            modifier = modifier.weight(1f).padding(16.dp),
            singleLine = true,
            readOnly = false //Here a fix was conducted: Users SHOULD be able to edit their bio after account creation
        )
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