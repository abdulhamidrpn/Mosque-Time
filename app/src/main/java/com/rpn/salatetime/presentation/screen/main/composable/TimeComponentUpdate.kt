package com.rpn.salatetime.presentation.screen.main.composable


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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rpn.salatetime.domain.model.CompatDayOfWeek
import com.rpn.salatetime.domain.model.CompatLocalDateTime
import com.rpn.salatetime.domain.model.CompatLocalTime
import com.rpn.salatetime.ui.theme.SairaFontFamily


// ─────────────────────────────────────────────────────────────────────────────
// Responsive scale — same approach as AppTypography, no ssp/sdp needed
// ─────────────────────────────────────────────────────────────────────────────

private const val REFERENCE_WIDTH = 360f

@Composable
private fun fontScale() =
    (LocalConfiguration.current.screenWidthDp / REFERENCE_WIDTH).coerceIn(0.85f, 1.30f)

@Composable
private fun Int.rsp() = (this * fontScale()).sp
@Composable
private fun Int.rdp() = (this * fontScale()).dp


@Composable
fun CurrentTimeCardUpdate(
    dateTime: CompatLocalDateTime,
    hijriDate: String,
    is24Hour: Boolean = false,
    showAmPm: Boolean = true,
    modifier: Modifier = Modifier,
) {

    Column(
        modifier = modifier.background(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            shape = MaterialTheme.shapes.extraLarge
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Time Display
        TimeDisplay(
            dateTime = dateTime,
            is24Hour = is24Hour,
            showAmPm = showAmPm,
        )

        //Todo: Weekday
        //WeekdayDisplay(weekday = weekday)

        // Dates Section
        DatesSection(
            enDate = dateTime.toFormattedDate(),
            hijriDate = hijriDate,
            weekday = dateTime.dayOfWeek.name
                .lowercase()
                .replaceFirstChar { it.uppercase() },
        )

    }
}

@Composable
private fun TimeDisplay(
    dateTime: CompatLocalDateTime,
    is24Hour: Boolean,
    showAmPm: Boolean,
) {
    // 12-hour adjustment lives in the display layer only — CompatLocalDateTime
    // always stores the true 24-hour value.
    val displayHour = if (!is24Hour) {
        when {
            dateTime.hour == 0 -> 12
            dateTime.hour > 12 -> dateTime.hour - 12
            else -> dateTime.hour
        }
    } else dateTime.hour

    val isAm = dateTime.hour < 12

    Row(
        modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hours
        val hourStr = displayHour.toString().padStart(2, '0')

        AnimatedDigit(digit = hourStr[0].digitToInt())
        AnimatedDigit(digit = hourStr[1].digitToInt())

        // Colon
        Text(
            text = ":",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 36.rsp(),
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier.padding(10.dp),
        )

        // Minutes
        val minuteStr = dateTime.minute.toString().padStart(2, '0')
        AnimatedDigit(digit = minuteStr[0].digitToInt())
        AnimatedDigit(digit = minuteStr[1].digitToInt())

        Spacer(Modifier.size(8.rdp()))
        // Seconds (smaller)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val secondStr = dateTime.second.toString().padStart(2, '0')
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
                Text(
                    text = "SEC - ${if (isAm) "AM" else "PM"}",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = SairaFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 8.rsp(),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    ),
                )
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
                    fontSize = if (isSecond) 36.rsp() else 46.rsp(),
                ),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun WeekdayDisplay(weekday: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = weekday,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontFamily = SairaFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.rsp(),
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun DatesSection(
    enDate: String,
    hijriDate: String,
    weekday: String
) {
    Row(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 1f),
                shape = MaterialTheme.shapes.large
            )
            .padding(horizontal = 32.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DateColumn(
            label = "",
            date = enDate
        )

        Spacer(
            modifier = Modifier
                .size(width = 4.dp, height = 40.dp)
                .background(
                    color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(2.dp)
                )
        )
        WeekdayDisplay(weekday)
        Spacer(
            modifier = Modifier
                .size(width = 4.dp, height = 40.dp)
                .background(
                    color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(2.dp)
                )
        )

        DateColumn(
            label = "",
            date = hijriDate
        )
    }
}

@Composable
private fun DateColumn(
    label: String = "",
    date: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (label.isNotEmpty())
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 12.rsp(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.7f)
                )
            )
        Text(
            text = date,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = SairaFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.rsp(),
                color = MaterialTheme.colorScheme.inverseOnSurface
            )
        )
    }
}


// --- Preview to verify the style ---
@Preview(widthDp = 400, showBackground = true, backgroundColor = 0xFF222222)
@Composable
private fun PreviewDatesSectionStyle() {
    MaterialTheme {
        Box(Modifier.padding(24.dp)) {
            DatesSection(
                enDate = "13 Feb 2026",
                hijriDate = "25 Sha 1447",
                weekday = "Sunday"
            )
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

private val previewDateTime = CompatLocalDateTime(
    year = 2026, month = 3, dayOfMonth = 15,
    hour = 14, minute = 30, second = 45,
    dayOfWeek = CompatDayOfWeek.SATURDAY,
)

@Preview(showBackground = true, widthDp = 1920, heightDp = 1080, device = "id:tv_1080p")
@Preview(showBackground = true, widthDp = 1200, heightDp = 800)
@Preview(showBackground = true, widthDp = 720, heightDp = 1280)
@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun PreviewCurrentTimeCard() {
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            CurrentTimeCardUpdate(
                dateTime = previewDateTime,
                hijriDate = "27 Dhu 1445",
                showAmPm = true,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview(widthDp = 400, showBackground = true, backgroundColor = 0xFF222222)
@Composable
private fun PreviewDatesSection() {
    MaterialTheme {
        Box(Modifier.padding(24.dp)) {
            DatesSection(
                enDate = previewDateTime.toFormattedDate(),
                hijriDate = "25 Sha 1447",
                weekday = "Sunday",
            )
        }
    }
}