package com.rpn.salatetime.presentation.screen.setting.composable

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.rpn.salatetime.R
import com.rpn.salatetime.presentation.screen.components.DynamicImage

// ─────────────────────────────────────────────────────────────────────────────
// Storage permission — adapts to Android API level automatically
//
//  API ≥ 33 (Android 13+): READ_MEDIA_IMAGES
//  API <  33              : READ_EXTERNAL_STORAGE
// ─────────────────────────────────────────────────────────────────────────────

private val storagePermission: String
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.READ_MEDIA_IMAGES
    else
        Manifest.permission.READ_EXTERNAL_STORAGE

// ─────────────────────────────────────────────────────────────────────────────
// Public entry point
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Full-featured background image picker dialog.
 *
 * Handles:
 *  - Preset images (URLs shown in a grid).
 *  - "Pick from gallery" — requests [storagePermission] before opening the
 *    system file picker. If permanently denied, shows a rationale dialog with
 *    a "Go to Settings" button.
 *  - "Add URL" — typed URL dialog with basic http/https validation.
 *  - Selection highlight + TV D-Pad / keyboard focus / hover states.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BackgroundImagePicker(
    availableImages: List<String>,
    selectedImage: String,
    onImageSelected: (String) -> Unit,
    onImageAdded: (String) -> Unit,
    onImageRemoved: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    var showUrlDialog by remember { mutableStateOf(false) }
    var showRationaleDialog by remember { mutableStateOf(false) }

    // Storage permission state (Accompanist)
    val permissionState = rememberPermissionState(permission = storagePermission)

    // System file picker — launched after permission is granted
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.toString()?.let { onImageAdded(it) }
        },
    )

    // Called when the user taps "Pick from Gallery"
    val onPickFromGallery: () -> Unit = {
        when (permissionState.status) {
            PermissionStatus.Granted -> galleryLauncher.launch("image/*")
            is PermissionStatus.Denied -> {
                val denied = permissionState.status as PermissionStatus.Denied
                if (denied.shouldShowRationale) {
                    // First denial — ask again via permission request
                    permissionState.launchPermissionRequest()
                } else {
                    // Permanently denied — show rationale so user can open Settings
                    showRationaleDialog = true
                }
            }
        }
    }

    // ── Main picker dialog ────────────────────────────────────────────────────
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_background_image)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                Text(
                    text = stringResource(R.string.choose_background_image),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Image grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(300.dp),
                ) {
                    items(availableImages) { image ->
                        ImageTile(
                            url = image,
                            isSelected = image == selectedImage,
                            onClick = { onImageSelected(image) },
                        )
                    }
                }

                // Action row: Pick from Gallery | Add URL
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilledTonalButton(
                        onClick = onPickFromGallery,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(" Gallery", maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = { showUrlDialog = true },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            Icons.Default.Link,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(" URL", maxLines = 1)
                    }
                }

                // Show currently selected path (truncated)
                if (selectedImage.isNotBlank()) {
                    Text(
                        text = "✔ ${selectedImage.substringAfterLast("/").take(40)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        confirmButton = {
            FilledTonalButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(" ${stringResource(R.string.select)}")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )

    // ── Add URL dialog ────────────────────────────────────────────────────────
    if (showUrlDialog) {
        AddImageUrlDialog(
            onDismiss = { showUrlDialog = false },
            onImageAdded = {
                onImageAdded(it)
                showUrlDialog = false
            },
        )
    }

    // ── Permission rationale dialog ───────────────────────────────────────────
    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showRationaleDialog = false },
            title = { Text("Storage Permission Required") },
            text = {
                Text(
                    "To pick an image from your gallery, please grant storage access " +
                            "in the app settings. Tap 'Open Settings' to continue."
                )
            },
            confirmButton = {
                FilledTonalButton(onClick = {
                    showRationaleDialog = false
                    // Deep-link directly to this app's permission settings
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                    )
                }) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationaleDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Image tile — TV + phone aware
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ImageTile(
    url: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val highlighted = isFocused || isHovered || isPressed || isSelected

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
        else if (highlighted) MaterialTheme.colorScheme.secondary
        else Color.Transparent,
        animationSpec = tween(150),
        label = "tileBorder",
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else if (highlighted) 1.06f else 1f,
        animationSpec = tween(150),
        label = "tileScale",
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .size(100.dp)
            .scale(scale)
            .clip(MaterialTheme.shapes.medium)
            .border(
                width = if (highlighted) 2.dp else 0.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.medium,
            ),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(Modifier.fillMaxSize()) {
            DynamicImage(
                source = url,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            // Selection tick overlay
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(50))
                        .align(Alignment.TopEnd)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Add URL dialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AddImageUrlDialog(
    onDismiss: () -> Unit,
    onImageAdded: (String) -> Unit,
) {
    var url by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_image_url)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it; isError = false },
                    label = { Text(stringResource(R.string.url)) },
                    placeholder = { Text("https://example.com/image.jpg") },
                    isError = isError,
                    supportingText = if (isError) {
                        { Text("Enter a valid http:// or https:// URL") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                )
            }
        },
        confirmButton = {
            FilledTonalButton(onClick = {
                val trimmed = url.trim()
                if (trimmed.isNotBlank() && (trimmed.startsWith("https://") || trimmed.startsWith("http://"))) {
                    onImageAdded(trimmed)
                } else {
                    isError = true
                }
            }) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun PreviewAddUrlDialog() {
    MaterialTheme {
        AddImageUrlDialog(onDismiss = {}, onImageAdded = {})
    }
}
