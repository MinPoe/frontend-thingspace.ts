package com.cpen321.usermanagement.ui.screens

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cpen321.usermanagement.ui.components.BackActionButton
import com.cpen321.usermanagement.ui.viewmodels.FilterViewModel
import com.cpen321.usermanagement.utils.IFeatureActions
import androidx.compose.foundation.layout.PaddingValues

//AI-generated imports
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.dp

@Composable
fun FilterScreen(
    filterViewModel: FilterViewModel,
    onBackClick: () -> Unit,
    featureActions: IFeatureActions
){
    FilterContent(
        onBackClick,
        availableTags = filterViewModel.getAvailTags(),
        selectedTags = featureActions.getSelectedTags(),
        allTagsSelected = featureActions.getAllTagsSelected(),
        onSelectionChanged = {selection:Set<String>, allSelected:Boolean ->
            featureActions.updateTagSelection(
            selectedTags = selection.toList(),
            allTagsSelected = allSelected
            ) }
        )
}

@Composable
fun FilterContent(onBackClick: () -> Unit,
                  availableTags:List<String>,
                  selectedTags:List<String>,
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
        // "All" checkbox row — now fully clickable
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            Checkbox(
                checked = isAllSelected,
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
            Text(
                text = "All",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Replace deprecated Divider with HorizontalDivider
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Tag checkboxes
        // Tag checkboxes
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
                        currentSelectedTags = if (isChecked) {
                            (currentSelectedTags - tag).toMutableSet()
                        } else {
                            (currentSelectedTags + tag).toMutableSet()
                        }
                        // "All" does not change automatically
                        onSelectionChanged(currentSelectedTags, isAllSelected)
                    }
                )
                Text(
                    text = tag,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clickable {
                            currentSelectedTags = if (isChecked) {
                                (currentSelectedTags - tag).toMutableSet()
                            } else {
                                (currentSelectedTags + tag).toMutableSet()
                            }
                            onSelectionChanged(currentSelectedTags, isAllSelected)
                        }
                )
            }
        }
    }
}