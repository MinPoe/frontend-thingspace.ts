package com.cpen321.usermanagement.ui.screens

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cpen321.usermanagement.ui.components.BackActionButton
import com.cpen321.usermanagement.ui.viewmodels.FilterViewModel
import com.cpen321.usermanagement.utils.IFeatureActions
import androidx.compose.foundation.layout.PaddingValues

@Composable
fun FilterScreen(
    filterViewModel: FilterViewModel,
    onBackClick: () -> Unit,
    featureActions: IFeatureActions
){
    //event assignment logic

    FilterContent(onBackClick)
}

@Composable
fun FilterContent(onBackClick: () -> Unit, modifier:Modifier = Modifier)
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
            paddingValues = paddingValues,
            modifier = modifier
        )
    }
}

@Composable
fun FilterBody(onBackClick: ()-> Unit, paddingValues:PaddingValues, modifier: Modifier = Modifier)
{

}