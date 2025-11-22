package com.rpn.mosquetime.presentation.screen.settings

import android.widget.Toast
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import com.rpn.mosquetime.R
import com.rpn.mosquetime.presentation.common.CachedImage
import com.rpn.mosquetime.presentation.common.TvButton
import com.rpn.mosquetime.presentation.screen.settings.composable.BackgroundImagePicker
import com.rpn.mosquetime.presentation.screen.settings.composable.Theme
import com.rpn.mosquetime.presentation.screen.settings.composable.TimePickerDialog
import com.rpn.mosquetime.presentation.theme.AppIcons
import com.rpn.mosquetime.presentation.theme.MosqueTimeTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onChange: () -> Unit,
    navigateToLoginPage: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val uiEffect by viewModel.uiEffect.collectAsState(initial = null)

    var showTimePicker by remember { mutableStateOf<String?>(null) }
    var showImagePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.onEvent(SettingsUIEvent.LoadBackgroundImages)
    }

    LaunchedEffect(uiEffect) {
        uiEffect?.let { effect ->
            when (effect) {
                is SettingsUIEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }

                is SettingsUIEffect.NavigateToLogin -> {
                    navigateToLoginPage()
                }

                is SettingsUIEffect.NavigateToHome -> {
                    // TODO: Navigate to home
                }

                is SettingsUIEffect.ShowImagePicker -> {
                    showImagePicker = true
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            TvButton(
                text = "Back to Home",
                icon = Icons.Default.ArrowBackIosNew,
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            )

            Text(
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Theme Settings
        SettingsCard(title = stringResource(R.string.theme)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Theme.values().forEach { theme ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = uiState.theme == theme,
                            onClick = { viewModel.onEvent(SettingsUIEvent.SelectTheme(theme)) }
                        )
                        Text(text = theme.name)
                    }
                }
            }
        }

        // Time Format Settings
        SettingsCard(title = stringResource(R.string.basic)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.show_mosque_name))
                Switch(
                    checked = uiState.showMosqueName,
                    onCheckedChange = { viewModel.onEvent(SettingsUIEvent.ToggleShowMosqueName(it)) },
                )
            }
        }

        // Time Format Settings
        SettingsCard(title = stringResource(R.string.time_format)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.twenty_four_hour_format))
                Switch(
                    checked = uiState.is12HourFormat,
                    onCheckedChange = { viewModel.onEvent(SettingsUIEvent.ToggleTimeFormat(!it)) },
                )
            }
        }

        // Default Prayer Times
        SettingsCard(title = stringResource(R.string.default_prayer_times)) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PrayerTimeRow(
                    label = stringResource(R.string.fajr),
                    time = uiState.defaultFajrTime,
                    onTimeClick = { showTimePicker = context.getString(R.string.fajr) }
                )
                PrayerTimeRow(
                    label = stringResource(R.string.dhuhr),
                    time = uiState.defaultDhuhrTime,
                    onTimeClick = { showTimePicker = context.getString(R.string.dhuhr) }
                )
                PrayerTimeRow(
                    label = stringResource(R.string.asr),
                    time = uiState.defaultAsrTime,
                    onTimeClick = { showTimePicker = context.getString(R.string.asr) }
                )
                PrayerTimeRow(
                    label = stringResource(R.string.maghrib),
                    time = uiState.defaultMaghribTime,
                    onTimeClick = { showTimePicker = context.getString(R.string.maghrib) }
                )
                PrayerTimeRow(
                    label = stringResource(R.string.isha),
                    time = uiState.defaultIshaTime,
                    onTimeClick = { showTimePicker = context.getString(R.string.isha) }
                )
                PrayerTimeRow(
                    label = stringResource(R.string.jumah),
                    time = uiState.defaultJumahTime,
                    onTimeClick = { showTimePicker = context.getString(R.string.jumah) }
                )
                PrayerTimeRow(
                    label = stringResource(R.string.sunrise),
                    time = uiState.defaultSunriseTime,
                    onTimeClick = { showTimePicker = context.getString(R.string.sunrise) }
                )
            }
        }

        // Background Image
        SettingsCard(title = stringResource(R.string.background_image)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CachedImage(
                    url = uiState.selectedBackgroundImage,
                    modifier = Modifier
                        .heightIn(max = 150.dp)  // limit height
                        .aspectRatio(16 / 9f)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.FillHeight
                )
                Spacer(modifier = Modifier.weight(1f))
                TvButton(
                    text = stringResource(R.string.change_background_image),
                    icon = AppIcons.Image,
                    onClick = { showImagePicker = true }
                )
            }
        }

        // Notifications
        SettingsCard(title = stringResource(R.string.notifications)) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.pre_prayer_notifications))
                    Switch(
                        checked = uiState.enablePrePrayerNotification,
                        onCheckedChange = {
                            viewModel.onEvent(
                                SettingsUIEvent.TogglePrePrayerNotification(
                                    it
                                )
                            )
                        }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.post_prayer_notifications))
                    Switch(
                        checked = uiState.enablePostPrayerNotification,
                        onCheckedChange = {
                            viewModel.onEvent(
                                SettingsUIEvent.TogglePostPrayerNotification(
                                    it
                                )
                            )
                        }
                    )
                }
            }
        }

        // Login and Sync
        SettingsCard(title = stringResource(R.string.account_and_sync)) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (uiState.isLoggedIn) {
                    Text(stringResource(R.string.logged_in_as, uiState.userEmail))
                    Text(uiState.userEmail)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TvButton(
                            text = stringResource(R.string.sync_data),
                            icon = AppIcons.Sync,
                            onClick = { viewModel.onEvent(SettingsUIEvent.SyncData) },
                            modifier = Modifier.weight(1f)
                        )
                        TvButton(
                            text = stringResource(R.string.logout),
                            icon = AppIcons.Logout,
                            onClick = { viewModel.onEvent(SettingsUIEvent.Logout) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Text(stringResource(R.string.not_logged_in))

                    TvButton(
                        text = stringResource(R.string.login_to_sync_data),
                        icon = AppIcons.Login,
                        onClick = { viewModel.onEvent(SettingsUIEvent.Login) },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }

        // Back Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.back_to_main_screen))
            }
        }
    }

    // Time Picker Dialog
    showTimePicker?.let { prayer ->
        TimePickerDialog(
            title = stringResource(R.string.set_prayer_time, prayer),
            currentTime = when (prayer) {
                stringResource(R.string.fajr) -> uiState.defaultFajrTime
                stringResource(R.string.dhuhr) -> uiState.defaultDhuhrTime
                stringResource(R.string.asr) -> uiState.defaultAsrTime
                stringResource(R.string.maghrib) -> uiState.defaultMaghribTime
                stringResource(R.string.isha) -> uiState.defaultIshaTime
                else -> "00:00"
            },
            onTimeSelected = { time ->
                viewModel.onEvent(SettingsUIEvent.UpdateDefaultPrayerTime(prayer, time))
            },
            onDismiss = { showTimePicker = null }
        )
    }

    // Background Image Picker Dialog
    if (showImagePicker) {
        BackgroundImagePicker(
            availableImages = uiState.availableBackgroundImages,
            selectedImage = uiState.selectedBackgroundImage,
            onImageSelected = { imagePath ->
                viewModel.onEvent(SettingsUIEvent.SelectBackgroundImage(imagePath))
            },
            onImageAdded = { url ->
                viewModel.onEvent(SettingsUIEvent.SelectBackgroundImage(url))
            },
            onImageRemoved = {},
            onDismiss = { showImagePicker = false }
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Divider()
            content()
        }
    }
}



@Composable
private fun PrayerTimeRow(
    label: String,
    time: String,
    onTimeClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val focusRequester = remember { FocusRequester() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        onClick = onTimeClick,
        shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.medium),
        colors = ClickableSurfaceDefaults.colors(

            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            pressedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            focusedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            pressedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = Color.Transparent,
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        interactionSource = interactionSource
    ) {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label)
            Text(time, fontWeight = FontWeight.Medium)
        }
    }

}

@Preview(device = "id:tv_1080p", showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    MosqueTimeTheme {
        SettingsScreen(
            onBack = {},
            onChange = {}
        )
    }
}
