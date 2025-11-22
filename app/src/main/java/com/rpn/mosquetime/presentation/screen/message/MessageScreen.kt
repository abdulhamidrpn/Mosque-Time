package com.rpn.mosquetime.presentation.screen.message

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rpn.mosquetime.R
import com.rpn.mosquetime.domain.model.time.CompatLocalTime
import com.rpn.mosquetime.presentation.common.CachedImage
import com.rpn.mosquetime.presentation.common.SquareProgressIndicator
import com.rpn.mosquetime.presentation.screen.main.MainNotification
import com.rpn.mosquetime.presentation.screen.main.MainScreenState
import com.rpn.mosquetime.presentation.screen.message.composable.CurrentTimeCardLite
import com.rpn.mosquetime.presentation.theme.MosqueTimeTheme

@Composable
fun MessageScreen(
    state: MainScreenState = MainScreenState(),
    notification: MainNotification? = null,
    onBack: () -> Unit
) {

    // Derive current minute from state.now
    val currentMinute by remember { derivedStateOf { state.now.minute } }

    // Track previous minute
    var previousMinute by remember { mutableIntStateOf(currentMinute) }

    // Mutable state for countdown
    var countdown by remember { mutableIntStateOf(60 - state.now.second) } // Initial value

    // Effect to update countdown every time second changes (every second)
    LaunchedEffect(state.now.second) {
        countdown = state.now.countDown()
        if (state.now.minute != previousMinute) {
            Log.d("TAG", "MessageScreen: previousMinute $previousMinute")
            onBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {


        // Background image (default or from notification)
        if (notification?.imageMessage.isNullOrEmpty()) {
            Image(
                painter = painterResource(id = R.drawable.bg_mosque),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            CachedImage(
                url = notification.imageMessage,
                modifier = Modifier.fillMaxSize()
            )
            /*AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(notification.imageMessage)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )*/
        }

        // Top Row: Current Time
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CurrentTimeCardLite(
                notification = notification,
                nowTime = state.now,
                weekday = state.weekday,
                enDate = state.enDate,
                hijriDate = state.hijriDate,
                is24HourFormat = state.is24HourFormat
            )
        }

        // Countdown timer UI (centered)
        Column(
            modifier = Modifier
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            /*CircularProgressIndicator(
                progress = { countdown / 59f },
                modifier = Modifier.size(80.dp),
                color = ProgressIndicatorDefaults.circularColor,
                strokeWidth = 6.dp,
                trackColor = ProgressIndicatorDefaults.circularDeterminateTrackColor,
                strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
            )*/
            Text(
                text = "$countdown s",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp)
            )
        }



        Box(
            modifier = Modifier.matchParentSize()
        ) {
            SquareProgressIndicator(
                progress = countdown / 59f,
                strokeWidth = 12.dp,
                progressColor = MaterialTheme.colorScheme.primary.copy(0.8f),
                trackColor = Color.Transparent
            )
        }

    }
}


@Preview(device = "id:tv_1080p", showBackground = true)
@Composable
fun MessageScreenPreview() {
    MosqueTimeTheme {
        MessageScreen(
            state = MainScreenState(
                now = CompatLocalTime(13, 0),
                weekday = "Monday",
                enDate = "Aug 26, 2024",
                hijriDate = "10 Muharram, 1446",
                is24HourFormat = true
            ),
            notification = MainNotification(
                id = "1",
                title = "Jumma Reminder",
                message = "Don't forget Jumma prayer at 1:15 PM",
                imageMessage = null,
                timestamp = System.currentTimeMillis(),
            ),
            onBack = {}
        )
    }
}