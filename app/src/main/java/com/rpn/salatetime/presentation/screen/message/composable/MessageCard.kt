package com.rpn.salatetime.presentation.screen.message.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rpn.salatetime.R
import com.rpn.salatetime.domain.model.CompatLocalTime
import com.rpn.salatetime.domain.model.NotificationTrigger
import com.rpn.salatetime.domain.model.TriggerType

// ─────────────────────────────────────────────────────────────────────────────
// Responsive scale
// Reference 360 dp (phone) = 1.0  |  768 dp (tablet) ≈ 1.18  |  1280 dp (TV) = 1.30
// ─────────────────────────────────────────────────────────────────────────────

private const val SCALE_REFERENCE_WIDTH = 360f
private const val SCALE_MIN = 0.85f
private const val SCALE_MAX = 1.30f

@Composable
private fun screenScale(): Float =
    (LocalConfiguration.current.screenWidthDp / SCALE_REFERENCE_WIDTH)
        .coerceIn(SCALE_MIN, SCALE_MAX)

@Composable
private fun Float.rsp(): TextUnit = (this * screenScale()).sp

@Composable
private fun Int.rsp(): TextUnit = toFloat().rsp()

@Composable
private fun Float.rdp(): Dp = (this * screenScale()).dp

@Composable
private fun Int.rdp(): Dp = toFloat().rdp()

// ─────────────────────────────────────────────────────────────────────────────
// Offset that triggers the countdown-only mode
// ─────────────────────────────────────────────────────────────────────────────

private const val FINAL_MINUTE_OFFSET = -1   // 1 minute before prayer

// ─────────────────────────────────────────────────────────────────────────────
// Public composable
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Full-screen clock card for the message / notification screen.
 *
 * Special behaviour when [notification] has offsetMinutes == -1 (final minute
 * before prayer): the clock area switches to a large isolated seconds countdown
 * — hours and minutes are hidden so the countdown fills the space dramatically.
 *
 * @param time                 Live wall-clock time (H:M:S).
 * @param weekday              Localised day name, e.g. "Friday".
 * @param gregorianDate        Formatted Gregorian date, e.g. "15 Jun 2024".
 * @param hijriDate            Formatted Hijri date, e.g. "27 Dhu 1445".
 * @param notification         Active [NotificationTrigger] — drives message and clock mode.
 * @param is24HourFormat       True → 24 h; false → 12 h (AM/PM tile appears when false).
 * @param showAmPm             True → show AM/PM tile after the seconds column.
 * @param useSimpleTimeDisplay True → compact single-line "HH:mm:ss" text.
 */
@Composable
fun CurrentTimeCardLite(
    time: CompatLocalTime,
    weekday: String,
    gregorianDate: String,
    hijriDate: String,
    notification: NotificationTrigger? = null,
    is24HourFormat: Boolean = false,
    showAmPm: Boolean = false,
    useSimpleTimeDisplay: Boolean = false,
    modifier: Modifier = Modifier,
) {
    // True when we are exactly 1 minute away — show seconds-only countdown.
    val isCountdownMode = notification?.offsetMinutes == FINAL_MINUTE_OFFSET

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.rdp()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        // ── Clock area ────────────────────────────────────────────────────────
        when {
            isCountdownMode -> SecondsCountdownDisplay(time = time)
            useSimpleTimeDisplay -> SimpleClockDisplay(time = time, is24HourFormat = is24HourFormat)
            else -> AnimatedClockDisplay(
                time = time,
                is24HourFormat = is24HourFormat,
                showAmPm = showAmPm,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // ── Prayer notification message ───────────────────────────────────────
        val message = notification?.message
        if (!message.isNullOrBlank()) {
            PrayerNotificationLabel(message = message)
        }

        Spacer(modifier = Modifier.height(8.rdp()))

        // ── Date pills ────────────────────────────────────────────────────────
        DateRow(
            gregorianDate = gregorianDate,
            weekday = weekday,
            hijriDate = hijriDate,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Clock — seconds-only countdown (final minute before prayer)
//
// Displays only the two second digits at maximum scale — no hours, no minutes.
// The digits use the same animated tile as the full clock so the animation
// language is consistent. Size is bumped to 96 rsp so they dominate the screen.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SecondsCountdownDisplay(time: CompatLocalTime) {
    val secondStr = time.countDown().toString().padStart(2, '0')

    Row(
        modifier = Modifier.padding(horizontal = 32.rdp(), vertical = 16.rdp()),
        horizontalArrangement = Arrangement.spacedBy(4.rdp()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ClockDigitTile(
            digit = secondStr[0].digitToInt(),
            isSecondaryStyle = false,   // full primary scale — we want maximum impact
            fontSizeOverride = 96.rsp(),
        )
        ClockDigitTile(
            digit = secondStr[1].digitToInt(),
            isSecondaryStyle = false,
            fontSizeOverride = 96.rsp(),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Clock — compact single-line variant
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SimpleClockDisplay(
    time: CompatLocalTime,
    is24HourFormat: Boolean,
) {
    val displayHour = time.toDisplayHour(is24HourFormat)
    val text = "%02d:%02d:%02d".format(displayHour, time.minute, time.second)

    Text(
        text = text,
        modifier = Modifier.padding(vertical = 16.rdp(), horizontal = 24.rdp()),
        style = MaterialTheme.typography.displayMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            fontSize = 32.rsp(),
        ),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Clock — full animated digit variant
//
// Layout:  [H][H] : [M][M] : [S][S] [AM/PM]
//
// AM/PM uses the same AnimatedContent tile as the digits but at a smaller
// scale (18 rsp font / 10 rdp padding) and sits inline after the seconds,
// bottom-aligned within the seconds Column.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AnimatedClockDisplay(
    time: CompatLocalTime,
    is24HourFormat: Boolean,
    showAmPm: Boolean,
) {
    val displayHour = time.toDisplayHour(is24HourFormat)
    val isAfternoon = time.hour >= 12
    val amPmText = if (isAfternoon) "PM" else "AM"

    Row(
        modifier = Modifier.padding(start = 32.rdp(), end = 32.rdp(), top = 16.rdp()),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ── Hours ──────────────────────────────────────────────────────────────
        val hourStr = displayHour.toString().padStart(2, '0')
        ClockDigitTile(digit = hourStr[0].digitToInt(), isSecondaryStyle = false)
        ClockDigitTile(digit = hourStr[1].digitToInt(), isSecondaryStyle = false)

        ClockColonSeparator()

        // ── Minutes ────────────────────────────────────────────────────────────
        val minuteStr = time.minute.toString().padStart(2, '0')
        ClockDigitTile(digit = minuteStr[0].digitToInt(), isSecondaryStyle = false)
        ClockDigitTile(digit = minuteStr[1].digitToInt(), isSecondaryStyle = false)

        ClockColonSeparator()

        // ── Seconds + AM/PM tile ───────────────────────────────────────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.rdp()),
            verticalAlignment = Alignment.Bottom,
        ) {
            val secondStr = time.second.toString().padStart(2, '0')
            // Seconds always use isSecondaryStyle = true (smaller, secondary tint)
            ClockDigitTile(digit = secondStr[0].digitToInt(), isSecondaryStyle = false)
            ClockDigitTile(digit = secondStr[1].digitToInt(), isSecondaryStyle = false)

            // AM/PM animated tile — same AnimatedContent mechanism as digits,
            // bottom-aligned so it sits flush with the base of the second tiles,
            // scaled to ~half the secondary digit size.
            if (showAmPm && !is24HourFormat) {
                AmPmTile(text = amPmText)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AM/PM animated tile
//
// Slides in/out exactly like the clock digits when AM flips to PM at noon.
// Font: 18 rsp (≈ half the secondary digit size) — readable but unobtrusive.
// Padding is tighter than the digit tiles to keep it compact.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AmPmTile(text: String) {
    Box(
        modifier = Modifier
            .padding(bottom = 4.rdp(), start = 2.rdp())
            .background(
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(6.rdp()),
            )
            .padding(horizontal = 6.rdp(), vertical = 4.rdp()),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = text,
            transitionSpec = {
                slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
                    initialOffsetY = { -it },
                ).togetherWith(
                    slideOutVertically(
                        animationSpec = tween(200),
                        targetOffsetY = { it },
                    )
                )
            },
            label = "amPm",
        ) { animatedText ->
            Text(
                text = animatedText,
                maxLines = 1,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 18.rsp(),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.80f),
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Clock digit tile
//
// [isSecondaryStyle]
//   false  →  46 rsp font, primary tint  (hours & minutes, or countdown mode)
//   true   →  36 rsp font, secondary tint (seconds in full clock mode)
//
// [fontSizeOverride] — used by SecondsCountdownDisplay to pump seconds up to
// 96 rsp without needing a separate composable.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ClockDigitTile(
    digit: Int,
    isSecondaryStyle: Boolean,
    fontSizeOverride: TextUnit = TextUnit.Unspecified,
) {
    val backgroundColor = if (isSecondaryStyle)
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f)
    else
        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)

    val resolvedFontSize = when {
        fontSizeOverride != TextUnit.Unspecified -> fontSizeOverride
        isSecondaryStyle -> 36.rsp()
        else -> 46.rsp()
    }

    Box(
        modifier = Modifier
            .padding(2.rdp())
            .background(color = backgroundColor, shape = RoundedCornerShape(12.rdp()))
            .padding(horizontal = 12.rdp(), vertical = 8.rdp()),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = digit,
            transitionSpec = {
                slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
                    initialOffsetY = { -it },
                ).togetherWith(
                    slideOutVertically(
                        animationSpec = tween(200),
                        targetOffsetY = { it },
                    )
                )
            },
            label = "clockDigit",
        ) { animatedDigit ->
            Text(
                text = animatedDigit.toString(),
                maxLines = 1,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    fontSize = resolvedFontSize,
                ),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Colon separator
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ClockColonSeparator() {
    Text(
        text = ":",
        style = MaterialTheme.typography.displayLarge.copy(
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center,
            fontSize = 46.rsp(),
        ),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Notification message label
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PrayerNotificationLabel(message: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(id = R.drawable.no_mobile),
            contentDescription = null,
            modifier = Modifier.size(46.rdp()),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.size(12.dp))

        Text(
            text = message,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.rdp()),
                )
                .padding(horizontal = 16.rdp(), vertical = 4.rdp()),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            ),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Date row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DateRow(
    gregorianDate: String,
    weekday: String,
    hijriDate: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.rdp(), vertical = 8.rdp()),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DateLabel(text = gregorianDate, usePrimaryContainer = true)
        WeekdayLabel(text = weekday)
        DateLabel(text = hijriDate, usePrimaryContainer = true)
    }
}

@Composable
private fun DateLabel(text: String, usePrimaryContainer: Boolean) {
    val bgColor = if (usePrimaryContainer)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
    else
        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
    val textColor = if (usePrimaryContainer)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurface

    Text(
        text = text,
        modifier = Modifier
            .background(color = bgColor, shape = RoundedCornerShape(8.rdp()))
            .padding(horizontal = 16.rdp(), vertical = 4.rdp()),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = textColor,
        ),
    )
}

@Composable
private fun WeekdayLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                shape = RoundedCornerShape(8.rdp()),
            )
            .padding(horizontal = 16.rdp(), vertical = 4.rdp()),
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        ),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// 12h conversion helper
// ─────────────────────────────────────────────────────────────────────────────

private fun CompatLocalTime.toDisplayHour(is24HourFormat: Boolean): Int = when {
    is24HourFormat -> hour
    hour == 0 -> 12
    hour > 12 -> hour - 12
    else -> hour
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview data
// ─────────────────────────────────────────────────────────────────────────────

private val previewTime = CompatLocalTime(name = "now", hour = 14, minute = 30, second = 45)

private val previewBeforeTrigger = NotificationTrigger(
    compatLocalTime = CompatLocalTime(name = "maghrib", hour = 18, minute = 3),
    triggerType = TriggerType.BEFORE_PRAYER,
    offsetMinutes = -5,
    triggerTime = previewTime,
    imageMessage = null,
    message = "Maghrib in 5 minutes",
)

/** Triggers the large seconds-only countdown mode. */
private val previewFinalMinuteTrigger = NotificationTrigger(
    compatLocalTime = CompatLocalTime(name = "maghrib", hour = 18, minute = 3),
    triggerType = TriggerType.BEFORE_PRAYER,
    offsetMinutes = -1,               // ← activates SecondsCountdownDisplay
    triggerTime = previewTime,
    imageMessage = null,
    message = "Maghrib in less than 1 minute!",
)

private val previewAfterTrigger = NotificationTrigger(
    compatLocalTime = CompatLocalTime(name = "fajr", hour = 4, minute = 50),
    triggerType = TriggerType.AFTER_PRAYER,
    offsetMinutes = 10,
    triggerTime = previewTime,
    imageMessage = null,
    message = "Fajr prayer — 10 minutes ago",
)

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

@Preview(name = "TV 4K — Before prayer (-5m)", device = "id:tv_4k", showBackground = true)
@Composable
private fun PreviewTv4kBefore() {
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            CurrentTimeCardLite(
                time = previewTime,
                weekday = "Friday",
                gregorianDate = "15 Jun 2024",
                hijriDate = "27 Dhu 1445",
                notification = previewBeforeTrigger,
            )
        }
    }
}

@Preview(
    name = "TV 1080p — Final minute countdown (-1m)",
    device = "id:tv_1080p",
    showBackground = true
)
@Composable
private fun PreviewTv1080pFinalMinute() {
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            CurrentTimeCardLite(
                time = previewTime,
                weekday = "Friday",
                gregorianDate = "15 Jun 2024",
                hijriDate = "27 Dhu 1445",
                notification = previewFinalMinuteTrigger,
            )
        }
    }
}

@Preview(name = "TV 1080p — After prayer (+10m)", device = "id:tv_1080p", showBackground = true)
@Composable
private fun PreviewTv1080pAfter() {
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            CurrentTimeCardLite(
                time = previewTime,
                weekday = "Friday",
                gregorianDate = "15 Jun 2024",
                hijriDate = "27 Dhu 1445",
                notification = previewAfterTrigger,
            )
        }
    }
}

@Preview(
    name = "Tablet 12h AM/PM — final minute",
    showBackground = true,
    widthDp = 768,
    heightDp = 1024
)
@Composable
private fun PreviewTabletFinalMinute12h() {
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            CurrentTimeCardLite(
                time = previewTime,
                weekday = "Friday",
                gregorianDate = "15 Jun 2024",
                hijriDate = "27 Dhu 1445",
                notification = previewFinalMinuteTrigger,
                is24HourFormat = false,
                showAmPm = true,
            )
        }
    }
}

@Preview(
    name = "Tablet 12h AM/PM — before prayer",
    showBackground = true,
    widthDp = 768,
    heightDp = 1024
)
@Composable
private fun PreviewTablet12hBefore() {
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            CurrentTimeCardLite(
                time = previewTime,
                weekday = "Friday",
                gregorianDate = "15 Jun 2024",
                hijriDate = "27 Dhu 1445",
                notification = previewBeforeTrigger,
                is24HourFormat = false,
                showAmPm = true,
            )
        }
    }
}

@Preview(name = "Phone — before prayer", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewPhoneBefore() {
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            CurrentTimeCardLite(
                time = previewTime,
                weekday = "Saturday",
                gregorianDate = "15 Jun 2024",
                hijriDate = "27 Dhu 1445",
                notification = previewBeforeTrigger,
            )
        }
    }
}

@Preview(
    name = "Phone — final minute countdown",
    showBackground = true,
    widthDp = 360,
    heightDp = 780
)
@Composable
private fun PreviewPhoneFinalMinute() {
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            CurrentTimeCardLite(
                time = previewTime,
                weekday = "Saturday",
                gregorianDate = "15 Jun 2024",
                hijriDate = "27 Dhu 1445",
                notification = previewFinalMinuteTrigger,
            )
        }
    }
}

@Preview(name = "Phone — no notification", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewPhoneNoNotification() {
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            CurrentTimeCardLite(
                time = previewTime,
                weekday = "Saturday",
                gregorianDate = "15 Jun 2024",
                hijriDate = "27 Dhu 1445",
            )
        }
    }
}

@Preview(name = "Phone — compact clock", showBackground = true, widthDp = 360, heightDp = 360)
@Composable
private fun PreviewPhoneCompact() {
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            CurrentTimeCardLite(
                time = previewTime,
                weekday = "Saturday",
                gregorianDate = "15 Jun 2024",
                hijriDate = "27 Dhu 1445",
                useSimpleTimeDisplay = true,
            )
        }
    }
}