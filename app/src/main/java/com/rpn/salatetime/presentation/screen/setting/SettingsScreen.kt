package com.rpn.salatetime.presentation.screen.setting

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rpn.salatetime.R
import com.rpn.salatetime.domain.model.Theme
import com.rpn.salatetime.presentation.screen.components.DynamicImage
import com.rpn.salatetime.presentation.screen.setting.composable.BackgroundImagePicker
import com.rpn.salatetime.presentation.screen.setting.composable.TimePickerDialog
import org.koin.androidx.compose.koinViewModel

// ─────────────────────────────────────────────────────────────────────────────
// Responsive scale — consistent with AppTypography and MainScreen
// ─────────────────────────────────────────────────────────────────────────────

private const val REFERENCE_WIDTH = 360f

@Composable
private fun fontScale() =
    (LocalConfiguration.current.screenWidthDp / REFERENCE_WIDTH).coerceIn(0.85f, 1.30f)

@Composable
private fun Int.rsp() = (this * fontScale()).sp

// ─────────────────────────────────────────────────────────────────────────────
// Screen entry point
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    navigateToLogin: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showTimePicker by remember { mutableStateOf<String?>(null) }
    var showImagePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.onEvent(SettingsUIEvent.LoadBackgroundImages)
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is SettingsUIEffect.ShowToast -> Toast.makeText(
                    context,
                    effect.message,
                    Toast.LENGTH_SHORT
                ).show()

                is SettingsUIEffect.NavigateToLogin -> navigateToLogin()
                is SettingsUIEffect.NavigateToHome -> onBack()
                is SettingsUIEffect.ShowImagePicker -> showImagePicker = true
            }
        }
    }

    SettingsScreen(
        uiState = uiState,
        onBack = onBack,
        onEvent = viewModel::onEvent,
        onShowTimePicker = { showTimePicker = it },
        onShowImagePicker = { showImagePicker = true },
    )

    // ── Dialogs ───────────────────────────────────────────────────────────────

    showTimePicker?.let { prayer ->
        val currentTime = uiState.prayerTimeFor(prayer)
        TimePickerDialog(
            title = stringResource(R.string.set_prayer_time, prayer),
            currentTime = currentTime,
            onTimeSelected = {
                viewModel.onEvent(
                    SettingsUIEvent.UpdateDefaultPrayerTime(
                        prayer,
                        it
                    )
                )
            },
            onDismiss = { showTimePicker = null },
        )
    }

    if (showImagePicker) {
        BackgroundImagePicker(
            availableImages = uiState.availableBackgroundImages,
            selectedImage = uiState.selectedBackgroundImage,
            onImageSelected = { viewModel.onEvent(SettingsUIEvent.SelectBackgroundImage(it)) },
            onImageAdded = { viewModel.onEvent(SettingsUIEvent.SelectBackgroundImage(it)) },
            onImageRemoved = {},
            onDismiss = { showImagePicker = false },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pure content — previewable with no ViewModel dependency
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SettingsScreen(
    uiState: SettingsUIState,
    onBack: () -> Unit,
    onEvent: (SettingsUIEvent) -> Unit,
    onShowTimePicker: (String) -> Unit,
    onShowImagePicker: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.settings),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                )
            }

            // ── Theme ─────────────────────────────────────────────────────────
            SettingsCard(stringResource(R.string.theme)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup(),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    Theme.entries.forEach { theme ->
                        LabeledRadio(
                            label = theme.name.lowercase().replaceFirstChar { it.uppercase() },
                            selected = uiState.theme == theme,
                            onClick = { onEvent(SettingsUIEvent.SelectTheme(theme)) },
                        )
                    }
                }
            }

            // ── Language ──────────────────────────────────────────────────────
            SettingsCard(stringResource(R.string.language)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    listOf(
                        "en" to "English",
                        "fr" to "Français",
                        "bn" to "বাংলা"
                    ).forEach { (code, label) ->
                        LabeledRadio(
                            label = label,
                            selected = uiState.selectedLanguage == code,
                            onClick = { onEvent(SettingsUIEvent.SelectLanguage(code)) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // ── Display ───────────────────────────────────────────────────────
            SettingsCard(stringResource(R.string.basic)) {
                SwitchRow(
                    label = stringResource(R.string.show_mosque_name),
                    checked = uiState.showMosqueName,
                    onCheckedChange = { onEvent(SettingsUIEvent.ToggleShowMosqueName(it)) },
                )
            }

            // ── Time format ───────────────────────────────────────────────────
            SettingsCard(stringResource(R.string.time_format)) {
                SwitchRow(
                    label = stringResource(R.string.twenty_four_hour_format),
                    checked = uiState.is12HourFormat,   // is12Hour=true means 24h is OFF
                    onCheckedChange = { onEvent(SettingsUIEvent.ToggleTimeFormat(it)) },
                )
            }

            // ── Default prayer times ──────────────────────────────────────────
            SettingsCard(stringResource(R.string.default_prayer_times)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(
                        stringResource(R.string.fajr) to uiState.defaultFajrTime,
                        stringResource(R.string.sunrise) to uiState.defaultSunriseTime,
                        stringResource(R.string.dhuhr) to uiState.defaultDhuhrTime,
                        stringResource(R.string.asr) to uiState.defaultAsrTime,
                        stringResource(R.string.maghrib) to uiState.defaultMaghribTime,
                        stringResource(R.string.isha) to uiState.defaultIshaTime,
                        stringResource(R.string.jumah) to uiState.defaultJumahTime,
                    ).forEach { (label, time) ->
                        PrayerTimeRow(
                            label = label,
                            time = time,
                            onTimeClick = { onShowTimePicker(label) },
                        )
                    }
                }
            }

            // ── Background image ──────────────────────────────────────────────
            SettingsCard(stringResource(R.string.background_image)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DynamicImage(
                        source = uiState.selectedBackgroundImage.ifBlank { null },
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .heightIn(max = 120.dp)
                            .aspectRatio(16 / 9f)
                            .clip(MaterialTheme.shapes.medium),
                    )
                    Spacer(Modifier.weight(1f))
                    SettingsButton(
                        text = stringResource(R.string.change_background_image),
                        icon = Icons.Default.Image,
                        onClick = onShowImagePicker,
                    )
                }
            }

            // ── Notifications ─────────────────────────────────────────────────
            SettingsCard(stringResource(R.string.notifications)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SwitchRow(
                        label = stringResource(R.string.pre_prayer_notifications),
                        checked = uiState.enablePrePrayerNotification,
                        onCheckedChange = { onEvent(SettingsUIEvent.TogglePrePrayerNotification(it)) },
                    )
                    SwitchRow(
                        label = stringResource(R.string.post_prayer_notifications),
                        checked = uiState.enablePostPrayerNotification,
                        onCheckedChange = { onEvent(SettingsUIEvent.TogglePostPrayerNotification(it)) },
                    )
                }
            }

            // ── Account ───────────────────────────────────────────────────────
            SettingsCard(stringResource(R.string.account_and_sync)) {
                if (uiState.isLoggedIn) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Profile info card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                if (uiState.mosqueName.isNotBlank()) {
                                    InfoField(
                                        label = stringResource(R.string.mosque),
                                        value = uiState.mosqueName
                                    )
                                }
                                InfoField(
                                    label = stringResource(R.string.email),
                                    value = uiState.userEmail
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            SettingsButton(
                                text = stringResource(R.string.sync_data),
                                icon = Icons.Default.Sync,
                                onClick = { onEvent(SettingsUIEvent.SyncData) },
                                modifier = Modifier.weight(1f),
                            )
                            SettingsButton(
                                text = stringResource(R.string.logout),
                                icon = Icons.Default.Logout,
                                onClick = { onEvent(SettingsUIEvent.Logout) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            stringResource(R.string.not_logged_in),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        SettingsButton(
                            text = stringResource(R.string.login_to_sync_data),
                            icon = Icons.Default.Sync,
                            onClick = { onEvent(SettingsUIEvent.Login) },
                        )
                    }
                }
            }

            // ── Footer ────────────────────────────────────────────────────────
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                TextButton(onClick = onBack) {
                    Text(stringResource(R.string.back_to_main_screen))
                }
            }
        }

        // ── Loading overlay ───────────────────────────────────────────────────
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared components
// ─────────────────────────────────────────────────────────────────────────────

/** Card container used for every settings section. */
@Composable
private fun SettingsCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            HorizontalDivider()
            content()
        }
    }
}

/**
 * A single prayer-time row.
 *
 * Handles touch, hover, and D-Pad/keyboard focus independently using
 * [MutableInteractionSource] so it works on both TV and phone.
 */
@Composable
private fun PrayerTimeRow(
    label: String,
    time: String,
    onTimeClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val isHighlighted = isFocused || isHovered || isPressed

    val bgColor by animateColorAsState(
        targetValue = if (isHighlighted) MaterialTheme.colorScheme.primaryContainer
        else Color.Transparent,
        animationSpec = tween(150),
        label = "prayerRowBg",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(bgColor)
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = MaterialTheme.shapes.medium,
            )
            .selectable(
                selected = false,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onTimeClick,
            )
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isHighlighted) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = time,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (isHighlighted) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** A labelled [Switch] row usable on TV and phone. */
@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/** A [RadioButton] + label pair. Focus-aware via [Modifier.focusable]. */
@Composable
private fun LabeledRadio(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.selectable(
            selected = selected,
            role = Role.RadioButton,
            onClick = onClick
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

/** TV-aware button: hovers, focuses, and scales. Works on phone too. */
@Composable
private fun SettingsButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale = when {
        isPressed -> 0.94f
        isFocused || isHovered -> 1.05f
        else -> 1f
    }

    FilledTonalButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier.scale(scale),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(text)
    }
}

/** Two-line label + value display used in the account card. */
@Composable
private fun InfoField(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helper: resolve time string for a given prayer label
// ─────────────────────────────────────────────────────────────────────────────

private fun SettingsUIState.prayerTimeFor(prayer: String): String = when (prayer.lowercase()) {
    "fajr" -> defaultFajrTime
    "dhuhr" -> defaultDhuhrTime
    "asr" -> defaultAsrTime
    "maghrib" -> defaultMaghribTime
    "isha" -> defaultIshaTime
    "jumah" -> defaultJumahTime
    "sunrise" -> defaultSunriseTime
    else -> "00:00"
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

private val previewState = SettingsUIState(
    isLoading = false,
    isLoggedIn = true,
    userEmail = "admin@mosque.com",
    mosqueName = "Al-Aqsa Mosque",
    selectedBackgroundImage = "https://picsum.photos/400/225",
    theme = Theme.SYSTEM,
    selectedLanguage = "en",
)

@Preview(name = "TV 1080p", device = "id:tv_1080p", showBackground = true)
@Composable
private fun PreviewTv() {
    MaterialTheme(lightColorScheme()) {
        SettingsScreen(
            uiState = previewState,
            onBack = {},
            onEvent = {},
            onShowTimePicker = {},
            onShowImagePicker = {},
        )
    }
}

@Preview(name = "Phone", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewPhone() {
    MaterialTheme(lightColorScheme()) {
        SettingsScreen(
            uiState = previewState.copy(isLoggedIn = false),
            onBack = {},
            onEvent = {},
            onShowTimePicker = {},
            onShowImagePicker = {},
        )
    }
}

@Preview(name = "Loading", showBackground = true, widthDp = 360)
@Composable
private fun PreviewLoading() {
    MaterialTheme(lightColorScheme()) {
        SettingsScreen(
            uiState = previewState.copy(isLoading = true),
            onBack = {},
            onEvent = {},
            onShowTimePicker = {},
            onShowImagePicker = {},
        )
    }
}
