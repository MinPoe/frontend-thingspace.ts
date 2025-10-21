package com.cpen321.usermanagement.ui.components

import androidx.compose.runtime.Composable
import Button
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.cpen321.usermanagement.data.remote.dto.Note
import com.cpen321.usermanagement.data.remote.dto.User
import com.cpen321.usermanagement.ui.theme.LocalFontSizes

@Composable
fun NoteDisplayList(
    onNoteClick: (String)->Unit, //the input is noteId
    notes: List<Note>,
    modifier: Modifier = Modifier
){
    for(note in notes){
        Button(onClick = {onNoteClick(note._id)}){
            val fontSizes = LocalFontSizes.current
            Text(
                text = note._id, //TODO: for now just displays a note id, we need to add note header-ing
                style = MaterialTheme.typography.bodyLarge,
                fontSize = fontSizes.extraLarge3,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = modifier
            )
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
                        text = profiles[i].name, //TODO: for now just displays a note id, we need to add note header-ing
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
                        text = "unknown", //TODO: for now just displays a note id, we need to add note header-ing
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