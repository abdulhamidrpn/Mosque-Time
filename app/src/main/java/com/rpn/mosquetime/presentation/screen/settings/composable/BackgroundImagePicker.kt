package com.rpn.mosquetime.presentation.screen.settings.composable

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import coil.compose.AsyncImage
import com.rpn.mosquetime.R
import com.rpn.mosquetime.presentation.common.TvButton

@Composable
fun BackgroundImagePicker(
    availableImages: List<String>,
    selectedImage: String,
    onImageSelected: (String) -> Unit,
    onImageAdded: (String) -> Unit,
    onImageRemoved: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showAddImageDialog by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            result.data?.data?.let {
                onImageAdded(it.toString())
            }
        }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_background_image)) },
        text = {
            Column {
                Text(stringResource(R.string.choose_background_image))
                Spacer(modifier = Modifier.height(16.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableImages) { image ->
                        Surface(
                            modifier = Modifier.size(100.dp),
                            onClick = { onImageSelected(image) },
                            shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.medium),
                            colors = ClickableSurfaceDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                pressedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                focusedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                pressedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                containerColor = Color.Transparent,
                            ),
                            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
                        ) {
                            AsyncImage(
                                model = image,
                                contentDescription = stringResource(R.string.background_image),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    item {
                        Surface(
                            modifier = Modifier.size(100.dp),
                            onClick = { showAddImageDialog = true },
                            shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.medium),
                            colors = ClickableSurfaceDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                pressedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                focusedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                pressedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                containerColor = Color.Transparent,
                            ),
                            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text("+", fontSize = 40.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.selected_image, selectedImage))
            }
        },
        confirmButton = {
            TvButton(
                text = stringResource(R.string.select),
                icon = Icons.Default.Check,
                modifier = Modifier.padding(0.dp),
                onClick = onDismiss
            )
        },
        dismissButton = {
            TvButton(
                text = stringResource(R.string.cancel),
                icon = Icons.Default.Cancel,
                modifier = Modifier.padding(0.dp),
                onClick = onDismiss
            )
        }
    )
    AddImageUrlDialog(
        showDialog = showAddImageDialog,
        onDismiss = {
            showAddImageDialog = false
        },
        onImageAdded = { imageUrl ->
            onImageAdded(imageUrl)
        }

    )
}

@Composable
fun AddImageUrlDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onImageAdded: (String) -> Unit
) {
    if (!showDialog) return

    var url by remember { mutableStateOf("") }

    // focus requesters
    val textFieldRequester = remember { FocusRequester() }
    val addButtonRequester = remember { FocusRequester() }
    val cancelButtonRequester = remember { FocusRequester() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_image_url)) },
        text = {
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text(stringResource(R.string.url)) },
                modifier = Modifier
                    .focusRequester(textFieldRequester)
                    .focusProperties {
                        // when pressing ↓, go to Add button
                        down = addButtonRequester
                    }
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (url.isNotBlank() && (url.startsWith("https://") || url.startsWith("http://"))) {
                        onImageAdded(url)
                        onDismiss()
                    }
                }
            ) {
                Text(text = stringResource(R.string.add))
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )

    // Request focus on the text field when dialog opens
    LaunchedEffect(Unit) {
        textFieldRequester.requestFocus()
    }
}