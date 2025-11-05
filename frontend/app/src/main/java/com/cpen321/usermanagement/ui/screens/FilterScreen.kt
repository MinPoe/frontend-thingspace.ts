package com.cpen321.usermanagement.ui.screens

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cpen321.usermanagement.ui.components.BackActionButton
import com.cpen321.usermanagement.ui.viewmodels.FilterViewModel
import com.cpen321.usermanagement.utils.FeatureActions
import androidx.compose.foundation.layout.PaddingValues

//AI-generated imports
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cpen321.usermanagement.R

@Composable
fun FilterScreen(
    filterViewModel: FilterViewModel,
    onBackClick: () -> Unit,
    featureActions: FeatureActions
){
    val loading = filterViewModel.loading.collectAsState()
    FilterContent(
        onBackClick,
        availableTags = filterViewModel.getAvailTags(),
        selectedTags = featureActions.state.getSelectedTags(),
        loading = loading.value,
        allTagsSelected = featureActions.state.getAllTagsSelected(),
        onSelectionChanged = {selection:Set<String>, allSelected:Boolean ->
            featureActions.state.updateTagSelection(
            selectedTags = selection.toList(),
            allTagsSelected = allSelected
            ) }
        )
}

@Composable
fun FilterContent(onBackClick: () -> Unit,
                  availableTags:List<String>,
                  selectedTags:List<String>,
                  loading: Boolean,
                  allTagsSelected: Boolean,
                  onSelectionChanged: (Set<String>, Boolean) -> Unit,
                  modifier:Modifier = Modifier)
{
    Scaffold(
        modifier = modifier,
        bottomBar = {
            BackActionButton(
                onBackClick,
                modifier = modifier)
        }
    ){paddingValues ->
        if (loading){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {CircularProgressIndicator(modifier = modifier.align(Alignment.Center))}
        }
        else {
            FilterBody(
                onBackClick = onBackClick,
                availableTags = availableTags,
                allTagsSelected = allTagsSelected,
                selectedTags = selectedTags,
                onSelectionChanged = onSelectionChanged,
                paddingValues = paddingValues,
                modifier = modifier
            )
        }
    }
}

@Composable
fun FilterBody(onBackClick: ()-> Unit,
               availableTags:List<String>,
               selectedTags:List<String>,
               allTagsSelected: Boolean,
               onSelectionChanged: (Set<String>, Boolean) -> Unit,
               paddingValues:PaddingValues,
               modifier: Modifier = Modifier)
{
    TagSelector(availableTags, selectedTags.toSet(),
        allTagsSelected, onSelectionChanged)
}

@Composable
fun TagSelector(
    tags: List<String>,
    selectedTags: Set<String>,
    allTagsSelected: Boolean,
    onSelectionChanged: (Set<String>, Boolean) -> Unit
) {
    var currentSelectedTags by remember { mutableStateOf(selectedTags.toMutableSet()) }
    var isAllSelected by remember { mutableStateOf(allTagsSelected) }

    Column(modifier = Modifier.padding(16.dp)) {
        AllTagsCheckbox(
            isAllSelected = isAllSelected,
            onCheckedChange = { checked ->
                isAllSelected = checked
                if (checked) {
                    currentSelectedTags = tags.toMutableSet()
                } else {
                    currentSelectedTags.clear()
                }
                onSelectionChanged(currentSelectedTags, isAllSelected)
            }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        TagsList(
            tags = tags,
            currentSelectedTags = currentSelectedTags,
            isAllSelected = isAllSelected,
            onSelectedTagsChange = { currentSelectedTags = it },
            onSelectionChanged = onSelectionChanged
        )
    }
}

@Composable
private fun AllTagsCheckbox(
    isAllSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Checkbox(
            checked = isAllSelected,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = stringResource(R.string.all),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun TagsList(
    tags: List<String>,
    currentSelectedTags: MutableSet<String>,
    isAllSelected: Boolean,
    onSelectedTagsChange: (MutableSet<String>) -> Unit,
    onSelectionChanged: (Set<String>, Boolean) -> Unit
) {
    tags.forEach { tag ->
        val isChecked = currentSelectedTags.contains(tag)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { checked ->
                    val newSelectedTags = if (checked) {
                        (currentSelectedTags + tag).toMutableSet()
                    } else {
                        (currentSelectedTags - tag).toMutableSet()
                    }
                    onSelectedTagsChange(newSelectedTags)
                    onSelectionChanged(newSelectedTags, isAllSelected)
                }
            )
            Text(
                text = tag,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable {
                        val newSelectedTags = if (isChecked) {
                            (currentSelectedTags - tag).toMutableSet()
                        } else {
                            (currentSelectedTags + tag).toMutableSet()
                        }
                        onSelectedTagsChange(newSelectedTags)
                        onSelectionChanged(newSelectedTags, isAllSelected)
                    }
            )
        }
    }
}