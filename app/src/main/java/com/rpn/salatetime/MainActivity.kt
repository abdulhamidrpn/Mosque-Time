package com.rpn.salatetime

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rpn.salatetime.domain.model.Theme
import com.rpn.salatetime.presentation.navigation.NavGraph
import com.rpn.salatetime.presentation.screen.main.MainViewModel
import com.rpn.salatetime.presentation.screen.setting.SettingsViewModel
import com.rpn.salatetime.ui.theme.SalateTimeTheme
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModel()
    private val settingsViewModel: SettingsViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Keep display on for TV/kiosk; avoids screen blanking without full WakeLock
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        //installSplashScreen()
        enableEdgeToEdge()
        setContent {
            val uiState by settingsViewModel.uiState.collectAsState()
            val useDarkTheme = when (uiState.theme) {
                Theme.LIGHT -> false
                Theme.DARK -> true
                Theme.SYSTEM -> isSystemInDarkTheme()
            }

            SalateTimeTheme(darkTheme = useDarkTheme) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph()
                }
            }
        }
    }
}

@Composable
fun PrayerScreen(viewModel: MainViewModel) {
    // Collect both states
    val clockState by viewModel.timeState.collectAsStateWithLifecycle()
    val mosqueState by viewModel.uiState.collectAsStateWithLifecycle()

    val time = clockState.currentDateTime

    if (mosqueState.isLoading && clockState.currentDateTime.year == 0) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        // --- SECTION 1: THE CLOCK & DATE ---

        Text(
            text = "🕐 ${time.hour.toString().padStart(2, '0')}:${
                time.minute.toString().padStart(2, '0')
            }:${time.second.toString().padStart(2, '0')}" ?: "", // "12:05:01"
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold
        )
        Text(text = "🗓️ ${clockState.currentDateTime.toFormattedDate()}")
        Text(text = "🌙 ${clockState.hijriDateFormatted}")

        Spacer(Modifier.height(16.dp))

        // --- SECTION 2: ACTIVE NOTIFICATIONS ---
        // Show what's happening right now (e.g., "10 mins to Asr")
        clockState.currentNotification?.let { trigger ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(text = "🔔 ${trigger.message}", modifier = Modifier.padding(8.dp))
            }
        }

        Divider(Modifier.padding(vertical = 16.dp))

        // --- SECTION 3: PRAYER TIMES LIST ---
        Text("Today's Schedule", style = MaterialTheme.typography.titleLarge)

        mosqueState.data?.todayPrayerTime?.apply {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = sunrise.uppercase(), fontWeight = FontWeight.Medium)
                Text(text = fajr.uppercase(), fontWeight = FontWeight.Medium)
                Text(text = dhuhr.uppercase(), fontWeight = FontWeight.Medium)
                Text(text = asr.uppercase(), fontWeight = FontWeight.Medium)
                Text(text = maghrib.uppercase(), fontWeight = FontWeight.Medium)
                Text(text = isha.uppercase(), fontWeight = FontWeight.Medium)
            }
        }
    }
}
