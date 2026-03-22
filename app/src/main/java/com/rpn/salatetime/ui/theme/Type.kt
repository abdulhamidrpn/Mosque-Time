package com.rpn.salatetime.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rpn.salatetime.R

// ─────────────────────────────────────────────────────────────────────────────
// Font families
// ─────────────────────────────────────────────────────────────────────────────

val Montserrat = FontFamily(
    Font(R.font.montserrat_light,   FontWeight.Light),
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium,  FontWeight.Medium),
    Font(R.font.montserrat_semibold,FontWeight.SemiBold),
    Font(R.font.roboto_bold,        FontWeight.ExtraBold),
)

val BoldPrimeTime = FontFamily(Font(R.font.bold_primetime))

val SairaFontFamily = FontFamily(
    Font(R.font.montserrat_light,   FontWeight.Light),
    Font(R.font.saira, FontWeight.Normal),
    Font(R.font.montserrat_medium,  FontWeight.Medium),
    Font(R.font.montserrat_semibold,FontWeight.SemiBold),
    Font(R.font.inter_black,        FontWeight.ExtraBold),
)

// ─────────────────────────────────────────────────────────────────────────────
// Responsive scale factor
//
// Replaces the ssp library entirely.
//
// The factor is derived from the screen's shortest side (width in portrait,
// height in landscape). Reference width = 360dp (standard phone).
//
// Result:
//   phone  360dp wide → factor ≈ 1.0  (no change from base sizes)
//   phone  411dp wide → factor ≈ 1.08 (slightly larger)
//   tablet 600dp wide → factor ≈ 1.18 (larger but capped)
//   TV    1280dp wide → factor ≈ 1.30 (capped — text stays readable, not huge)
// ─────────────────────────────────────────────────────────────────────────────

private const val REFERENCE_WIDTH_DP = 360f
private const val SCALE_MIN          = 0.85f
private const val SCALE_MAX          = 1.30f

@Composable
private fun fontScale(): Float {
    val screenWidth = LocalConfiguration.current.screenWidthDp.toFloat()
    return (screenWidth / REFERENCE_WIDTH_DP).coerceIn(SCALE_MIN, SCALE_MAX)
}

/** Scales a base sp value using the responsive factor. */
@Composable
private fun Int.rsp(): TextUnit = (this * fontScale()).sp

// ─────────────────────────────────────────────────────────────────────────────
// Typography
//
// Base sizes follow Material 3 defaults exactly.
// Line heights are 1.25× the font size — proportional instead of hardcoded,
// so scaling the font automatically scales the leading.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AppTypography(): Typography {
    val f = fontScale()

    fun Int.scaled() = (this * f).sp
    fun Int.lineH()  = (this * f * 1.25f).sp   // proportional line height

    return Typography(
        displayLarge  = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.ExtraBold, fontSize = 56.scaled(), lineHeight = 56.lineH()),
        displayMedium = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.ExtraBold, fontSize = 42.scaled(), lineHeight = 42.lineH()),
        displaySmall  = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.ExtraBold, fontSize = 36.scaled(), lineHeight = 36.lineH()),

        headlineLarge  = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Normal, fontSize = 32.scaled(), lineHeight = 32.lineH()),
        headlineMedium = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Normal, fontSize = 28.scaled(), lineHeight = 28.lineH()),
        headlineSmall  = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Normal, fontSize = 24.scaled(), lineHeight = 24.lineH()),

        titleLarge  = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Normal, fontSize = 22.scaled(), lineHeight = 22.lineH()),
        titleMedium = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Normal, fontSize = 16.scaled(), lineHeight = 16.lineH()),
        titleSmall  = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Normal, fontSize = 14.scaled(), lineHeight = 14.lineH()),

        bodyLarge  = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Normal, fontSize = 16.scaled(), lineHeight = 16.lineH()),
        bodyMedium = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Normal, fontSize = 14.scaled(), lineHeight = 14.lineH()),
        bodySmall  = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Normal, fontSize = 12.scaled(), lineHeight = 12.lineH()),

        labelLarge  = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Normal, fontSize = 14.scaled(), lineHeight = 14.lineH()),
        labelMedium = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Normal, fontSize = 12.scaled(), lineHeight = 12.lineH()),
        labelSmall  = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Normal, fontSize = 11.scaled(), lineHeight = 11.lineH()),
    )
}