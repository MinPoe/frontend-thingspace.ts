package com.cpen321.usermanagement.ui.components

import androidx.compose.runtime.Composable
import Button
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.data.remote.dto.Note
import com.cpen321.usermanagement.data.remote.dto.TextField
import com.cpen321.usermanagement.data.remote.dto.NumberField
import com.cpen321.usermanagement.data.remote.dto.DateTimeField
import com.cpen321.usermanagement.data.remote.dto.User
import com.cpen321.usermanagement.ui.theme.LocalFontSizes
import com.cpen321.usermanagement.ui.theme.LocalSpacing
import com.cpen321.usermanagement.data.remote.dto.Field

@Composable
private fun getFieldPreview(field: Field): String {
    return when (field) {
        is TextField -> field.content?.takeIf { it.isNotEmpty() } 
            ?: field.label.ifEmpty { stringResource(R.string.empty_note) }
        is NumberField -> field.content?.toString() 
            ?: field.label.ifEmpty { stringResource(R.string.empty_note) }
        is DateTimeField -> field.content?.toString() 
            ?: field.label.ifEmpty { stringResource(R.string.empty_note) }
        else -> stringResource(R.string.empty_note)
    }
}

@Composable
fun NoteDisplayList(
    onNoteClick: (String)->Unit,
    notes: List<Note>,
    modifier: Modifier = Modifier
){
    val spacing = LocalSpacing.current
    
    for(note in notes){
        val notePreview = note.fields.firstOrNull()?.let { getFieldPreview(it) }
            ?: stringResource(R.string.empty_note)
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.medium, vertical = spacing.small)
                .clickable { onNoteClick(note._id) },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = spacing.extraSmall
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.medium)
            ) {
                Text(
                    text = notePreview,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
fun ChatDisplayList(
    onProfileClick: (String)->Unit, //the input is noteId
    notes: List<Note>,
    profiles: List<User>?,
    modifier: Modifier = Modifier
){
    for(i in 0 until notes.size ){
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = modifier
        ){
            val fontSizes = LocalFontSizes.current
            if (profiles!=null){
                Button(onClick = {onProfileClick(profiles[i]._id)}){
                    Text(
                        text = profiles[i].profile.name, //TODO: for now just displays a note id, we need to add note header-ing
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = fontSizes.extraLarge3,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = modifier
                    )
                }
            }
            else{
                Button(onClick = {}){
                    Text(
                        text = stringResource(R.string.unknown), //TODO: for now just displays a note id, we need to add note header-ing
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = fontSizes.extraLarge3,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        //modifier = modifier
                    )
                }
            }
            Text(
                text = notes[i]._id, //TODO: for now just displays a note id, we need to add note header-ing
                style = MaterialTheme.typography.bodyLarge,
                fontSize = fontSizes.extraLarge3,
                color = MaterialTheme.colorScheme.inverseSurface,
                //modifier = modifier
            )

        }

    }
}