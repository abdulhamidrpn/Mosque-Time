package com.rpn.mosquetime.presentation.screen.permission.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class PermissionInfo(
    val permission: String, // Use a unique string identifier, even for non-standard permissions
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isEssential: Boolean = true, // Mark if crucial for core functionality
    val isStandardPermission: Boolean = true // Flag to differentiate standard vs special (Intent-based)
)

@Composable
fun PermissionCard(
    permissionInfo: PermissionInfo,
    isGranted: Boolean,
    onGrantClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = permissionInfo.icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = permissionInfo.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = permissionInfo.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalContentColor.current.copy(alpha = 0.8f)
                )
            }

            Spacer(Modifier.width(16.dp))

            // Show Checkmark or Button
            AnimatedVisibility(visible = isGranted) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Permission Granted",
                    tint = MaterialTheme.colorScheme.primary, // Or a success color
                    modifier = Modifier.size(24.dp)
                )
            }
            AnimatedVisibility(visible = !isGranted) {
                Button(
                    onClick = onGrantClick,
                    modifier = Modifier.align(Alignment.CenterVertically) // Ensure button alignment
                ) {
                    Text("Grant")
                }
            }
        }
    }
}