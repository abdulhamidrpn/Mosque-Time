package com.rpn.mosquetime.presentation.screen.main.composable


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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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


@Preview(showBackground = true, widthDp = 1920, heightDp = 1080, device = "id:tv_1080p")
@Preview(showBackground = true, widthDp = 720, heightDp = 1280)
@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Preview(showBackground = true, widthDp = 1200, heightDp = 800)
@Composable
private fun PreviewAdaptiveColumnLayout() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            CurrentTimeCard(
                nowTime = CompatLocalTime(14, 30, 45),
                weekday = "Saturday",
                enDate = "15 Jun 2024",
                hijriDate = "27 Dhu 1445",
                showAmPm = true,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun CurrentTimeCard(
    nowTime: CompatLocalTime,
    weekday: String,
    enDate: String,
    hijriDate: String,
    is24HourFormat: Boolean = false,
    showAmPm: Boolean = false,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Time Display
            TimeDisplay(
                nowTime = nowTime,
                showAmPm = showAmPm,
                is24HourFormat = is24HourFormat
            )

            // Weekday
            WeekdayDisplay(weekday = weekday)

            // Dates Section
            DatesSection(
                enDate = enDate,
                hijriDate = hijriDate
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
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(10.dp)
        )

        // Minutes
        val minuteStr = now.minute.toString().padStart(2, '0')
        AnimatedDigit(digit = minuteStr[0].digitToInt())
        AnimatedDigit(digit = minuteStr[1].digitToInt())

        Spacer(
            modifier = Modifier.size(
                size = dimensionResource(id = com.intuit.sdp.R.dimen._8sdp).value.dp
            )
        )
        // Seconds (smaller)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val secondStr = now.second.toString().padStart(2, '0')
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AnimatedDigit(
                    digit = secondStr[0].digitToInt(),
                    isSecond = true
                )
                AnimatedDigit(
                    digit = secondStr[1].digitToInt(),
                    isSecond = true
                )
            }

            if (showAmPm) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "SEC ${" - " + if (nowTime.hour < 12) "AM" else "PM"}",
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
}

@Composable
private fun AnimatedDigit(
    digit: Int,
    showBackground: Boolean = false,
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
                    .background(
                        color = if (isSecond)
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    )
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
                    color = MaterialTheme.colorScheme.onSurface,
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

@Composable
private fun WeekdayDisplay(weekday: String) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = weekday,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = dimensionResource(id = com.intuit.ssp.R.dimen._16ssp).value.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun DatesSection(
    enDate: String,
    hijriDate: String
) {
    Surface(
        color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.8f),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DateColumn(
                label = "GREGORIAN",
                date = enDate
            )

            Spacer(
                modifier = Modifier
                    .size(width = 2.dp, height = 40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(1.dp)
                    )
            )

            DateColumn(
                label = "HIJRI",
                date = hijriDate
            )
        }
    }
}

@Composable
private fun DateColumn(
    label: String,
    date: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(

                fontSize = dimensionResource(id = com.intuit.ssp.R.dimen._12ssp).value.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.7f)
            )
        )
        Text(
            text = date,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = dimensionResource(id = com.intuit.ssp.R.dimen._16ssp).value.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.inverseOnSurface
            )
        )
    }
}
