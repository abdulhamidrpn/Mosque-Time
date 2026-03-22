package com.rpn.salatetime.presentation.screen.main.composable

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.rpn.salatetime.domain.model.CompatLocalTime
import com.rpn.salatetime.ui.theme.SairaFontFamily

// ── Colour tokens — change once, reflected everywhere ────────────────────────

private val ActiveBackground = Color(0xFFE0C097)
private val ActiveContent = Color(0xFF2D2D2D)

/**
 * WaktCard — a single, unified prayer-time card.
 *
 * Replaces the old WaktCard + WaktCardx pair.
 *
 * Font sizes are expressed in `em` (relative to the parent text style) so they
 * scale automatically across screen densities and user font-size preferences —
 * no ssp library needed, no hardcoded sp values.
 *
 * @param title         Prayer name, e.g. "Fajr".
 * @param time          "HH:mm" string from the database.
 * @param isActive      Highlights the card with a golden accent.
 * @param is24Hour      Toggles 12 / 24-hour display.
 * @param modifier      Caller controls size: weight(1f) in a Row, fixed size, etc.
 */
@Composable
fun WaktCard(
    title: String,
    time: String,
    isActive: Boolean = false,
    is24Hour: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val animSpec = tween<Color>(500)

    val cardColor by animateColorAsState(
        targetValue = if (isActive) ActiveBackground
        else MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.75f),
        animationSpec = animSpec,
        label = "cardBg",
    )
    val contentColor by animateColorAsState(
        targetValue = if (isActive) ActiveContent
        else MaterialTheme.colorScheme.inverseOnSurface,
        animationSpec = animSpec,
        label = "contentColor",
    )
    val elevation by animateDpAsState(
        targetValue = if (isActive) 8.dp else 2.dp,
        label = "elevation",
    )

    // Parse + format once; only recomputes when time or format toggle changes
    val displayTime = remember(time, is24Hour) {
        runCatching {
            val (h, m) = time.split(":").map { it.toInt() }
            CompatLocalTime(hour = h, minute = m).toDisplayString(is24Hour,false)
        }.getOrDefault(time)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = cardColor, shape = MaterialTheme.shapes.medium)
            .padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ── Label ─────────────────────────────────────────────────────────────
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = SairaFontFamily,
                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                // 0.75em of labelMedium = always proportional to the base style
                fontSize = MaterialTheme.typography.labelMedium.fontSize * 0.85,
                color = contentColor.copy(alpha = if (isActive) 0.85f else 0.7f),
                letterSpacing = 0.08.em,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(4.dp))

        // ── Time value ────────────────────────────────────────────────────────
        Text(
            text = displayTime,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = SairaFontFamily,
                fontWeight = FontWeight.Black,
                color = contentColor,
                letterSpacing = (-0.02).em,
            ),
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

@Preview(name = "Row — all prayers", widthDp = 420, showBackground = true)
@Composable
private fun PreviewRow() {
    Row(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf(
            "Fajr" to "04:30",
            "Dhuhr" to "12:30",
            "Asr" to "16:15"
        ).forEachIndexed { i, (name, t) ->
            WaktCard(title = name, time = t, isActive = i == 1, modifier = Modifier.weight(1f))
        }
    }
}

@Preview(name = "12-hour format", widthDp = 200, showBackground = true)
@Composable
private fun Preview12Hour() {
    WaktCard(title = "Isha", time = "20:45", is24Hour = false, modifier = Modifier.padding(12.dp))
}

@Preview(name = "Active golden", widthDp = 160, showBackground = true)
@Composable
private fun PreviewActive() {
    WaktCard(title = "Maghrib", time = "18:30", isActive = true, modifier = Modifier.padding(12.dp))
}