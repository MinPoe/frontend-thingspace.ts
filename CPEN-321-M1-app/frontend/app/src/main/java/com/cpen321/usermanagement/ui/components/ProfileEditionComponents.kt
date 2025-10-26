package com.cpen321.usermanagement.ui.components

import android.net.Uri

data class ManageProfileScreenActions(
    val onBackClick: () -> Unit,
    val onNameChange: (String) -> Unit,
    val onBioChange: (String) -> Unit,
    val onEditPictureClick: () -> Unit,
    val onSaveClick: () -> Unit,
    val onImagePickerDismiss: () -> Unit,
    val onImageSelected: (Uri) -> Unit,
    val onLoadingPhotoChange: (Boolean) -> Unit,
    val onSuccessMessageShown: () -> Unit,
    val onErrorMessageShown: () -> Unit
)