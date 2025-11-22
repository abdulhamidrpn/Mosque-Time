package com.rpn.mosquetime.presentation.screen.main


import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rpn.mosquetime.R
import com.rpn.mosquetime.domain.model.MasjidInfo
import com.rpn.mosquetime.domain.model.Timings
import com.rpn.mosquetime.domain.model.time.CompatLocalTime
import com.rpn.mosquetime.presentation.common.CachedImage
import com.rpn.mosquetime.presentation.common.TvDefaultButton
import com.rpn.mosquetime.presentation.common.TvImageDialog
import com.rpn.mosquetime.presentation.common.TvImgButton
import com.rpn.mosquetime.presentation.screen.main.composable.CurrentTimeCard
import com.rpn.mosquetime.presentation.screen.main.composable.WaktCard


@Composable
fun MainRoute(
    viewModel: MainViewModel,
    onNavigateToMessage: (MainNotification) -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    // Handle one-off effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { eff ->
            when (eff) {
                MainScreenEffect.OpenMore -> {
                    onNavigateToSettings()
                }

                MainScreenEffect.OpenQr -> {

                }

                is MainScreenEffect.Error -> {

                }

                is MainScreenEffect.ShowNotification -> {
                    onNavigateToMessage(eff.notification)
                }

                is MainScreenEffect.ShowToast -> {
                    Toast.makeText(context, eff.message, Toast.LENGTH_SHORT)
                        .show()
                }

                else -> {}
            }
        }
    }

    MainScreen(
        state = state,
        onEvent = viewModel::onEvent
    )
}


@Composable
fun MainScreen(
    state: MainScreenState,
    onEvent: (MainScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val surfaceColor = MaterialTheme.colorScheme.surface


    Box(modifier.fillMaxSize()) {
        // Background image with enhanced readability
        if (state.selectedBackgroundImage.isEmpty()) {
            Image(
                painter = painterResource(id = R.drawable.bg_mosque),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            CachedImage(
                url = state.selectedBackgroundImage,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Enhanced background overlay for TV readability
        Box(
            Modifier
                .fillMaxSize()
                .blur(35.dp)
        ) {
            // Additional dark overlay for better contrast
            Box(
                Modifier
                    .fillMaxSize()
                    .shadow(
                        elevation = 8.dp,
                        spotColor = Color.Black.copy(alpha = 0.4f),
                        ambientColor = Color.Black.copy(alpha = 0.3f)
                    )
            )
        }

        // Main content with TV-optimized spacing
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom)
        ) {
            // Enhanced mosque name display
            if (state.showMosqueName) {
                TvDefaultButton(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp),
                    onClick = { onEvent(MainScreenEvent.OnMoreClick) }
                ) {
                    Text(
                        text = state.mosqueName,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = dimensionResource(id = com.intuit.ssp.R.dimen._16ssp).value.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        ),
                        maxLines = 1
                    )
                }

            }

            // Top Row: Sunrise | Time | Jumma

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                WaktCard(
                    title = stringResource(R.string.sunrise),
                    time = state.timings.sunrise,
                    is24HourFormat = state.is24HourFormat,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterVertically)
                        .weight(3f),
                    contentAlignment = Alignment.Center
                ) {

                    CurrentTimeCard(
                        nowTime = state.now,
                        weekday = state.weekday,
                        enDate = state.enDate,
                        hijriDate = state.hijriDate,
                        is24HourFormat = state.is24HourFormat
                    )
                }
                WaktCard(
                    title = stringResource(R.string.jumah),
                    time = state.jummaTiming,
                    is24HourFormat = state.is24HourFormat,
                    modifier = Modifier.weight(1f)
                )
            }

            // Five prayer times with enhanced visual hierarch
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WaktCard(
                    title = stringResource(R.string.fajr),
                    time = state.timings.fajr,
                    is24HourFormat = state.is24HourFormat,
                    modifier = Modifier.weight(1f)
                )
                WaktCard(
                    title = stringResource(R.string.dhuhr),
                    time = state.timings.dhuhr,
                    is24HourFormat = state.is24HourFormat,
                    modifier = Modifier.weight(1f)
                )
                WaktCard(
                    title = stringResource(R.string.asr),
                    time = state.timings.asr,
                    is24HourFormat = state.is24HourFormat,
                    modifier = Modifier.weight(1f)
                )
                WaktCard(
                    title = stringResource(R.string.maghrib),
                    time = state.timings.maghrib,
                    is24HourFormat = state.is24HourFormat,
                    modifier = Modifier.weight(1f)
                )
                WaktCard(
                    title = stringResource(R.string.isha),
                    time = state.timings.isha,
                    is24HourFormat = state.is24HourFormat,
                    modifier = Modifier.weight(1f)
                )
            }


            // Enhanced action buttons with better TV focus
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {

                TvImgButton(
                    icon = R.drawable.qr,
                    onClick = { onEvent(MainScreenEvent.OnQrClick) }
                )

                Spacer(Modifier.weight(1f))

                TvImgButton(
                    icon = R.drawable.logo,
                    onClick = { onEvent(MainScreenEvent.OnMoreClick) },
                )
            }

            // Enhanced marquee message with better TV readability
            Surface(
                color = surfaceColor.copy(alpha = 0.95f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = state.mosqueMessage.replace(Regex("<[^>]*>"), " ").trim(),
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .basicMarquee(iterations = Int.MAX_VALUE),
                    maxLines = 1,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = dimensionResource(id = com.intuit.ssp.R.dimen._12ssp).value.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    ),
                    overflow = TextOverflow.Visible
                )
            }

        }


        // Enhanced loading indicator
        if (state.isLoading) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shadowElevation = 12.dp,
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(32.dp)
                            .size(64.dp),
                        strokeWidth = 6.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Enhanced notification banner
        if (state.showNotificationBanner) {
            TvImageDialog(
                imageRes = R.drawable.qr,
                onDismiss = { onEvent(MainScreenEvent.DismissNotificationBanner) }
            )
        }
    }
}


@Preview(showBackground = true, widthDp = 1920, heightDp = 1080, device = "id:tv_1080p")
@Preview(showBackground = true, widthDp = 720, heightDp = 1280)
@Preview(showBackground = true, widthDp = 1200, heightDp = 800)
@Composable
private fun PreviewMain() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        MainScreen(
            state = MainScreenState(
                isLoading = false,
                now = CompatLocalTime(16, 12, 45),
                weekday = "Sunday",
                enDate = "24/Aug/25",
                hijriDate = "18 Safar 1447",
                showMosqueName = true,
                timings = Timings(
                    fajr = "04:12",
                    dhuhr = "12:03",
                    asr = "04:31",
                    maghrib = "06:19",
                    isha = "07:35",
                    sunrise = "05:31"
                ),
                masjidInfo = MasjidInfo(
                    thumbnail = "https://firebasestorage.googleapis.com/v0/b/mosque-time.apppatch.com/o/IMAGE_STORAGE%2FMOSQUE_MESSAGE%2F48959?alt=media&token=c69d0139-857e-4eb3-a34c-dc9e01be816e",
                    message = "Welcome to the Masjid - This is a sample scrolling message to demonstrate marquee",
                    jumua = "01:30"
                )
            ),
            onEvent = {}
        )
    }
}