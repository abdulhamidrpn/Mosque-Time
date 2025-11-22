package com.rpn.mosquetime.presentation.screen.message.composable


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rpn.mosquetime.domain.model.time.CompatLocalTime
import com.rpn.mosquetime.presentation.screen.main.MainNotification


@Preview(device = "id:tv_1080p", showBackground = true)
@Composable
private fun PreviewCurrentTimeCardLite() {
    CurrentTimeCardLite(
        nowTime = CompatLocalTime(14, 30, 45),
        weekday = "Saturday",
        enDate = "15 Jun 2024",
        hijriDate = "27 Dhu 1445",
        modifier = Modifier.padding(16.dp)
    )
}

/*3 Proper scrolling animation */
@Composable
fun CurrentTimeCardLite(
    notification: MainNotification? = null,
    nowTime: CompatLocalTime,
    weekday: String,
    enDate: String,
    hijriDate: String,
    is24HourFormat: Boolean = false,
    showAmPm: Boolean = false,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Time Display with Flip Animation
        TimeDisplay(nowTime, showAmPm, is24HourFormat)

        Spacer(modifier = Modifier.weight(1f))

        notification?.message?.let {
            Text(
                it,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(
                        horizontal = 16.dp, vertical = 4.dp
                    ),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        Spacer(modifier = Modifier.size(8.dp))


        // 📆 Dates (Gregorian + Hijri)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                enDate,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(
                        horizontal = 16.dp, vertical = 4.dp
                    ),
                style = MaterialTheme.typography.titleLarge.copy(
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )

            // 📅 Weekday
            Text(
                weekday,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(
                        horizontal = 16.dp, vertical = 4.dp
                    ),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Text(
                hijriDate,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(
                        horizontal = 16.dp, vertical = 4.dp
                    ),
                style = MaterialTheme.typography.titleLarge.copy(
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }

}


@Composable
private fun TimeDisplay(
    nowTime: CompatLocalTime,
    showAmPm: Boolean,
    is24HourFormat: Boolean
) {
    val now = nowTime.timeFormat(is24 = is24HourFormat)

    Row(
        modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hours
        val hourStr = now.hour.toString().padStart(2, '0')
        AnimatedDigit(digit = hourStr[0].digitToInt())
        AnimatedDigit(digit = hourStr[1].digitToInt())

        // Colon
        Text(
            text = ":",

            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
        )

        // Minutes
        val minuteStr = now.minute.toString().padStart(2, '0')
        AnimatedDigit(digit = minuteStr[0].digitToInt())
        AnimatedDigit(digit = minuteStr[1].digitToInt())
        // Colon
        Text(
            text = ":",

            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            ),
        )
        // Seconds (smaller)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val secondStr = now.second.toString().padStart(2, '0')
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AnimatedDigit(
                    digit = secondStr[0].digitToInt(),
                    isSecond = showAmPm
                )
                AnimatedDigit(
                    digit = secondStr[1].digitToInt(),
                    isSecond = showAmPm
                )
            }
            if (showAmPm) {
                Text(
                    text = if (now.hour < 12) "AM" else "PM",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = dimensionResource(id = com.intuit.ssp.R.dimen._8ssp).value.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                )

            }

        }

    }
}


@Composable
private fun AnimatedDigit(
    digit: Int,
    showBackground: Boolean = true,
    isSecond: Boolean = false
) {
    var currentDigit by remember { mutableIntStateOf(digit) }

    LaunchedEffect(digit) {
        if (digit != currentDigit) {
            currentDigit = digit
        }
    }

    Box(
        modifier = Modifier
            .then(
                if (showBackground) Modifier
                    .padding(4.dp)
                    .background(
                        color = if (isSecond)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(vertical = 8.dp, horizontal = 16.dp)
                else Modifier
            )
            .padding(horizontal = 1.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = currentDigit,
            transitionSpec = {
                slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    initialOffsetY = { -it }
                ).togetherWith(
                    slideOutVertically(
                        animationSpec = tween(200),
                        targetOffsetY = { it }
                    )
                )
            },
            label = "digit"
        ) { animatedDigit ->

            Text(
                text = animatedDigit.toString(),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    fontSize = if (isSecond)
                        dimensionResource(id = com.intuit.ssp.R.dimen._38ssp).value.sp
                    else
                        dimensionResource(id = com.intuit.ssp.R.dimen._48ssp).value.sp

                ),
                maxLines = 1,
            )
        }
    }
}