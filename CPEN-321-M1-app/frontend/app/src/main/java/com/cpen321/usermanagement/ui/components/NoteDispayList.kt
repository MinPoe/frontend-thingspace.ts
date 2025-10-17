package com.cpen321.usermanagement.ui.components

import androidx.compose.runtime.Composable
import Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.cpen321.usermanagement.ui.theme.LocalFontSizes

@Composable
fun NoteDisplayList(
    onNoteClick: (String)->Unit, //the input is noteId
    notes: List<String>,
    modifier: Modifier = Modifier
){
    for(note in notes){
        Button(onClick = {onNoteClick(note)}){
            val fontSizes = LocalFontSizes.current
            Text(
                text = note, //TODO: for now just displays a note id, we need to add note header-ing
                style = MaterialTheme.typography.bodyLarge,
                fontSize = fontSizes.extraLarge3,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = modifier
            )
        }
    }
}