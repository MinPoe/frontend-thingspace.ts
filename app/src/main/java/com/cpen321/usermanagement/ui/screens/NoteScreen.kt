package com.cpen321.usermanagement.ui.screens

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cpen321.usermanagement.ui.components.BackActionButton
import com.cpen321.usermanagement.ui.viewmodels.NoteViewModel
import com.cpen321.usermanagement.utils.IFeatureActions
import androidx.compose.foundation.layout.PaddingValues

@Composable
fun NoteScreen(
    noteViewModel: NoteViewModel,
    onBackClick: () -> Unit,
    featureActions: IFeatureActions
){
    //event assignment logic

    NoteContent(onBackClick)
}

@Composable
fun NoteContent(onBackClick: () -> Unit, modifier:Modifier = Modifier)
{
    Scaffold(
        modifier = modifier,
        bottomBar = {
            BackActionButton(
                onBackClick,
                modifier = modifier)
        }
    ){paddingValues ->
        NoteBody(
            onBackClick = onBackClick,
            paddingValues = paddingValues,
            modifier = modifier
        )
    }
}

@Composable
fun NoteBody(onBackClick: ()-> Unit, paddingValues:PaddingValues, modifier: Modifier = Modifier)
{

}