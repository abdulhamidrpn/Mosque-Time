package com.rpn.salatetime.presentation.screen.setting.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.rpn.salatetime.R

/**
 * A TV and mobile friendly time picker.
 *
 * - Touch: tap the dropdown, scroll the list, tap a value.
 * - D-Pad / keyboard: the dropdown opens on Select/Enter; arrow keys scroll
 *   the LazyColumn; Select/Enter confirms.
 * - Auto-scrolls to the current hour/minute when each dropdown opens.
 * - Old versions (TimePickerDialogOld, TimePickerDialogOld2) removed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    title: String,
    currentTime: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedHour by remember {
        mutableIntStateOf(currentTime.split(":").getOrNull(0)?.toIntOrNull() ?: 0)
    }
    var selectedMinute by remember {
        mutableIntStateOf(currentTime.split(":").getOrNull(1)?.toIntOrNull() ?: 0)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.Top,
            ) {
                TimeDropdown(
                    label = stringResource(R.string.hour),
                    values = (0..23).toList(),
                    selectedValue = selectedHour,
                    onValueChange = { selectedHour = it },
                    modifier = Modifier.weight(1f),
                )

                Text(
                    text = ":",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(top = 36.dp),
                )

                TimeDropdown(
                    label = stringResource(R.string.minute),
                    values = (0..59).toList(),
                    selectedValue = selectedMinute,
                    onValueChange = { selectedMinute = it },
                    modifier = Modifier.weight(1f),
                )
            }
        },
        confirmButton = {
            FilledTonalButton(onClick = {
                onTimeSelected("%02d:%02d".format(selectedHour, selectedMinute))
                onDismiss()
            }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

/**
 * A single hour or minute dropdown column.
 * Scrolls to the selected item automatically when opened.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeDropdown(
    label: String,
    values: List<Int>,
    selectedValue: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Auto-scroll to selected item when the dropdown opens
    LaunchedEffect(expanded) {
        if (expanded) {
            val index = values.indexOf(selectedValue).coerceAtLeast(0)
            withFrameNanos { } // wait one frame for LazyColumn to compose
            listState.animateScrollToItem(index)
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            TextField(
                value = "%02d".format(selectedValue),
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                singleLine = true,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .width(120.dp),
                ) {
                    LazyColumn(state = listState) {
                        items(values) { value ->
                            DropdownMenuItem(
                                text = { Text("%02d".format(value)) },
                                onClick = {
                                    onValueChange(value)
                                    expanded = false
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun PreviewTimePicker() {
    MaterialTheme {
        TimePickerDialog(
            title = "Set Fajr Time",
            currentTime = "05:30",
            onTimeSelected = {},
            onDismiss = {},
        )
    }
}
