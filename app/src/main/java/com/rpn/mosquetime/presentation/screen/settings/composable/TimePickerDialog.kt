package com.rpn.mosquetime.presentation.screen.settings.composable


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rpn.mosquetime.R
import com.rpn.mosquetime.presentation.common.TvButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    title: String,
    currentTime: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var expandedHour by remember { mutableStateOf(false) }
    var expandedMinute by remember { mutableStateOf(false) }
    var selectedHour by remember {
        mutableIntStateOf(
            currentTime.split(":").getOrNull(0)?.toIntOrNull() ?: 0
        )
    }
    var selectedMinute by remember {
        mutableIntStateOf(
            currentTime.split(":").getOrNull(1)?.toIntOrNull() ?: 0
        )
    }

    val hours = (0..23).toList()
    val minutes = (0..59).toList()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hour Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.hour),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        ExposedDropdownMenuBox(
                            expanded = expandedHour,
                            onExpandedChange = { expandedHour = !expandedHour }
                        ) {
                            TextField(
                                value = String.format("%02d", selectedHour),
                                onValueChange = { },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedHour) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedHour,
                                onDismissRequest = { expandedHour = false }
                            ) {
                                val listState = rememberLazyListState()

                                LaunchedEffect(expandedHour, selectedHour) {
                                    if (expandedHour) {
                                        val index = hours.indexOf(selectedHour)
                                        if (index >= 0) {
                                            // wait one frame so LazyColumn is composed
                                            withFrameNanos { }
                                            listState.animateScrollToItem(index)
                                        }
                                    }
                                }


                                // ✅ Wrap LazyColumn in Box with fixed height
                                Box(
                                    modifier = Modifier
                                        .height(200.dp)
                                        .width(100.dp)
                                ) {
                                    LazyColumn(state = listState) {
                                        items(hours) { hour ->
                                            DropdownMenuItem(
                                                text = { Text(String.format("%02d", hour)) },
                                                onClick = {
                                                    selectedHour = hour
                                                    expandedHour = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Text(":", modifier = Modifier.padding(horizontal = 16.dp))

                // Minute Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.minute),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        ExposedDropdownMenuBox(
                            expanded = expandedMinute,
                            onExpandedChange = { expandedMinute = !expandedMinute }
                        ) {
                            TextField(
                                value = String.format("%02d", selectedMinute),
                                onValueChange = { },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMinute) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedMinute,
                                onDismissRequest = { expandedMinute = false }
                            ) {

                                val listState = rememberLazyListState()

                                LaunchedEffect(expandedMinute, selectedMinute) {
                                    if (expandedMinute) {
                                        val index = minutes.indexOf(selectedMinute)
                                        if (index >= 0) {
                                            // wait one frame so LazyColumn is composed
                                            withFrameNanos { }
                                            listState.animateScrollToItem(index)
                                        }
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .height(200.dp)
                                        .width(100.dp)
                                ) {
                                    LazyColumn(state = listState) {
                                        items(minutes) { minute ->
                                            DropdownMenuItem(
                                                text = { Text(String.format("%02d", minute)) },
                                                onClick = {
                                                    selectedMinute = minute
                                                    expandedMinute = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
        },
        confirmButton = {
            TvButton(
                text = stringResource(R.string.ok),
                icon = Icons.Default.Check,
                modifier = Modifier.padding(0.dp),
                onClick = {
                    val timeString = String.format("%02d:%02d", selectedHour, selectedMinute)
                    onTimeSelected(timeString)
                    onDismiss()
                }
            )
        },
        dismissButton = {
            TvButton(
                text = stringResource(R.string.cancel),
                icon = Icons.Default.Close,
                modifier = Modifier.padding(0.dp),
                onClick = onDismiss
            )
        }
    )
}

@Preview
@Composable
private fun PreviewTimePickerDialog() {
    TimePickerDialog(
        title = "Time Pick",
        currentTime = "15:30",
        onTimeSelected = {},
        onDismiss = {}
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialogOld2(
    title: String,
    currentTime: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var expandedHour by remember { mutableStateOf(false) }
    var expandedMinute by remember { mutableStateOf(false) }
    var selectedHour by remember {
        mutableIntStateOf(
            currentTime.split(":").getOrNull(0)?.toIntOrNull() ?: 0
        )
    }
    var selectedMinute by remember {
        mutableIntStateOf(
            currentTime.split(":").getOrNull(1)?.toIntOrNull() ?: 0
        )
    }

    val hours = (0..23).toList()
    val minutes = (0..59).toList()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hour Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.hour),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        ExposedDropdownMenuBox(
                            expanded = expandedHour,
                            onExpandedChange = { expandedHour = !expandedHour }
                        ) {
                            TextField(
                                value = String.format("%02d", selectedHour),
                                onValueChange = { },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedHour) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedHour,
                                onDismissRequest = { expandedHour = false }
                            ) {
                                val listState = rememberLazyListState()
                                LaunchedEffect(expandedHour) {
                                    if (expandedHour) {
                                        val index = hours.indexOf(selectedHour)
                                        if (index >= 0) {
                                            listState.scrollToItem(index)
                                        }
                                    }
                                }
                                LazyColumn(state = listState) {
                                    items(hours.size) { index ->
                                        val hour = hours[index]
                                        DropdownMenuItem(
                                            text = { Text(String.format("%02d", hour)) },
                                            onClick = {
                                                selectedHour = hour
                                                expandedHour = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Text(":", modifier = Modifier.padding(horizontal = 16.dp))

                // Minute Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.minute),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        ExposedDropdownMenuBox(
                            expanded = expandedMinute,
                            onExpandedChange = { expandedMinute = !expandedMinute }
                        ) {
                            TextField(
                                value = String.format("%02d", selectedMinute),
                                onValueChange = { },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMinute) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedMinute,
                                onDismissRequest = { expandedMinute = false }
                            ) {
                                val listState = rememberLazyListState()
                                LaunchedEffect(expandedMinute) {
                                    if (expandedMinute) {
                                        val index = minutes.indexOf(selectedMinute)
                                        if (index >= 0) {
                                            listState.scrollToItem(index)
                                        }
                                    }
                                }
                                LazyColumn(state = listState) {
                                    items(minutes.size) { index ->
                                        val minute = minutes[index]
                                        DropdownMenuItem(
                                            text = { Text(String.format("%02d", minute)) },
                                            onClick = {
                                                selectedMinute = minute
                                                expandedMinute = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TvButton(
                text = stringResource(R.string.ok),
                icon = Icons.Default.Check,
                modifier = Modifier.padding(0.dp),
                onClick = {
                    val timeString = String.format("%02d:%02d", selectedHour, selectedMinute)
                    onTimeSelected(timeString)
                    onDismiss()
                }
            )
        },
        dismissButton = {
            TvButton(
                text = stringResource(R.string.cancel),
                icon = Icons.Default.Close,
                modifier = Modifier.padding(0.dp),
                onClick = onDismiss
            )
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialogOld(
    title: String,
    currentTime: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var expandedHour by remember { mutableStateOf(false) }
    var expandedMinute by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableIntStateOf(currentTime.split(":")[0].toIntOrNull() ?: 0) }
    var selectedMinute by remember {
        mutableIntStateOf(
            currentTime.split(":")[1].toIntOrNull() ?: 0
        )
    }

    val hours = (0..23).toList()
    val minutes = (0..59).toList()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hour Dropdown
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.hour), modifier = Modifier.padding(bottom = 8.dp))
                    ExposedDropdownMenuBox(
                        expanded = expandedHour,
                        onExpandedChange = { expandedHour = !expandedHour }
                    ) {
                        TextField(
                            value = String.format("%02d", selectedHour),
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedHour) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedHour,
                            onDismissRequest = { expandedHour = false }
                        ) {
                            hours.forEach { hour ->
                                DropdownMenuItem(
                                    text = { Text(String.format("%02d", hour)) },
                                    onClick = {
                                        selectedHour = hour
                                        expandedHour = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))
                Text(":", modifier = Modifier.padding(horizontal = 16.dp))

                // Minute Dropdown
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(R.string.minute),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    ExposedDropdownMenuBox(
                        expanded = expandedMinute,
                        onExpandedChange = { expandedMinute = !expandedMinute }
                    ) {
                        TextField(
                            value = String.format("%02d", selectedMinute),
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMinute) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedMinute,
                            onDismissRequest = { expandedMinute = false }
                        ) {
                            minutes.forEach { minute ->
                                DropdownMenuItem(
                                    text = { Text(String.format("%02d", minute)) },
                                    onClick = {
                                        selectedMinute = minute
                                        expandedMinute = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val timeString = String.format("%02d:%02d", selectedHour, selectedMinute)
                    onTimeSelected(timeString)
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}









