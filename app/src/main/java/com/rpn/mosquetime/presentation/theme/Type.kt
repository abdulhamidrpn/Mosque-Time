package com.rpn.mosquetime.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.rpn.mosquetime.R

val Montserrat = FontFamily(
    Font(R.font.montserrat_light, FontWeight.Light),
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.roboto_bold, FontWeight.ExtraBold),
)
val BoldPrimeTime = FontFamily(
    Font(R.font.bold_primetime),
)

@Composable
fun AppTypography(): Typography {
    val displayLargeSize = dimensionResource(id = com.intuit.ssp.R.dimen._48ssp).value.sp
    val displayMediumSize = dimensionResource(id = com.intuit.ssp.R.dimen._42ssp).value.sp
    val displaySmallSize = dimensionResource(id = com.intuit.ssp.R.dimen._36ssp).value.sp
    val headlineLargeSize = dimensionResource(id = com.intuit.ssp.R.dimen._32ssp).value.sp
    val headlineMediumSize = dimensionResource(id = com.intuit.ssp.R.dimen._28ssp).value.sp
    val headlineSmallSize = dimensionResource(id = com.intuit.ssp.R.dimen._24ssp).value.sp
    val titleLargeSize = dimensionResource(id = com.intuit.ssp.R.dimen._22ssp).value.sp
    val titleMediumSize = dimensionResource(id = com.intuit.ssp.R.dimen._16ssp).value.sp
    val titleSmallSize = dimensionResource(id = com.intuit.ssp.R.dimen._14ssp).value.sp
    val bodyLargeSize = dimensionResource(id = com.intuit.ssp.R.dimen._16ssp).value.sp
    val bodyMediumSize = dimensionResource(id = com.intuit.ssp.R.dimen._14ssp).value.sp
    val bodySmallSize = dimensionResource(id = com.intuit.ssp.R.dimen._12ssp).value.sp
    val labelLargeSize = dimensionResource(id = com.intuit.ssp.R.dimen._14ssp).value.sp
    val labelMediumSize = dimensionResource(id = com.intuit.ssp.R.dimen._12ssp).value.sp
    val labelSmallSize = dimensionResource(id = com.intuit.ssp.R.dimen._11ssp).value.sp

    return Typography(
        displayLarge = TextStyle(
            fontFamily = Montserrat,
            fontWeight = FontWeight.ExtraBold,
            fontSize = displayLargeSize,
            lineHeight = 64.sp
        ),
        displayMedium = TextStyle(
            fontFamily = Montserrat,
            fontWeight = FontWeight.ExtraBold,
            fontSize = displayMediumSize,
            lineHeight = 52.sp
        ),
        displaySmall = TextStyle(
            fontFamily = Montserrat,
            fontWeight = FontWeight.ExtraBold,
            fontSize = displaySmallSize,
            lineHeight = 44.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = Montserrat,
            fontWeight = FontWeight.Normal,
            fontSize = headlineLargeSize,
            lineHeight = 40.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = Montserrat,
            fontWeight = FontWeight.Normal,
            fontSize = headlineMediumSize,
            lineHeight = 36.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = Montserrat,
            fontWeight = FontWeight.Normal,
            fontSize = headlineSmallSize,
            lineHeight = 32.sp
        ),
        titleLarge = TextStyle(
            fontFamily = Montserrat,
            fontWeight = FontWeight.Normal,
            fontSize = titleLargeSize,
            lineHeight = 28.sp
        ),
        titleMedium = TextStyle(
            fontFamily = Montserrat,
            fontWeight = FontWeight.Normal,
            fontSize = titleMediumSize,
            lineHeight = 24.sp
        ),
        titleSmall = TextStyle(
            fontFamily = Montserrat,
            fontWeight = FontWeight.Normal,
            fontSize = titleSmallSize,
            lineHeight = 20.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = Montserrat,
            fontWeight = FontWeight.Normal,
            fontSize = bodyLargeSize,
            lineHeight = 24.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = Montserrat,
            fontWeight = FontWeight.Normal,
            fontSize = bodyMediumSize,
            lineHeight = 20.sp
        ),
        bodySmall = TextStyle(
            fontFamily = Montserrat,
            fontWeight = FontWeight.Normal,
            fontSize = bodySmallSize,
            lineHeight = 16.sp
        ),
        labelLarge = TextStyle(
            fontFamily = Montserrat,
            fontWeight = FontWeight.Normal,
            fontSize = labelLargeSize,
            lineHeight = 20.sp
        ),
        labelMedium = TextStyle(
            fontFamily = Montserrat,
            fontWeight = FontWeight.Normal,
            fontSize = labelMediumSize,
            lineHeight = 16.sp
        ),
        labelSmall = TextStyle(
            fontFamily = Montserrat,
            fontWeight = FontWeight.Normal,
            fontSize = labelSmallSize,
            lineHeight = 16.sp
        )
    )
}
