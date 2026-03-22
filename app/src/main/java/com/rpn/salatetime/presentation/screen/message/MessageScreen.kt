package com.rpn.salatetime.presentation.screen.message

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rpn.salatetime.domain.model.NotificationTrigger
import com.rpn.salatetime.domain.model.TimeState
import com.rpn.salatetime.presentation.screen.components.DynamicImage
import com.rpn.salatetime.presentation.screen.components.SquareProgressIndicator
import com.rpn.salatetime.presentation.screen.main.MainScreenState
import com.rpn.salatetime.presentation.screen.main.MainViewModel
import com.rpn.salatetime.presentation.screen.message.composable.CurrentTimeCardLite
import timber.log.Timber

// ─────────────────────────────────────────────────────────────────────────────
// Route
// ─────────────────────────────────────────────────────────────────────────────

/*@Composable
fun MessageRoute(
    viewModel: MainViewModel,
    notificationTrigger: NotificationTrigger,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val timeState by viewModel.timeState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect    { effect ->
            when (effect) {
                MainScreenEffect.CloseMessageScreen -> {
                    Timber.d("Message Return to home")
                    onBack()
                }

                else -> {}
            }
        }
    }
    MessageScreen(
        notificationTrigger = notificationTrigger,
        uiState = uiState,
        timeState = timeState,
    )
}*/


@Composable
fun MessageRoute(
    viewModel: MainViewModel,
    notificationTrigger: NotificationTrigger,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val timeState by viewModel.timeState.collectAsStateWithLifecycle()

    val enteredMinute = remember { timeState.currentDateTime.minute }

    LaunchedEffect(timeState.currentDateTime.minute) {
        Timber.d("MessageRoute:" +
                "\nEntered Minute: $enteredMinute" +
                "\nCurrent Minute: ${timeState.currentDateTime.minute}")

        if (timeState.currentDateTime.minute != enteredMinute) onBack()
    }
    // ── Render the message screen with live state ──────────────────────────
    MessageScreen(
        notificationTrigger = notificationTrigger,
        uiState = uiState,
        timeState = timeState,
    )
}
// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MessageScreen(
    notificationTrigger: NotificationTrigger,
    uiState: MainScreenState,
    timeState: TimeState,
) {
    val dateTime = timeState.currentDateTime
    val hijriDate = timeState.hijriDateFormatted

    val countdown = dateTime.countDownSeconds()                  // 0–59
    val progress = (countdown / 59f).coerceIn(0f, 1f)

    val imagePath = notificationTrigger.imageMessage

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {

        // ── Background image ────────────────────────────────────────────────
        // Resolves local paths (starting with /) to File objects, remote URLs to URLs
        DynamicImage(
            source = imagePath,
            modifier = Modifier.fillMaxSize(),
            blurRadius = 0.dp,
            overlayAlpha = 0.35f,
        )

        // ── Clock + notification info ───────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom),
        ) {
            CurrentTimeCardLite(
                // FIX 3: was `notificationTrigger.compatLocalTime` — that is the
                // *prayer's* scheduled time (static), not the live wall-clock.
                // `dateTime.toTime()` gives the current H:M:S from the ticker.
                time = dateTime.toLocalTime(),
                weekday = dateTime.dayOfWeek.name
                    .lowercase()
                    .replaceFirstChar { it.uppercase() },
                gregorianDate = dateTime.toFormattedDate(),
                hijriDate = hijriDate,
                notification = notificationTrigger,
                is24HourFormat = uiState.is24HourFormat,
                showAmPm = uiState.showAmPm,
            )
        }

        // ── Seconds ring ────────────────────────────────────────────────────
        // Animates from full → empty over each 60-second cycle.
        SquareProgressIndicator(
            progress = progress,
            strokeWidth = 6.dp,
            progressColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            trackColor = Color.Transparent,
            animationSpec = tween(durationMillis = 250),
        )
    }
}