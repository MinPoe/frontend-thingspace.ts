package com.cpen321.usermanagement.ui.components

import Button
import Icon
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.ui.theme.LocalSpacing
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cpen321.usermanagement.ui.theme.LocalFontSizes

@Composable
fun SearchBar(
    onSearchClick: ()->Unit,
    onFilterClick: ()->Unit,
    onQueryChange: (String)->Unit,
    modifier:Modifier = Modifier,
    query:String=""
){
    var _query by remember { mutableStateOf(query) }
    val spacing = LocalSpacing.current
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.medium)
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ){
            OutlinedTextField(
                value = _query,
                onValueChange = {newVal:String->_query=newVal
                                onQueryChange(_query)},
                label = { Text(stringResource(R.string.search)) },
                placeholder = { Text(stringResource(R.string.search_placeholder)) },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = spacing.small),
                singleLine = true,
                readOnly = false,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            FilterActionButton(
                onClick = onFilterClick
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = spacing.medium)
        ) {
            Button(onClick = onSearchClick) {
                Text(text = stringResource(R.string.search))
            }
        }
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
        name = R.drawable.filter,
    )
}