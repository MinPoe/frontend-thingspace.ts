package com.cpen321.usermanagement.ui.components

import android.provider.ContactsContract
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.data.remote.dto.Note
import com.cpen321.usermanagement.data.remote.dto.NoteType
import com.cpen321.usermanagement.ui.theme.LocalSpacing
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun NoteInfoRow(
    creationDateString: String,
    lastEditDateSting: String,
){
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ){
        // Creation Date
        DateInfoCard(
            title = stringResource(R.string.created),
            dateString = creationDateString
        )

        // Last Edit Date
        DateInfoCard(
            title = stringResource(R.string.last_edited),
            dateString =lastEditDateSting
        )
    }
}

@Composable
private fun DateInfoCard(
    title: String,
    dateString: String
) {
    InfoCard(
        title = title,
        content = try {
            java.time.Instant.parse(dateString)
                .atZone(java.time.ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"))
        } catch (e: java.time.format.DateTimeParseException) {
            dateString
        }
    )
}

@Composable
private fun InfoCard(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(spacing.medium)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(spacing.extraSmall))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}