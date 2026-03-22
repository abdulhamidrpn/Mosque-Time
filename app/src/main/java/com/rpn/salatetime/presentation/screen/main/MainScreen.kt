package com.rpn.salatetime.presentation.screen.main

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rpn.salatetime.R
import com.rpn.salatetime.domain.model.CompatDayOfWeek
import com.rpn.salatetime.domain.model.CompatLocalDateTime
import com.rpn.salatetime.domain.model.NotificationTrigger
import com.rpn.salatetime.domain.model.TimeState
import com.rpn.salatetime.presentation.screen.components.DynamicImage
import com.rpn.salatetime.presentation.screen.components.ImageDialog
import com.rpn.salatetime.presentation.screen.components.TvImgButton
import com.rpn.salatetime.presentation.screen.main.composable.CurrentTimeCard
import com.rpn.salatetime.presentation.screen.main.composable.WaktCard
import com.rpn.salatetime.ui.theme.SairaFontFamily
import timber.log.Timber

// ─────────────────────────────────────────────────────────────────────────────
// Responsive scale — no ssp/sdp dependency
// ─────────────────────────────────────────────────────────────────────────────

private const val REFERENCE_WIDTH = 360f

@Composable
private fun fontScale() =
    (LocalConfiguration.current.screenWidthDp / REFERENCE_WIDTH).coerceIn(0.85f, 1.30f)

@Composable
private fun Int.rsp() = (this * fontScale()).sp

// ─────────────────────────────────────────────────────────────────────────────
// Route — handles ViewModel wiring and side effects
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MainRoute(
    viewModel: MainViewModel,
    onNavigateToMessage: (NotificationTrigger) -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val timeState by viewModel.timeState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                MainScreenEffect.NavigateToSettings -> onNavigateToSettings()
                is MainScreenEffect.NavigateToMessage -> onNavigateToMessage(effect.notification)
                is MainScreenEffect.ShowToast -> Toast.makeText(
                    context,
                    effect.message,
                    Toast.LENGTH_SHORT
                ).show()

                is MainScreenEffect.Error -> { /* handle error */
                }

                MainScreenEffect.CloseMessageScreen -> {
                    Timber.d("MainScreen Return to home")
                }
            }
        }
    }

    MainScreen(
        uiState = uiState,
        timeState = timeState,
        onEvent = viewModel::onEvent,
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Screen — pure composable, receives state, emits events
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MainScreen(
    uiState: MainScreenState,
    timeState: TimeState,
    onEvent: (MainScreenEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Convenience aliases — derived from the two state objects, never recomputed
    // unless those objects change.
    val mosque = uiState.data?.mosque
    val todayPrayer = uiState.todayPrayerTime
    val dateTime = timeState.currentDateTime
    val hijriDate = timeState.hijriDateFormatted

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // ── Background image ──────────────────────────────────────────────────
        DynamicImage(
            source = uiState.selectedBackgroundImage,
            modifier = Modifier.fillMaxSize(),
            blurRadius =  0.dp,
            overlayAlpha = 0.35f,
        )

        // ── Main content ──────────────────────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom),
        ) {

            // ── Top row: Sunrise | Clock | Jumu'ah ────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                WaktCard(
                    title = stringResource(R.string.sunrise),
                    time = todayPrayer?.sunrise.orEmpty(),
                    is24Hour = uiState.is24HourFormat,
                    modifier = Modifier.weight(1f),
                )

                Box(
                    modifier = Modifier.weight(3f),
                    contentAlignment = Alignment.Center,
                ) {
                    CurrentTimeCard(
                        dateTime = dateTime,
                        hijriDate = hijriDate,
                        is24Hour = uiState.is24HourFormat,
                        showAmPm = uiState.showAmPm,
                    )
                }

                WaktCard(
                    title = stringResource(R.string.jumah),
                    time = todayPrayer?.jumah.orEmpty(),
                    is24Hour = uiState.is24HourFormat,
                    modifier = Modifier.weight(1f),
                )
            }

            // ── Five prayer times ─────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val activeNames =
                    timeState.currentNotification?.let { setOf(it.compatLocalTime.name.lowercase()) } ?: emptySet()

                listOf(
                    stringResource(R.string.fajr) to todayPrayer?.fajr.orEmpty(),
                    stringResource(R.string.dhuhr) to todayPrayer?.dhuhr.orEmpty(),
                    stringResource(R.string.asr) to todayPrayer?.asr.orEmpty(),
                    stringResource(R.string.maghrib) to todayPrayer?.maghrib.orEmpty(),
                    stringResource(R.string.isha) to todayPrayer?.isha.orEmpty(),
                ).forEach { (title, time) ->
                    WaktCard(
                        title = title,
                        time = time,
                        isActive = title.lowercase() in activeNames,
                        is24Hour = uiState.is24HourFormat,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // ── Action buttons ────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                TvImgButton(
                    icon = R.drawable.qr,
                    onClick = { onEvent(MainScreenEvent.OnQrClick) },
                )
                Spacer(Modifier.weight(1f))
                TvImgButton(
                    icon = R.drawable.logo,
                    onClick = { onEvent(MainScreenEvent.OnMoreClick) },
                )
            }

            // ── Scrolling mosque message ──────────────────────────────────────
            if (uiState.mosqueMessage.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = uiState.mosqueMessage.replace(Regex("<[^>]*>"), " ").trim(),
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .basicMarquee(iterations = Int.MAX_VALUE),
                        maxLines = 1,
                        overflow = TextOverflow.Visible,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = SairaFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.rsp(),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        ),
                    )
                }
            }
        }

        // ── Loading overlay ───────────────────────────────────────────────────
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Surface(
                    shadowElevation = 12.dp,
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(32.dp)
                            .size(64.dp),
                        strokeWidth = 6.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        // ── Notification banner ───────────────────────────────────────────────
        if (uiState.showNotificationBanner) {
            ImageDialog(
                imageRes  = R.drawable.qr,
                onDismiss = { onEvent(MainScreenEvent.DismissNotificationBanner) },
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

private val previewDateTime = CompatLocalDateTime(
    year = 2026, month = 3, dayOfMonth = 15,
    hour = 16, minute = 12, second = 45,
    dayOfWeek = CompatDayOfWeek.SUNDAY,
)

private val previewTimeState = TimeState(
    currentDateTime = previewDateTime,
    currentNotification = null,
    hijriDateFormatted = "18 Safar 1447",
)

private val previewUiState = MainScreenState(
    isLoading = false,
    selectedBackgroundImage = "https://picsum.photos/1920/1080",
    is24HourFormat = true,
    showAmPm = true,
    mosqueMessage = "Welcome to the Masjid — This is a sample scrolling message to demonstrate marquee",
)

@Preview(
    name = "TV 1080p",
    showBackground = true,
    widthDp = 1920,
    heightDp = 1080,
    device = "id:tv_1080p"
)
@Composable
private fun PreviewTv1080() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        MainScreen(previewUiState, previewTimeState, onEvent = {})
    }
}

@Preview(name = "TV 720p", showBackground = true, device = "id:tv_720p")
@Composable
private fun PreviewTv720() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        MainScreen(previewUiState, previewTimeState, onEvent = {})
    }
}

@Preview(name = "Tablet landscape", showBackground = true, widthDp = 1200, heightDp = 800)
@Composable
private fun PreviewTablet() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        MainScreen(previewUiState, previewTimeState, onEvent = {})
    }
}

@Preview(name = "Phone portrait", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun PreviewPhone() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        MainScreen(previewUiState, previewTimeState, onEvent = {})
    }
}

@Preview(name = "Loading state", showBackground = true, widthDp = 1280, heightDp = 720)
@Composable
private fun PreviewLoading() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        MainScreen(previewUiState.copy(isLoading = true), previewTimeState, onEvent = {})
    }
}